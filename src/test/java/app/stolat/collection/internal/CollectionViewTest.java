package app.stolat.collection.internal;

import app.stolat.TestcontainersConfiguration;
import app.stolat.collection.Album;
import app.stolat.collection.CollectionService;
import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import com.github.mvysny.kaributesting.v10.spring.MockSpringServlet;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.UUID;

import static com.github.mvysny.kaributesting.v10.LocatorJ._find;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class CollectionViewTest {

    private static final Routes routes = new Routes().autoDiscoverViews("app.stolat");

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private CollectionService collectionService;

    @BeforeEach
    void setUp() {
        MockVaadin.setup(UI::new, new MockSpringServlet(routes, ctx, UI::new));
    }

    @AfterEach
    void tearDown() {
        MockVaadin.tearDown();
    }

    @Test
    @WithMockUser
    void shouldDisplayCollectionView() {
        UI.getCurrent().navigate(CollectionView.class);

        assertThat(_find(H2.class)).isNotEmpty();
        assertThat(_find(Grid.class)).isNotEmpty();
        assertThat(_find(Button.class)).isNotEmpty();
    }

    @Test
    @WithMockUser
    void shouldDisplayAlbumsInGrid() {
        collectionService.importAlbum("Radiohead", UUID.randomUUID(), "OK Computer", UUID.randomUUID());
        collectionService.importAlbum("Portishead", UUID.randomUUID(), "Dummy", UUID.randomUUID());

        UI.getCurrent().navigate(CollectionView.class);

        @SuppressWarnings("unchecked")
        Grid<Album> grid = _get(Grid.class);
        var items = grid.getGenericDataView().getItems().toList();
        assertThat(items).hasSize(2);
    }

    @Test
    @WithMockUser
    void shouldHaveScanButton() {
        UI.getCurrent().navigate(CollectionView.class);

        var scanButton = _get(Button.class, spec -> spec.withText("Scan Collection"));
        assertThat(scanButton).isNotNull();
    }
}
