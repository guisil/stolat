package app.stolat.birthday.internal;

import app.stolat.TestcontainersConfiguration;
import app.stolat.birthday.AlbumBirthday;
import app.stolat.birthday.ReleaseDateSource;
import com.github.mvysny.kaributesting.v10.GridKt;
import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import com.github.mvysny.kaributesting.v10.spring.MockSpringServlet;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.UUID;

import static com.github.mvysny.kaributesting.v10.LocatorJ._find;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class BirthdayViewTest {

    private static final Routes routes = new Routes().autoDiscoverViews("app.stolat");

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private AlbumBirthdayRepository albumBirthdayRepository;

    @BeforeEach
    void setUp() {
        albumBirthdayRepository.deleteAll();
    }

    private void setupMockVaadin() {
        MockVaadin.setup(UI::new, new MockSpringServlet(routes, ctx, UI::new));
    }

    @AfterEach
    void tearDown() {
        MockVaadin.tearDown();
        albumBirthdayRepository.deleteAll();
    }

    @Test
    @WithMockUser
    void shouldDisplayBirthdayView() {
        setupMockVaadin();

        assertThat(_find(H2.class)).isNotEmpty();
        assertThat(_find(Grid.class)).isNotEmpty();
    }

    @Test
    @WithMockUser
    void shouldDisplayTodaysBirthdays() {
        var today = LocalDate.now();
        albumBirthdayRepository.save(new AlbumBirthday("OK Computer", "Radiohead", UUID.randomUUID(), today));

        setupMockVaadin();

        @SuppressWarnings("unchecked")
        Grid<AlbumBirthday> grid = _get(Grid.class);
        assertThat(GridKt._size(grid)).isEqualTo(1);
    }

    @Test
    @WithMockUser
    void shouldShowAllBirthdaysWhenAllRangeSelected() {
        albumBirthdayRepository.save(new AlbumBirthday("OK Computer", "Radiohead",
                UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.of(1997, 6, 16), ReleaseDateSource.MUSICBRAINZ));
        albumBirthdayRepository.save(new AlbumBirthday("Dummy", "Portishead",
                UUID.randomUUID(), null,
                LocalDate.of(1994, 8, 22), ReleaseDateSource.DISCOGS));

        setupMockVaadin();

        // Select "All" in the range dropdown (first Select is the range)
        @SuppressWarnings("unchecked")
        var rangeSelect = (Select<String>) _find(Select.class).getFirst();
        rangeSelect.setValue("All");

        @SuppressWarnings("unchecked")
        Grid<AlbumBirthday> grid = _get(Grid.class);
        assertThat(GridKt._size(grid)).isEqualTo(2);
    }

    @Test
    @WithMockUser
    void shouldFilterBySourceWhenSourceFilterSelected() {
        albumBirthdayRepository.save(new AlbumBirthday("OK Computer", "Radiohead",
                UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.of(1997, 6, 16), ReleaseDateSource.MUSICBRAINZ));
        albumBirthdayRepository.save(new AlbumBirthday("Dummy", "Portishead",
                UUID.randomUUID(), null,
                LocalDate.of(1994, 8, 22), ReleaseDateSource.DISCOGS));

        setupMockVaadin();

        // Select "All" range first to see both
        @SuppressWarnings("unchecked")
        var rangeSelect = (Select<String>) _find(Select.class).getFirst();
        rangeSelect.setValue("All");

        // Select source filter (second Select)
        @SuppressWarnings("unchecked")
        var sourceFilter = (Select<String>) _find(Select.class).get(1);
        sourceFilter.setValue("Discogs");

        @SuppressWarnings("unchecked")
        Grid<AlbumBirthday> grid = _get(Grid.class);
        assertThat(GridKt._size(grid)).isEqualTo(1);
    }

    @Test
    @WithMockUser
    void shouldDisplaySourceColumn() {
        albumBirthdayRepository.save(new AlbumBirthday("OK Computer", "Radiohead",
                UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.of(1997, 6, 16), ReleaseDateSource.MUSICBRAINZ));

        setupMockVaadin();

        @SuppressWarnings("unchecked")
        var rangeSelect = (Select<String>) _find(Select.class).getFirst();
        rangeSelect.setValue("All");

        @SuppressWarnings("unchecked")
        Grid<AlbumBirthday> grid = _get(Grid.class);
        var columnHeaders = grid.getColumns().stream()
                .map(c -> c.getHeaderText())
                .toList();
        assertThat(columnHeaders).contains("Source");
    }

    @Test
    @WithMockUser
    void shouldDisplayCountLabel() {
        albumBirthdayRepository.save(new AlbumBirthday("OK Computer", "Radiohead",
                UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.of(1997, 6, 16), ReleaseDateSource.MUSICBRAINZ));

        setupMockVaadin();

        @SuppressWarnings("unchecked")
        var rangeSelect = (Select<String>) _find(Select.class).getFirst();
        rangeSelect.setValue("All");

        var spans = _find(Span.class);
        var countSpan = spans.stream()
                .filter(s -> s.getText().contains("birthday"))
                .findFirst();
        assertThat(countSpan).isPresent();
        assertThat(countSpan.get().getText()).contains("1");
    }
}
