package app.stolat.collection.internal;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import app.stolat.MainLayout;
import app.stolat.collection.Album;
import app.stolat.collection.AlbumFormat;
import app.stolat.collection.CollectionService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Route(value = "collection", layout = MainLayout.class)
@PageTitle("Collection")
@AnonymousAllowed
@Slf4j
public class CollectionView extends VerticalLayout {

    private static final String ALL = "All";
    private static final String DIGITAL = "Digital";
    private static final String VINYL = "Vinyl";

    private final CollectionService collectionService;
    private final String musicDirectory;
    private final String discogsUsername;
    private Grid<Album> grid;
    private TextField searchField;
    private Select<String> formatFilter;

    public CollectionView(CollectionService collectionService,
                          @Value("${stolat.collection.music-directory}") String musicDirectory,
                          @Value("${stolat.discogs.username:}") String discogsUsername) {
        this.collectionService = collectionService;
        this.musicDirectory = musicDirectory;
        this.discogsUsername = discogsUsername;
        setSizeFull();

        var session = VaadinSession.getCurrent();
        var savedFormat = (String) session.getAttribute("collection.format");
        var savedSearch = (String) session.getAttribute("collection.search");

        var heading = new H2("Collection");

        searchField = new TextField();
        searchField.setPlaceholder("Search...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        formatFilter = new Select<>();
        formatFilter.setItems(ALL, DIGITAL, VINYL);
        formatFilter.setValue(savedFormat != null ? savedFormat : ALL);
        formatFilter.setLabel("Format");

        grid = new Grid<>(Album.class, false);
        grid.setMultiSort(true);
        var artistColumn = grid.addColumn(album -> album.getArtist().getName()).setHeader("Artist").setSortable(true);
        grid.addColumn(Album::getTitle).setHeader("Album").setSortable(true);
        var releaseDateColumn = grid.addColumn(Album::getReleaseDate).setHeader("Release Date").setSortable(true);
        grid.addComponentColumn(album -> {
            var formats = album.getFormats();
            if (formats.isEmpty()) return new Span();
            var layout = new HorizontalLayout();
            layout.setSpacing(false);
            layout.addClassName("format-icons");
            if (formats.contains(AlbumFormat.DIGITAL)) {
                var icon = VaadinIcon.FILE_SOUND.create();
                icon.addClassName("format-icon");
                icon.setTooltipText("Digital");
                layout.add(icon);
            }
            if (formats.contains(AlbumFormat.VINYL)) {
                var icon = VaadinIcon.DISC.create();
                icon.addClassName("format-icon");
                icon.setTooltipText("Vinyl");
                layout.add(icon);
            }
            return layout;
        }).setHeader("Format").setWidth("80px").setFlexGrow(0);
        grid.setSizeFull();
        grid.getColumns().forEach(c -> c.setResizable(true));
        grid.sort(GridSortOrder.asc(artistColumn)
                .thenAsc(releaseDateColumn).build());

        refreshGrid();

        if (savedSearch != null && !savedSearch.isEmpty()) {
            searchField.setValue(savedSearch);
            applySearchFilter();
        }

        searchField.addValueChangeListener(event -> {
            session.setAttribute("collection.search", event.getValue());
            applySearchFilter();
        });

        formatFilter.addValueChangeListener(event -> {
            searchField.clear();
            session.setAttribute("collection.format", event.getValue());
            refreshGrid();
        });

        var scanButton = new Button("Scan Collection", event -> startScan());

        var toolbar = new HorizontalLayout(scanButton);

        if (!discogsUsername.isEmpty()) {
            var discogsScanButton = new Button("Scan Discogs", event -> startDiscogsScan());
            toolbar.add(discogsScanButton);
        }

        toolbar.add(formatFilter, searchField);
        toolbar.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        add(heading, toolbar, grid);
    }

    private void startScan() {
        var ui = UI.getCurrent();
        Notification.show("Scanning collection...");
        startGridPolling(ui);

        CompletableFuture.runAsync(() -> {
            var albums = collectionService.scanDirectory(Path.of(musicDirectory));
            ui.access(() -> {
                refreshGrid();
                Notification.show("Scan complete: " + albums.size() + " albums imported.");
            });
        });
    }

    private void startDiscogsScan() {
        var ui = UI.getCurrent();
        Notification.show("Scanning Discogs collection...");
        startGridPolling(ui);

        CompletableFuture.runAsync(() -> {
            var albums = collectionService.scanDiscogs(discogsUsername);
            ui.access(() -> {
                refreshGrid();
                Notification.show("Discogs scan complete: " + albums.size() + " albums processed.");
            });
        });
    }

    private void startGridPolling(UI ui) {
        var thread = new Thread(() -> {
            for (int i = 0; i < 60; i++) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                if (!ui.isAttached()) {
                    return;
                }
                ui.access(this::refreshGrid);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @SuppressWarnings("unchecked")
    private void refreshGrid() {
        var selectedFormat = formatFilter != null ? formatFilter.getValue() : ALL;
        var albums = switch (selectedFormat) {
            case DIGITAL -> collectionService.findAlbumsByFormat(AlbumFormat.DIGITAL);
            case VINYL -> collectionService.findAlbumsByFormat(AlbumFormat.VINYL);
            default -> collectionService.findAllActiveAlbums();
        };
        grid.setItems(new ListDataProvider<>(albums));
    }

    @SuppressWarnings("unchecked")
    private void applySearchFilter() {
        var dp = grid.getDataProvider();
        if (dp instanceof ListDataProvider<?>) {
            var listDataProvider = (ListDataProvider<Album>) dp;
            listDataProvider.clearFilters();
            var filterText = searchField.getValue().trim().toLowerCase();
            if (!filterText.isEmpty()) {
                listDataProvider.addFilter(album ->
                        album.getArtist().getName().toLowerCase().contains(filterText) ||
                        album.getTitle().toLowerCase().contains(filterText));
            }
        }
    }
}
