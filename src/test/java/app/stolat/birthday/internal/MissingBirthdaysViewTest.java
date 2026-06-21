package app.stolat.birthday.internal;

import app.stolat.TestcontainersConfiguration;
import app.stolat.collection.Album;
import app.stolat.collection.AlbumFormat;
import app.stolat.collection.CollectionService;
import com.github.mvysny.kaributesting.v10.GridKt;
import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import com.github.mvysny.kaributesting.v10.spring.MockSpringServlet;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import app.stolat.birthday.BirthdayService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import java.util.List;
import java.util.UUID;

import com.vaadin.flow.component.button.Button;

import static com.github.mvysny.kaributesting.v10.LocatorJ._find;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static com.github.mvysny.kaributesting.v10.LocatorJ._click;
import static com.github.mvysny.kaributesting.v10.GridKt._getCellComponent;
import static com.github.mvysny.kaributesting.v10.GridKt._get;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class MissingBirthdaysViewTest {

    private static final Routes routes = new Routes().autoDiscoverViews("app.stolat");

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private BirthdayService birthdayService;

    private void setupMockVaadin() {
        MockVaadin.setup(UI::new, new MockSpringServlet(routes, ctx, UI::new));
    }

    @AfterEach
    void tearDown() {
        MockVaadin.tearDown();
    }

    @Test
    @WithMockUser
    void shouldDisplayMissingBirthdaysView() {
        setupMockVaadin();
        UI.getCurrent().navigate(MissingBirthdaysView.class);

        assertThat(_find(H2.class)).isNotEmpty();
        assertThat(_get(H2.class).getText()).isEqualTo("Missing Birthdays");
        assertThat(_find(Grid.class)).isNotEmpty();
        assertThat(_find(Span.class).stream()
                .anyMatch(s -> s.getText().contains("albums without birthdays"))).isTrue();
    }

    @Test
    @WithMockUser
    void shouldShowAlbumsWithoutBirthdays() {
        // Import an album without triggering birthday lookup (no MBID)
        collectionService.importAlbum("Test Artist", null,
                "Test Album", null, AlbumFormat.DIGITAL, List.of());

        setupMockVaadin();
        UI.getCurrent().navigate(MissingBirthdaysView.class);

        @SuppressWarnings("unchecked")
        Grid<Album> grid = _get(Grid.class);
        assertThat(GridKt._size(grid)).isGreaterThanOrEqualTo(1);
    }

    @Test
    @WithMockUser
    void shouldDisplayTryBandcampButton() {
        setupMockVaadin();
        UI.getCurrent().navigate(MissingBirthdaysView.class);

        var tryBandcampButtons = _find(Button.class).stream()
                .filter(b -> "Try Bandcamp".equals(b.getText()))
                .toList();
        assertThat(tryBandcampButtons).hasSize(1);
    }

    @Test
    @WithMockUser
    void shouldPrePopulateBandcampDialogWithStoredUrl() {
        var album = collectionService.importAlbum("Lusitanian Ghosts", null,
                "The Wrath of God", null, AlbumFormat.DIGITAL, List.of());
        collectionService.updateBandcampUrl(album.getId(),
                "https://lusitanianghosts.bandcamp.com/album/the-wrath-of-god");

        setupMockVaadin();
        UI.getCurrent().navigate(MissingBirthdaysView.class);

        @SuppressWarnings("unchecked")
        Grid<Album> grid = _get(Grid.class);
        int rowIndex = -1;
        for (int i = 0; i < GridKt._size(grid); i++) {
            if ("The Wrath of God".equals(GridKt._get(grid, i).getTitle())) {
                rowIndex = i;
                break;
            }
        }
        assertThat(rowIndex).as("album row not found in grid").isGreaterThanOrEqualTo(0);
        var actionsCell = _getCellComponent(grid, rowIndex, "actions");
        var buttons = _find(actionsCell, Button.class);
        // Buttons in actions: [retry, bandcamp] (no discogsId) — bandcamp is last
        _click(buttons.getLast());

        var urlField = _get(TextField.class, spec -> spec.withPlaceholder("https://artist.bandcamp.com/album/..."));
        assertThat(urlField.getValue())
                .isEqualTo("https://lusitanianghosts.bandcamp.com/album/the-wrath-of-god");
    }

    @Test
    @WithMockUser
    void shouldDisplayStatusBreakdownInCountLabel() {
        // Album without MBID
        collectionService.importAlbum("Artist A", null,
                "Album No MBID", null, AlbumFormat.DIGITAL, List.of());
        // Album with MBID but no birthday (failed lookup)
        var mbid = UUID.randomUUID();
        collectionService.importAlbum("Artist B", UUID.randomUUID(),
                "Album Failed Lookup", mbid, AlbumFormat.DIGITAL, List.of());

        setupMockVaadin();
        UI.getCurrent().navigate(MissingBirthdaysView.class);

        var countLabel = _find(Span.class).stream()
                .filter(s -> s.getText().contains("albums without birthdays"))
                .findFirst()
                .orElseThrow();
        assertThat(countLabel.getText()).contains("without MBID");
        assertThat(countLabel.getText()).contains("failed lookup");
    }
}
