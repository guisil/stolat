package app.stolat.birthday.internal;

import app.stolat.birthday.AlbumBirthday;
import app.stolat.birthday.BirthdayService;
import app.stolat.collection.AlbumDiscoveredEvent;
import app.stolat.collection.CollectionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AlbumDiscoveredListenerTest {

    @Mock
    private BirthdayService birthdayService;

    @Mock
    private CollectionService collectionService;

    @InjectMocks
    private AlbumDiscoveredListener listener;

    @Test
    void shouldResolveReleaseDateAndUpdateAlbumWhenAlbumDiscovered() {
        var albumId = UUID.randomUUID();
        var musicBrainzId = UUID.randomUUID();
        var releaseDate = LocalDate.of(1997, 6, 16);
        var event = new AlbumDiscoveredEvent(albumId, "OK Computer", "Radiohead", musicBrainzId);
        var birthday = new AlbumBirthday("OK Computer", "Radiohead", musicBrainzId, releaseDate);
        given(birthdayService.resolveReleaseDate("OK Computer", "Radiohead", musicBrainzId))
                .willReturn(Optional.of(birthday));

        listener.onAlbumDiscovered(event);

        then(birthdayService).should().resolveReleaseDate("OK Computer", "Radiohead", musicBrainzId);
        then(collectionService).should().updateAlbumReleaseDate(musicBrainzId, releaseDate);
    }

    @Test
    void shouldNotUpdateAlbumWhenReleaseDateNotFound() {
        var albumId = UUID.randomUUID();
        var musicBrainzId = UUID.randomUUID();
        var event = new AlbumDiscoveredEvent(albumId, "Unknown Album", "Unknown Artist", musicBrainzId);
        given(birthdayService.resolveReleaseDate("Unknown Album", "Unknown Artist", musicBrainzId))
                .willReturn(Optional.empty());

        listener.onAlbumDiscovered(event);

        then(birthdayService).should().resolveReleaseDate("Unknown Album", "Unknown Artist", musicBrainzId);
        then(collectionService).shouldHaveNoInteractions();
    }
}
