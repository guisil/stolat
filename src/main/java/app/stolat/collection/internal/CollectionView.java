package app.stolat.collection.internal;

import java.nio.file.Path;

import app.stolat.MainLayout;
import app.stolat.collection.Album;
import app.stolat.collection.CollectionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Value;

@Route(value = "collection", layout = MainLayout.class)
@PageTitle("Collection")
@AnonymousAllowed
public class CollectionView extends VerticalLayout {

    public CollectionView(CollectionService collectionService,
                          @Value("${stolat.collection.music-directory}") String musicDirectory) {
        var heading = new H2("Collection");

        var grid = new Grid<>(Album.class, false);
        grid.addColumn(album -> album.getArtist().getName()).setHeader("Artist");
        grid.addColumn(Album::getTitle).setHeader("Album");
        grid.setItems(collectionService.findAllAlbums());

        var scanButton = new Button("Scan Collection", event -> {
            var albums = collectionService.scanDirectory(Path.of(musicDirectory));
            grid.setItems(collectionService.findAllAlbums());
            Notification.show("Scan complete: " + albums.size() + " albums imported");
        });

        var toolbar = new HorizontalLayout(scanButton);

        add(heading, toolbar, grid);
    }
}
