package app.stolat.birthday.internal;

import app.stolat.TestcontainersConfiguration;
import app.stolat.birthday.AlbumBirthday;
import com.github.mvysny.kaributesting.v10.GridKt;
import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import com.github.mvysny.kaributesting.v10.spring.MockSpringServlet;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import org.junit.jupiter.api.AfterEach;
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

    private void setupMockVaadin() {
        MockVaadin.setup(UI::new, new MockSpringServlet(routes, ctx, UI::new));
    }

    @AfterEach
    void tearDown() {
        MockVaadin.tearDown();
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
}
