package app.stolat.collection.internal;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import app.stolat.MainLayout;
import app.stolat.collection.Album;
import app.stolat.collection.CollectionService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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

    private final CollectionService collectionService;
    private final String musicDirectory;
    private Grid<Album> grid;
    private TextField searchField;

    public CollectionView(CollectionService collectionService,
                          @Value("${stolat.collection.music-directory}") String musicDirectory) {
        this.collectionService = collectionService;
        this.musicDirectory = musicDirectory;
        setSizeFull();

        var heading = new H2("Collection");

        searchField = new TextField();
        searchField.setPlaceholder("Search...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        grid = new Grid<>(Album.class, false);
        grid.addColumn(album -> album.getArtist().getName()).setHeader("Artist").setSortable(true);
        grid.addColumn(Album::getTitle).setHeader("Album").setSortable(true);
        grid.addColumn(Album::getReleaseDate).setHeader("Release Date").setSortable(true);
        grid.setSizeFull();

        refreshGrid();

        searchField.addValueChangeListener(event -> {
            var provider = (ListDataProvider<Album>) grid.getDataProvider();
            provider.clearFilters();
            var filterText = event.getValue().trim().toLowerCase();
            if (!filterText.isEmpty()) {
                provider.addFilter(album ->
                        album.getArtist().getName().toLowerCase().contains(filterText) ||
                        album.getTitle().toLowerCase().contains(filterText));
            }
        });

        var scanButton = new Button("Scan Collection", event -> startScan());

        var toolbar = new HorizontalLayout(scanButton, searchField);
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
        searchField.clear();
        grid.setItems(new ListDataProvider<>(collectionService.findAllAlbums()));
    }
}
