package app.stolat.birthday;

import app.stolat.TestcontainersConfiguration;
import app.stolat.birthday.internal.AlbumBirthdayRepository;
import app.stolat.collection.AlbumDiscoveredEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ApplicationModuleTest
@Import(TestcontainersConfiguration.class)
class BirthdayModuleIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private AlbumBirthdayRepository albumBirthdayRepository;

    @MockitoBean
    private ReleaseDateLookup releaseDateLookup;

    @Test
    void shouldCreateBirthdayWhenAlbumDiscoveredEventReceived() {
        var albumMbid = UUID.randomUUID();
        var releaseDate = LocalDate.of(1997, 6, 16);
        given(releaseDateLookup.lookUp(albumMbid)).willReturn(Optional.of(releaseDate));

        eventPublisher.publishEvent(
                new AlbumDiscoveredEvent(UUID.randomUUID(), "OK Computer", "Radiohead", albumMbid));

        var birthdays = albumBirthdayRepository.findAll();
        assertThat(birthdays).hasSize(1);
        assertThat(birthdays.getFirst().getAlbumTitle()).isEqualTo("OK Computer");
        assertThat(birthdays.getFirst().getArtistName()).isEqualTo("Radiohead");
        assertThat(birthdays.getFirst().getReleaseDate()).isEqualTo(releaseDate);
    }

    @Test
    void shouldNotCreateBirthdayWhenReleaseDateNotFound() {
        given(releaseDateLookup.lookUp(any())).willReturn(Optional.empty());

        eventPublisher.publishEvent(
                new AlbumDiscoveredEvent(UUID.randomUUID(), "Unknown", "Unknown", UUID.randomUUID()));

        assertThat(albumBirthdayRepository.findAll()).isEmpty();
    }
}
