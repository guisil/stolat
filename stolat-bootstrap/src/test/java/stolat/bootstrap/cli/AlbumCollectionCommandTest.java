package stolat.bootstrap.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stolat.bootstrap.dao.TrackCollectionDao;
import stolat.bootstrap.filesystem.TrackCollectionCrawler;
import stolat.bootstrap.model.Album;
import stolat.bootstrap.model.Track;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

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

    @Mock
    private List<Track> mockTrackBatch;

    @Captor
    private ArgumentCaptor<Consumer<List<Track>>> trackBatchProcessorCaptor;

    private AlbumCollectionCommand albumCollectionCommand;

    @BeforeEach
    void setUp() {
        albumCollectionCommand = new AlbumCollectionCommand(mockTrackCollectionDao, mockTrackCollectionCrawler);
    }

    @Test
    void shouldUpdateAlbumCollection() {
        albumCollectionCommand.updateAlbumCollectionDatabase(false, false);
        verify(mockTrackCollectionCrawler).processTrackCollection(trackBatchProcessorCaptor.capture());
        trackBatchProcessorCaptor.getValue().accept(mockTrackBatch);
        verify(mockTrackCollectionDao).updateTrackCollection(mockTrackBatch, false);
        verifyNoMoreInteractions(mockTrackCollectionDao);
    }

    @Test
    void shouldRecreateAlbumCollection() {
        albumCollectionCommand.updateAlbumCollectionDatabase(true, false);
        verify(mockTrackCollectionDao).clearTrackCollection();
        verify(mockTrackCollectionCrawler).processTrackCollection(trackBatchProcessorCaptor.capture());
        trackBatchProcessorCaptor.getValue().accept(mockTrackBatch);
        verify(mockTrackCollectionDao).updateTrackCollection(mockTrackBatch, false);
        verifyNoMoreInteractions(mockTrackCollectionDao);
    }

    @Test
    void shouldForceUpdateAlbumCollection() {
        albumCollectionCommand.updateAlbumCollectionDatabase(false, true);
        verify(mockTrackCollectionCrawler).processTrackCollection(trackBatchProcessorCaptor.capture());
        trackBatchProcessorCaptor.getValue().accept(mockTrackBatch);
        verify(mockTrackCollectionDao).updateTrackCollection(mockTrackBatch, true);
        verifyNoMoreInteractions(mockTrackCollectionDao);
    }

    @Test
    void shouldUpdateAlbumCollectionFromFolder() {
        albumCollectionCommand.updateAlbumCollectionDatabase(false, SOME_PATH, false);
        verify(mockTrackCollectionCrawler).processTrackCollection(eq(SOME_PATH), trackBatchProcessorCaptor.capture());
        trackBatchProcessorCaptor.getValue().accept(mockTrackBatch);
        verify(mockTrackCollectionDao).updateTrackCollection(mockTrackBatch, false);
        verifyNoMoreInteractions(mockTrackCollectionDao);
    }

    @Test
    void shouldRecreateAlbumCollectionFromFolder() {
        albumCollectionCommand.updateAlbumCollectionDatabase(true, SOME_PATH, false);
        verify(mockTrackCollectionDao).clearTrackCollection();
        verify(mockTrackCollectionCrawler).processTrackCollection(eq(SOME_PATH), trackBatchProcessorCaptor.capture());
        trackBatchProcessorCaptor.getValue().accept(mockTrackBatch);
        verify(mockTrackCollectionDao).updateTrackCollection(mockTrackBatch, false);
        verifyNoMoreInteractions(mockTrackCollectionDao);
    }
}