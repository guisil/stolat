package app.stolat.birthday;

import app.stolat.birthday.internal.AlbumBirthdayRepository;
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

    @InjectMocks
    private BirthdayService birthdayService;

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
        var musicBrainzId = UUID.randomUUID();
        var releaseDate = LocalDate.of(1997, 6, 16);
        given(albumBirthdayRepository.findByMusicBrainzId(musicBrainzId)).willReturn(Optional.empty());
        given(releaseDateLookup.lookUp(musicBrainzId)).willReturn(Optional.of(releaseDate));
        given(albumBirthdayRepository.save(any(AlbumBirthday.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        var result = birthdayService.resolveReleaseDate("OK Computer", "Radiohead", musicBrainzId);

        assertThat(result).isPresent();
        assertThat(result.get().getReleaseDate()).isEqualTo(releaseDate);
        then(albumBirthdayRepository).should().save(any(AlbumBirthday.class));
    }

    @Test
    void shouldReturnEmptyWhenReleaseDateNotFound() {
        var musicBrainzId = UUID.randomUUID();
        given(albumBirthdayRepository.findByMusicBrainzId(musicBrainzId)).willReturn(Optional.empty());
        given(releaseDateLookup.lookUp(musicBrainzId)).willReturn(Optional.empty());

        var result = birthdayService.resolveReleaseDate("Unknown Album", "Unknown Artist", musicBrainzId);

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

        var result = birthdayService.resolveReleaseDate("OK Computer", "Radiohead", musicBrainzId);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(existing);
        then(releaseDateLookup).shouldHaveNoInteractions();
    }

    @Test
    void shouldSaveDirectReleaseDateAndReturn() {
        var musicBrainzId = UUID.randomUUID();
        var releaseDate = LocalDate.of(1997, 6, 16);
        given(albumBirthdayRepository.findByMusicBrainzId(musicBrainzId)).willReturn(Optional.empty());
        given(albumBirthdayRepository.save(any(AlbumBirthday.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        var result = birthdayService.resolveReleaseDateDirect("OK Computer", "Radiohead", musicBrainzId, releaseDate);

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

        var result = birthdayService.resolveReleaseDateDirect("OK Computer", "Radiohead",
                musicBrainzId, LocalDate.of(1997, 6, 16));

        assertThat(result).isEqualTo(existing);
        then(albumBirthdayRepository).should(never()).save(any());
    }
}
