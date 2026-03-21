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
import com.vaadin.flow.component.html.H2;
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

        var heading = new H2("Collection");

        searchField = new TextField();
        searchField.setPlaceholder("Search...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        formatFilter = new Select<>();
        formatFilter.setItems(ALL, DIGITAL, VINYL);
        formatFilter.setValue(ALL);
        formatFilter.setLabel("Format");

        grid = new Grid<>(Album.class, false);
        grid.addColumn(album -> album.getArtist().getName()).setHeader("Artist").setSortable(true);
        grid.addColumn(Album::getTitle).setHeader("Album").setSortable(true);
        grid.addColumn(Album::getReleaseDate).setHeader("Release Date").setSortable(true);
        grid.addColumn(album -> {
            var formats = album.getFormats();
            if (formats.isEmpty()) return "";
            return formats.stream()
                    .map(f -> f == AlbumFormat.DIGITAL ? "Digital" : "Vinyl")
                    .sorted()
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
        }).setHeader("Format").setSortable(true);
        grid.setSizeFull();

        refreshGrid();

        searchField.addValueChangeListener(event -> applySearchFilter());

        formatFilter.addValueChangeListener(event -> {
            searchField.clear();
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

        CompletableFuture.runAsync(() -> {
            var albums = collectionService.scanDirectory(Path.of(musicDirectory));
            ui.access(() -> {
                refreshGrid();
                Notification.show("Scan complete: " + albums.size() + " albums imported. " +
                        "Release dates are being fetched in the background.");
                startReleaseDatePolling(ui);
            });
        });
    }

    private void startDiscogsScan() {
        var ui = UI.getCurrent();
        Notification.show("Scanning Discogs collection...");

        CompletableFuture.runAsync(() -> {
            var albums = collectionService.scanDiscogs(discogsUsername);
            ui.access(() -> {
                refreshGrid();
                Notification.show("Discogs scan complete: " + albums.size() + " albums processed.");
                startReleaseDatePolling(ui);
            });
        });
    }

    private void startReleaseDatePolling(UI ui) {
        var thread = new Thread(() -> {
            for (int i = 0; i < 30; i++) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                ui.access(this::refreshGrid);
                log.debug("Refreshed collection grid (polling for release dates)");
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
