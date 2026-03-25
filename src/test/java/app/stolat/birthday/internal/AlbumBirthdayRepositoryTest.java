package app.stolat.birthday.internal;

import app.stolat.TestcontainersConfiguration;
import app.stolat.birthday.AlbumBirthday;
import app.stolat.birthday.ReleaseDateSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

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
    void shouldPersistBirthdayWithReleaseDateSource() {
        var musicBrainzId = UUID.randomUUID();
        var albumId = UUID.randomUUID();
        var releaseDate = LocalDate.of(1997, 6, 16);
        var birthday = new AlbumBirthday("OK Computer", "Radiohead",
                albumId, musicBrainzId, releaseDate, ReleaseDateSource.MUSICBRAINZ);

        var saved = albumBirthdayRepository.save(birthday);
        var found = albumBirthdayRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getAlbumId()).isEqualTo(albumId);
        assertThat(found.get().getMusicBrainzId()).isEqualTo(musicBrainzId);
        assertThat(found.get().getReleaseDateSource()).isEqualTo(ReleaseDateSource.MUSICBRAINZ);
    }

    @Test
    void shouldPersistBirthdayWithAlbumIdAndNullMusicBrainzId() {
        var albumId = UUID.randomUUID();
        var releaseDate = LocalDate.of(2020, 3, 15);
        var birthday = new AlbumBirthday("Some Album", "Some Artist",
                albumId, null, releaseDate, ReleaseDateSource.DISCOGS);

        var saved = albumBirthdayRepository.save(birthday);
        var found = albumBirthdayRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getAlbumId()).isEqualTo(albumId);
        assertThat(found.get().getMusicBrainzId()).isNull();
        assertThat(found.get().getReleaseDateSource()).isEqualTo(ReleaseDateSource.DISCOGS);
    }

    @Test
    void shouldFindBirthdayByAlbumId() {
        var albumId = UUID.randomUUID();
        var releaseDate = LocalDate.of(2020, 3, 15);
        albumBirthdayRepository.save(new AlbumBirthday("Some Album", "Some Artist",
                albumId, null, releaseDate, ReleaseDateSource.DISCOGS));

        var found = albumBirthdayRepository.findByAlbumId(albumId);

        assertThat(found).isPresent();
        assertThat(found.get().getAlbumTitle()).isEqualTo("Some Album");
    }

    @Test
    void shouldFindDiscogsYearOnlyBirthdays() {
        var yearOnly = new AlbumBirthday("Album A", "Artist A",
                UUID.randomUUID(), null, 111L, LocalDate.of(2020, 1, 1), ReleaseDateSource.DISCOGS);
        var fullDate = new AlbumBirthday("Album B", "Artist B",
                UUID.randomUUID(), null, 222L, LocalDate.of(2020, 6, 15), ReleaseDateSource.DISCOGS);
        var musicBrainzJanFirst = new AlbumBirthday("Album C", "Artist C",
                UUID.randomUUID(), UUID.randomUUID(), LocalDate.of(2019, 1, 1), ReleaseDateSource.MUSICBRAINZ);
        var noDiscogsId = new AlbumBirthday("Album D", "Artist D",
                UUID.randomUUID(), null, LocalDate.of(2021, 1, 1), ReleaseDateSource.DISCOGS);
        albumBirthdayRepository.save(yearOnly);
        albumBirthdayRepository.save(fullDate);
        albumBirthdayRepository.save(musicBrainzJanFirst);
        albumBirthdayRepository.save(noDiscogsId);

        var results = albumBirthdayRepository.findDiscogsYearOnlyBirthdays(ReleaseDateSource.DISCOGS);

        assertThat(results).hasSize(1);
        assertThat(results).extracting(AlbumBirthday::getAlbumTitle, AlbumBirthday::getDiscogsId)
                .containsExactly(tuple("Album A", 111L));
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
