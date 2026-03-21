package app.stolat.birthday.internal;

import app.stolat.birthday.BirthdayService;
import app.stolat.collection.AlbumDiscoveredEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AlbumDiscoveredListenerTest {

    @Mock
    private BirthdayService birthdayService;

    @InjectMocks
    private AlbumDiscoveredListener listener;

    @Test
    void shouldResolveReleaseDateWhenAlbumDiscovered() {
        var albumId = UUID.randomUUID();
        var musicBrainzId = UUID.randomUUID();
        var event = new AlbumDiscoveredEvent(albumId, "OK Computer", "Radiohead", musicBrainzId);

        listener.onAlbumDiscovered(event);

        then(birthdayService).should().resolveReleaseDate("OK Computer", "Radiohead", musicBrainzId);
    }
}
