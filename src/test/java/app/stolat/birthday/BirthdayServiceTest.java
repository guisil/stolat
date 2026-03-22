package app.stolat.birthday;

import app.stolat.birthday.internal.AlbumBirthdayRepository;
import app.stolat.birthday.internal.BandcampLookup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class BirthdayServiceTest {

    @Mock
    private AlbumBirthdayRepository albumBirthdayRepository;

    @Mock
    private ReleaseDateLookup releaseDateLookup;

    @Mock
    private BandcampLookup bandcampLookup;

    @InjectMocks
    private BirthdayService birthdayService;

    @Test
    void shouldReturnBirthdaysBetweenDatesWhenRangeWithinSameYear() {
        var birthday1 = new AlbumBirthday("OK Computer", "Radiohead",
                UUID.randomUUID(), LocalDate.of(1997, 6, 16));
        var birthday2 = new AlbumBirthday("Kid A", "Radiohead",
                UUID.randomUUID(), LocalDate.of(2000, 10, 2));
        var birthday3 = new AlbumBirthday("The Bends", "Radiohead",
                UUID.randomUUID(), LocalDate.of(1995, 3, 13));
        given(albumBirthdayRepository.findAll())
                .willReturn(List.of(birthday1, birthday2, birthday3));

        var results = birthdayService.findBirthdaysBetween(
                LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 30));

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getAlbumTitle()).isEqualTo("OK Computer");
    }

    @Test
    void shouldReturnBirthdaysBetweenDatesWhenRangeWrapsAroundYearBoundary() {
        var birthday1 = new AlbumBirthday("OK Computer", "Radiohead",
                UUID.randomUUID(), LocalDate.of(1997, 6, 16));
        var birthday2 = new AlbumBirthday("Spirit of Eden", "Talk Talk",
                UUID.randomUUID(), LocalDate.of(1988, 1, 5));
        var birthday3 = new AlbumBirthday("Vitalogy", "Pearl Jam",
                UUID.randomUUID(), LocalDate.of(1994, 12, 22));
        given(albumBirthdayRepository.findAll())
                .willReturn(List.of(birthday1, birthday2, birthday3));

        var results = birthdayService.findBirthdaysBetween(
                LocalDate.of(2024, 12, 15), LocalDate.of(2025, 1, 15));

        assertThat(results).hasSize(2);
        assertThat(results).extracting(AlbumBirthday::getAlbumTitle)
                .containsExactlyInAnyOrder("Spirit of Eden", "Vitalogy");
    }

    @Test
    void shouldReturnEmptyWhenNoBirthdaysInRange() {
        var birthday1 = new AlbumBirthday("OK Computer", "Radiohead",
                UUID.randomUUID(), LocalDate.of(1997, 6, 16));
        given(albumBirthdayRepository.findAll()).willReturn(List.of(birthday1));

        var results = birthdayService.findBirthdaysBetween(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

        assertThat(results).isEmpty();
    }

    @Test
    void shouldReturnTodaysBirthdays() {
        var today = LocalDate.of(1997, 6, 16);
        var birthday = new AlbumBirthday("OK Computer", "Radiohead", UUID.randomUUID(), today);
        given(albumBirthdayRepository.findByReleaseDateMonthAndDay(6, 16))
                .willReturn(List.of(birthday));

        var results = birthdayService.findBirthdaysOn(today);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getAlbumTitle()).isEqualTo("OK Computer");
    }

    @Test
    void shouldLookUpReleaseDateAndSave() {
        var albumId = UUID.randomUUID();
        var musicBrainzId = UUID.randomUUID();
        var releaseDate = LocalDate.of(1997, 6, 16);
        given(albumBirthdayRepository.findByMusicBrainzId(musicBrainzId)).willReturn(Optional.empty());
        given(releaseDateLookup.lookUp(musicBrainzId)).willReturn(Optional.of(releaseDate));
        given(albumBirthdayRepository.save(any(AlbumBirthday.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        var result = birthdayService.resolveReleaseDate(albumId, "OK Computer", "Radiohead", musicBrainzId);

        assertThat(result).isPresent();
        assertThat(result.get().getReleaseDate()).isEqualTo(releaseDate);
        assertThat(result.get().getReleaseDateSource()).isEqualTo(ReleaseDateSource.MUSICBRAINZ);
        then(albumBirthdayRepository).should().save(any(AlbumBirthday.class));
    }

    @Test
    void shouldReturnEmptyWhenReleaseDateNotFound() {
        var musicBrainzId = UUID.randomUUID();
        given(albumBirthdayRepository.findByMusicBrainzId(musicBrainzId)).willReturn(Optional.empty());
        given(releaseDateLookup.lookUp(musicBrainzId)).willReturn(Optional.empty());

        var result = birthdayService.resolveReleaseDate(UUID.randomUUID(), "Unknown Album",
                "Unknown Artist", musicBrainzId);

        assertThat(result).isEmpty();
        then(albumBirthdayRepository).should(never()).save(any());
    }

    @Test
    void shouldSkipLookupWhenBirthdayAlreadyExists() {
        var musicBrainzId = UUID.randomUUID();
        var existing = new AlbumBirthday("OK Computer", "Radiohead",
                musicBrainzId, LocalDate.of(1997, 6, 16));
        given(albumBirthdayRepository.findByMusicBrainzId(musicBrainzId))
                .willReturn(Optional.of(existing));

        var result = birthdayService.resolveReleaseDate(UUID.randomUUID(), "OK Computer",
                "Radiohead", musicBrainzId);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(existing);
        then(releaseDateLookup).shouldHaveNoInteractions();
    }

    @Test
    void shouldSaveDirectReleaseDateAndReturn() {
        var albumId = UUID.randomUUID();
        var musicBrainzId = UUID.randomUUID();
        var releaseDate = LocalDate.of(1997, 6, 16);
        given(albumBirthdayRepository.findByMusicBrainzId(musicBrainzId)).willReturn(Optional.empty());
        given(albumBirthdayRepository.save(any(AlbumBirthday.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        var result = birthdayService.resolveReleaseDateDirect(albumId, "OK Computer", "Radiohead",
                musicBrainzId, releaseDate, ReleaseDateSource.MUSICBRAINZ);

        assertThat(result.getReleaseDate()).isEqualTo(releaseDate);
        then(albumBirthdayRepository).should().save(any(AlbumBirthday.class));
    }

    @Test
    void shouldReturnExistingWhenResolvingDirectAndAlreadyExists() {
        var musicBrainzId = UUID.randomUUID();
        var existing = new AlbumBirthday("OK Computer", "Radiohead",
                musicBrainzId, LocalDate.of(1997, 6, 16));
        given(albumBirthdayRepository.findByMusicBrainzId(musicBrainzId))
                .willReturn(Optional.of(existing));

        var result = birthdayService.resolveReleaseDateDirect(UUID.randomUUID(), "OK Computer",
                "Radiohead", musicBrainzId, LocalDate.of(1997, 6, 16), ReleaseDateSource.MUSICBRAINZ);

        assertThat(result).isEqualTo(existing);
        then(albumBirthdayRepository).should(never()).save(any());
    }

    @Test
    void shouldResolveReleaseDateForAlbumWithoutMbid() {
        var albumId = UUID.randomUUID();
        var releaseDate = LocalDate.of(2020, 1, 1);
        given(albumBirthdayRepository.findByAlbumId(albumId)).willReturn(Optional.empty());
        given(albumBirthdayRepository.save(any(AlbumBirthday.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        var result = birthdayService.resolveReleaseDateForAlbum(albumId, "Some Album",
                "Some Artist", releaseDate, ReleaseDateSource.DISCOGS);

        assertThat(result.getReleaseDate()).isEqualTo(releaseDate);
        assertThat(result.getMusicBrainzId()).isNull();
        assertThat(result.getReleaseDateSource()).isEqualTo(ReleaseDateSource.DISCOGS);
        then(albumBirthdayRepository).should().save(any(AlbumBirthday.class));
    }

    @Test
    void shouldReturnExistingWhenResolvingForAlbumAndAlreadyExists() {
        var albumId = UUID.randomUUID();
        var existing = new AlbumBirthday("Some Album", "Some Artist",
                albumId, null, LocalDate.of(2020, 1, 1), ReleaseDateSource.DISCOGS);
        given(albumBirthdayRepository.findByAlbumId(albumId)).willReturn(Optional.of(existing));

        var result = birthdayService.resolveReleaseDateForAlbum(albumId, "Some Album",
                "Some Artist", LocalDate.of(2020, 1, 1), ReleaseDateSource.DISCOGS);

        assertThat(result).isEqualTo(existing);
        then(albumBirthdayRepository).should(never()).save(any());
    }

    @Test
    void shouldResolveReleaseDateFromBandcamp() {
        var albumId = UUID.randomUUID();
        var releaseDate = LocalDate.of(2015, 1, 19);
        var url = "https://anushka.bandcamp.com/album/kisses";
        given(albumBirthdayRepository.findByAlbumId(albumId)).willReturn(Optional.empty());
        given(bandcampLookup.lookUp(url)).willReturn(Optional.of(releaseDate));
        given(albumBirthdayRepository.save(any(AlbumBirthday.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        var result = birthdayService.resolveReleaseDateFromBandcamp(albumId, "Kisses", "Anushka", url);

        assertThat(result).isPresent();
        assertThat(result.get().getReleaseDate()).isEqualTo(releaseDate);
        assertThat(result.get().getReleaseDateSource()).isEqualTo(ReleaseDateSource.BANDCAMP);
        then(albumBirthdayRepository).should().save(any(AlbumBirthday.class));
    }

    @Test
    void shouldSkipBandcampWhenBirthdayAlreadyExists() {
        var albumId = UUID.randomUUID();
        var existing = new AlbumBirthday("Kisses", "Anushka",
                albumId, null, LocalDate.of(2015, 1, 19), ReleaseDateSource.BANDCAMP);
        given(albumBirthdayRepository.findByAlbumId(albumId)).willReturn(Optional.of(existing));

        var result = birthdayService.resolveReleaseDateFromBandcamp(albumId, "Kisses", "Anushka",
                "https://anushka.bandcamp.com/album/kisses");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(existing);
        then(bandcampLookup).shouldHaveNoInteractions();
    }

    @Test
    void shouldReturnEmptyWhenBandcampLookupFails() {
        var albumId = UUID.randomUUID();
        var url = "https://example.bandcamp.com/album/nonexistent";
        given(albumBirthdayRepository.findByAlbumId(albumId)).willReturn(Optional.empty());
        given(bandcampLookup.lookUp(url)).willReturn(Optional.empty());

        var result = birthdayService.resolveReleaseDateFromBandcamp(albumId, "X", "Y", url);

        assertThat(result).isEmpty();
        then(albumBirthdayRepository).should(never()).save(any());
    }

    @Test
    void shouldFindAlbumIdsWithBirthdays() {
        var albumId1 = UUID.randomUUID();
        var albumId2 = UUID.randomUUID();
        var b1 = new AlbumBirthday("A", "B", albumId1, UUID.randomUUID(),
                LocalDate.of(1997, 6, 16), ReleaseDateSource.MUSICBRAINZ);
        var b2 = new AlbumBirthday("C", "D", albumId2, null,
                LocalDate.of(2020, 1, 1), ReleaseDateSource.DISCOGS);
        // Birthday without albumId (legacy)
        var b3 = new AlbumBirthday("E", "F", UUID.randomUUID(), LocalDate.of(2000, 1, 1));
        given(albumBirthdayRepository.findAll()).willReturn(List.of(b1, b2, b3));

        var result = birthdayService.findAlbumIdsWithBirthdays();

        assertThat(result).containsExactlyInAnyOrder(albumId1, albumId2);
    }
}
