package app.stolat.birthday.internal;

import app.stolat.TestcontainersConfiguration;
import app.stolat.birthday.AlbumBirthday;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class AlbumBirthdayRepositoryTest {

    @Autowired
    private AlbumBirthdayRepository albumBirthdayRepository;

    @Test
    void shouldPersistAndRetrieveAlbumBirthday() {
        var musicBrainzId = UUID.randomUUID();
        var releaseDate = LocalDate.of(1997, 6, 16);
        var birthday = new AlbumBirthday("OK Computer", "Radiohead", musicBrainzId, releaseDate);

        var saved = albumBirthdayRepository.save(birthday);
        var found = albumBirthdayRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getAlbumTitle()).isEqualTo("OK Computer");
        assertThat(found.get().getArtistName()).isEqualTo("Radiohead");
        assertThat(found.get().getMusicBrainzId()).isEqualTo(musicBrainzId);
        assertThat(found.get().getReleaseDate()).isEqualTo(releaseDate);
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFindBirthdaysByDateRange() {
        albumBirthdayRepository.save(new AlbumBirthday("OK Computer", "Radiohead",
                UUID.randomUUID(), LocalDate.of(1997, 6, 16)));
        albumBirthdayRepository.save(new AlbumBirthday("Kid A", "Radiohead",
                UUID.randomUUID(), LocalDate.of(2000, 10, 2)));
        albumBirthdayRepository.save(new AlbumBirthday("Dummy", "Portishead",
                UUID.randomUUID(), LocalDate.of(1994, 8, 22)));

        var results = albumBirthdayRepository.findByReleaseDateMonthAndDay(6, 16);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getAlbumTitle()).isEqualTo("OK Computer");
    }
}
