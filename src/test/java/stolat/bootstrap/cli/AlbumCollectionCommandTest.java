package stolat.bootstrap.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stolat.bootstrap.dao.TrackCollectionDao;
import stolat.bootstrap.filesystem.TrackCollectionCrawler;
import stolat.bootstrap.model.Album;
import stolat.bootstrap.model.Track;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlbumCollectionCommandTest {

    private static final Path SOME_PATH = Path.of(File.separator, "some", "path");
    private static final Album ALBUM =
            new Album(UUID.randomUUID().toString(), "First Album", UUID.randomUUID().toString(), "Some Artist");
    private static final Set<Track> TRACK_COLLECTION =
            Set.of(
                    new Track(UUID.randomUUID().toString(), Integer.toString(1), "first track", 123,
                            Path.of("some artist", "first album", "first track.flac").toString(), ALBUM),
                    new Track(UUID.randomUUID().toString(), Integer.toString(2), "second track", 321,
                            Path.of("some artist", "first album", "second track.flac").toString(), ALBUM));


    @Mock
    private TrackCollectionDao mockTrackCollectionDao;

    @Mock
    private TrackCollectionCrawler mockTrackCollectionCrawler;

    @InjectMocks
    private AlbumCollectionCommand albumCollectionCommand;

    @Test
    void shouldUpdateAlbumCollection() {
        when(mockTrackCollectionCrawler.fetchTrackCollection()).thenReturn(TRACK_COLLECTION);
        albumCollectionCommand.updateAlbumCollectionDatabase(false, false);
        verify(mockTrackCollectionDao).populateTrackCollection(TRACK_COLLECTION, false);
        verifyNoMoreInteractions(mockTrackCollectionDao);
    }

    @Test
    void shouldRecreateAlbumCollection() {
        when(mockTrackCollectionCrawler.fetchTrackCollection()).thenReturn(TRACK_COLLECTION);
        albumCollectionCommand.updateAlbumCollectionDatabase(true, false);
        verify(mockTrackCollectionDao).clearTrackCollection();
        verify(mockTrackCollectionDao).populateTrackCollection(TRACK_COLLECTION, false);
    }

    @Test
    void shouldForceUpdateAlbumCollection() {
        when(mockTrackCollectionCrawler.fetchTrackCollection()).thenReturn(TRACK_COLLECTION);
        albumCollectionCommand.updateAlbumCollectionDatabase(false, true);
        verify(mockTrackCollectionDao).populateTrackCollection(TRACK_COLLECTION, true);
        verifyNoMoreInteractions(mockTrackCollectionDao);
    }

    @Test
    void shouldUpdateAlbumCollectionFromFolder() {
        when(mockTrackCollectionCrawler.fetchTrackCollection(SOME_PATH)).thenReturn(TRACK_COLLECTION);
        albumCollectionCommand.updateAlbumCollectionDatabase(false, SOME_PATH, false);
        verify(mockTrackCollectionDao).populateTrackCollection(TRACK_COLLECTION, false);
        verifyNoMoreInteractions(mockTrackCollectionDao);
    }

    @Test
    void shouldRecreateAlbumCollectionFromFolder() {
        when(mockTrackCollectionCrawler.fetchTrackCollection(SOME_PATH)).thenReturn(TRACK_COLLECTION);
        albumCollectionCommand.updateAlbumCollectionDatabase(true, SOME_PATH, false);
        verify(mockTrackCollectionDao).clearTrackCollection();
        verify(mockTrackCollectionDao).populateTrackCollection(TRACK_COLLECTION, false);
        verifyNoMoreInteractions(mockTrackCollectionDao);
    }
}