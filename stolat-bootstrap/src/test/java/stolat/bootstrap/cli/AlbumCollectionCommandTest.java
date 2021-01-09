package stolat.bootstrap.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stolat.bootstrap.filesystem.TrackCollectionCrawler;
import stolat.dao.TrackCollectionDao;
import stolat.model.Track;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlbumCollectionCommandTest {

	private static final Path SOME_PATH = Path.of(File.separator, "some", "path");

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
		albumCollectionCommand.updateAlbumCollectionDatabase(false);
		verify(mockTrackCollectionCrawler).processTrackCollection(trackBatchProcessorCaptor.capture());
		trackBatchProcessorCaptor.getValue().accept(mockTrackBatch);
		verify(mockTrackCollectionDao).updateTrackCollection(mockTrackBatch);
		verifyNoMoreInteractions(mockTrackCollectionDao);
	}

	@Test
	void shouldRecreateAlbumCollection() {
		albumCollectionCommand.updateAlbumCollectionDatabase(true);
		verify(mockTrackCollectionDao).clearTrackCollection();
		verify(mockTrackCollectionCrawler).processTrackCollection(trackBatchProcessorCaptor.capture());
		trackBatchProcessorCaptor.getValue().accept(mockTrackBatch);
		verify(mockTrackCollectionDao).updateTrackCollection(mockTrackBatch);
		verifyNoMoreInteractions(mockTrackCollectionDao);
	}

	@Test
	void shouldUpdateAlbumCollectionFromFolder() {
		albumCollectionCommand.updateAlbumCollectionDatabase(false, SOME_PATH);
		verify(mockTrackCollectionCrawler).processTrackCollection(eq(SOME_PATH), trackBatchProcessorCaptor.capture());
		trackBatchProcessorCaptor.getValue().accept(mockTrackBatch);
		verify(mockTrackCollectionDao).updateTrackCollection(mockTrackBatch);
		verifyNoMoreInteractions(mockTrackCollectionDao);
	}

	@Test
	void shouldRecreateAlbumCollectionFromFolder() {
		albumCollectionCommand.updateAlbumCollectionDatabase(true, SOME_PATH);
		verify(mockTrackCollectionDao).clearTrackCollection();
		verify(mockTrackCollectionCrawler).processTrackCollection(eq(SOME_PATH), trackBatchProcessorCaptor.capture());
		trackBatchProcessorCaptor.getValue().accept(mockTrackBatch);
		verify(mockTrackCollectionDao).updateTrackCollection(mockTrackBatch);
		verifyNoMoreInteractions(mockTrackCollectionDao);
	}
}