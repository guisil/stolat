package app.stolat.birthday.internal;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;

import app.stolat.birthday.AlbumBirthday;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class LastFmSyncServiceTest {

    @Mock
    private AlbumBirthdayRepository albumBirthdayRepository;

    @Mock
    private LastFmClient lastFmClient;

    @InjectMocks
    private LastFmSyncService lastFmSyncService;

    @Test
    void shouldUpdatePlayCountWhenLastFmReturnsData() {
        var birthday = new AlbumBirthday("OK Computer", "Radiohead",
                UUID.randomUUID(), LocalDate.of(1997, 6, 16));
        given(albumBirthdayRepository.findAll()).willReturn(List.of(birthday));
        given(lastFmClient.fetchAlbumPlayCount("Radiohead", "OK Computer"))
                .willReturn(OptionalInt.of(142));

        var updated = lastFmSyncService.syncAllPlayCounts();

        assertThat(updated).isEqualTo(1);
        assertThat(birthday.getPlayCount()).isEqualTo(142);
        then(albumBirthdayRepository).should().save(birthday);
    }

    @Test
    void shouldSkipWhenPlayCountRecentlyUpdated() {
        var birthday = new AlbumBirthday("OK Computer", "Radiohead",
                UUID.randomUUID(), LocalDate.of(1997, 6, 16));
        birthday.updatePlayCount(100); // sets playCountUpdatedAt to now
        given(albumBirthdayRepository.findAll()).willReturn(List.of(birthday));

        var updated = lastFmSyncService.syncAllPlayCounts();

        assertThat(updated).isEqualTo(0);
        then(lastFmClient).shouldHaveNoInteractions();
        then(albumBirthdayRepository).should(never()).save(birthday);
    }

    @Test
    void shouldNotSaveWhenLastFmReturnsEmpty() {
        var birthday = new AlbumBirthday("Unknown Album", "Unknown Artist",
                UUID.randomUUID(), LocalDate.of(2020, 1, 1));
        given(albumBirthdayRepository.findAll()).willReturn(List.of(birthday));
        given(lastFmClient.fetchAlbumPlayCount("Unknown Artist", "Unknown Album"))
                .willReturn(OptionalInt.empty());

        var updated = lastFmSyncService.syncAllPlayCounts();

        assertThat(updated).isEqualTo(0);
        then(albumBirthdayRepository).should(never()).save(birthday);
    }

    @Test
    void shouldReturnZeroWhenNoBirthdaysExist() {
        given(albumBirthdayRepository.findAll()).willReturn(List.of());

        var updated = lastFmSyncService.syncAllPlayCounts();

        assertThat(updated).isEqualTo(0);
        then(lastFmClient).shouldHaveNoInteractions();
    }

    @Test
    void shouldUpdateMultipleBirthdays() {
        var birthday1 = new AlbumBirthday("OK Computer", "Radiohead",
                UUID.randomUUID(), LocalDate.of(1997, 6, 16));
        var birthday2 = new AlbumBirthday("Kid A", "Radiohead",
                UUID.randomUUID(), LocalDate.of(2000, 10, 2));
        given(albumBirthdayRepository.findAll()).willReturn(List.of(birthday1, birthday2));
        given(lastFmClient.fetchAlbumPlayCount("Radiohead", "OK Computer"))
                .willReturn(OptionalInt.of(142));
        given(lastFmClient.fetchAlbumPlayCount("Radiohead", "Kid A"))
                .willReturn(OptionalInt.of(88));

        var updated = lastFmSyncService.syncAllPlayCounts();

        assertThat(updated).isEqualTo(2);
        assertThat(birthday1.getPlayCount()).isEqualTo(142);
        assertThat(birthday2.getPlayCount()).isEqualTo(88);
    }
}
