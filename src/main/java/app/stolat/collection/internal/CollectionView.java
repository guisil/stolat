package app.stolat.collection.internal;

import app.stolat.MainLayout;
import app.stolat.collection.Album;
import app.stolat.collection.CollectionService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "collection", layout = MainLayout.class)
@PageTitle("Collection")
@AnonymousAllowed
public class CollectionView extends VerticalLayout {

    public CollectionView(CollectionService collectionService) {
        var heading = new H2("Collection");

        var grid = new Grid<>(Album.class, false);
        grid.addColumn(album -> album.getArtist().getName()).setHeader("Artist");
        grid.addColumn(Album::getTitle).setHeader("Album");
        grid.setItems(collectionService.findAllAlbums());

        add(heading, grid);
    }
}
