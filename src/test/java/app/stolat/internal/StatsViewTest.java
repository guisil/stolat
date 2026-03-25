package app.stolat.internal;

import app.stolat.TestcontainersConfiguration;
import app.stolat.birthday.AlbumBirthday;
import app.stolat.birthday.ReleaseDateSource;
import app.stolat.birthday.internal.AlbumBirthdayRepository;
import app.stolat.collection.CollectionService;
import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import com.github.mvysny.kaributesting.v10.spring.MockSpringServlet;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
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
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class StatsViewTest {

    private static final Routes routes = new Routes().autoDiscoverViews("app.stolat");

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private CollectionService collectionService;

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
    void shouldDisplayTotalAlbumCount() {
        collectionService.importAlbum("Radiohead", UUID.randomUUID(), "OK Computer", UUID.randomUUID());
        collectionService.importAlbum("Portishead", UUID.randomUUID(), "Dummy", UUID.randomUUID());

        setupMockVaadin();
        UI.getCurrent().navigate("stats");

        var spans = _find(Span.class);
        var totalSpan = spans.stream()
                .filter(s -> s.getText().contains("Total albums"))
                .findFirst();
        assertThat(totalSpan).isPresent();
        assertThat(totalSpan.get().getText()).contains("2");
    }

    @Test
    @WithMockUser
    void shouldDisplayBirthdayCountsBySource() {
        var albumId1 = UUID.randomUUID();
        var albumId2 = UUID.randomUUID();
        albumBirthdayRepository.save(new AlbumBirthday("OK Computer", "Radiohead",
                albumId1, UUID.randomUUID(),
                LocalDate.of(1997, 6, 16), ReleaseDateSource.MUSICBRAINZ));
        albumBirthdayRepository.save(new AlbumBirthday("Dummy", "Portishead",
                albumId2, null,
                LocalDate.of(1994, 8, 22), ReleaseDateSource.DISCOGS));

        setupMockVaadin();
        UI.getCurrent().navigate("stats");

        var spans = _find(Span.class);
        var totalBirthdays = spans.stream()
                .filter(s -> s.getText().contains("Total birthdays"))
                .findFirst();
        assertThat(totalBirthdays).isPresent();
        assertThat(totalBirthdays.get().getText()).contains("2");

        var musicBrainzSpan = spans.stream()
                .filter(s -> s.getText().contains("MusicBrainz"))
                .findFirst();
        assertThat(musicBrainzSpan).isPresent();
        assertThat(musicBrainzSpan.get().getText()).contains("1");

        var discogsSpan = spans.stream()
                .filter(s -> s.getText().contains("Discogs"))
                .findFirst();
        assertThat(discogsSpan).isPresent();
        assertThat(discogsSpan.get().getText()).contains("1");
    }

    @Test
    @WithMockUser
    void shouldDisplayMissingBirthdayCount() {
        // Album with birthday
        var album1 = collectionService.importAlbum("Radiohead", UUID.randomUUID(), "OK Computer", UUID.randomUUID());
        albumBirthdayRepository.save(new AlbumBirthday("OK Computer", "Radiohead",
                album1.getId(), album1.getMusicBrainzId(),
                LocalDate.of(1997, 6, 16), ReleaseDateSource.MUSICBRAINZ));
        // Album without birthday, no MBID
        collectionService.importAlbum("Unknown", null, "Mystery Album", null);
        // Album without birthday, has MBID (lookup failed)
        collectionService.importAlbum("Portishead", UUID.randomUUID(), "Dummy", UUID.randomUUID());

        setupMockVaadin();
        UI.getCurrent().navigate("stats");

        var spans = _find(Span.class);
        var missingSpan = spans.stream()
                .filter(s -> s.getText().contains("Missing birthdays"))
                .findFirst();
        assertThat(missingSpan).isPresent();
        assertThat(missingSpan.get().getText()).contains("2");
    }
}
