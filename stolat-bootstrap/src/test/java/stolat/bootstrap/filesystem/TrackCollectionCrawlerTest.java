package stolat.bootstrap.filesystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stolat.bootstrap.tags.TagInfoReader;
import stolat.model.Album;
import stolat.model.Track;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class TrackCollectionCrawlerTest {

    private static final String COLLECTION_ROOT_FOLDER = "collection";
    private static final String SOME_ARTIST_FOLDER = "Some Artist";
    private static final String FIRST_ALBUM_FOLDER = "First Album";
    private static final String FIRST_ALBUM_FIRST_TRACK = "Some Track.flac";
    private static final String FIRST_ALBUM_SECOND_TRACK = "Some Other Track.flac";
    private static final String FIRST_ALBUM_COVER = "cover.jpg";
    private static final String SOME_OTHER_ARTIST_FOLDER = "Some other Artist";
    private static final String SECOND_ALBUM_FOLDER = "Second Album";
    private static final String SECOND_ALBUM_FIRST_TRACK = "Something Else.flac";
    private static final String SECOND_ALBUM_SECOND_TRACK = "yetanothertrack.flac";
    private static final String SECOND_ALBUM_OUT_OF_PLACE_TRACK = "should be somewhere else.mp3";
    private static final String SECOND_ALBUM_PDF = "something.pdf";
    private static final String THIRD_ALBUM_FOLDER = "Third Album";
    private static final String THIRD_ALBUM_FIRST_TRACK = "What is this.flac";
    private static final String THIRD_ALBUM_SECOND_TRACK = "And what about this.flac";
    private static final String THIRD_ALBUM_COVER = "another_cover.jpg";
    private static final String EMPTY_ALBUM_FOLDER = "Empty Album";

    @TempDir
    File tempDir;

    @Mock
    private FileSystemProperties mockFileSystemProperties;

    @Mock
    private TagInfoReader mockTagInfoReader;

    private File someOtherArtistFolder;
    private File firstAlbumFirstTrackFile;
    private File firstAlbumSecondTrackFile;
    private File secondAlbumFirstTrackFile;
    private File secondAlbumSecondTrackFile;
    private File thirdAlbumFirstTrackFile;
    private File thirdAlbumSecondTrackFile;

    private Track firstAlbumFirstTrack;
    private Track firstAlbumSecondTrack;
    private Track secondAlbumFirstTrack;
    private Track secondAlbumSecondTrack;
    private Track thirdAlbumFirstTrack;
    private Track thirdAlbumSecondTrack;

    private TrackCollectionCrawler trackCollectionCrawler;

    @BeforeEach
    void setUp() throws IOException {
        trackCollectionCrawler =
                new TrackCollectionCrawler(
                        mockFileSystemProperties, mockTagInfoReader);
        initialiseFilesystem();
        initialiseTracks();
    }

    private void initialiseFilesystem() throws IOException {
        final File collectionRootFolder = new File(tempDir, COLLECTION_ROOT_FOLDER);

        final File someArtistFolder = new File(collectionRootFolder, SOME_ARTIST_FOLDER);
        final File firstAlbumFolder = new File(someArtistFolder, FIRST_ALBUM_FOLDER);
        firstAlbumFolder.mkdirs();
        firstAlbumFirstTrackFile = new File(firstAlbumFolder, FIRST_ALBUM_FIRST_TRACK);
        firstAlbumFirstTrackFile.createNewFile();
        firstAlbumSecondTrackFile = new File(firstAlbumFolder, FIRST_ALBUM_SECOND_TRACK);
        firstAlbumSecondTrackFile.createNewFile();
        new File(firstAlbumFolder, FIRST_ALBUM_COVER).createNewFile();
        someOtherArtistFolder = new File(collectionRootFolder, SOME_OTHER_ARTIST_FOLDER);
        final File secondAlbumFolder = new File(someOtherArtistFolder, SECOND_ALBUM_FOLDER);
        secondAlbumFolder.mkdirs();
        secondAlbumFirstTrackFile = new File(secondAlbumFolder, SECOND_ALBUM_FIRST_TRACK);
        secondAlbumFirstTrackFile.createNewFile();
        secondAlbumSecondTrackFile = new File(secondAlbumFolder, SECOND_ALBUM_SECOND_TRACK);
        secondAlbumSecondTrackFile.createNewFile();
        new File(secondAlbumFolder, SECOND_ALBUM_OUT_OF_PLACE_TRACK).createNewFile();
        new File(secondAlbumFolder, SECOND_ALBUM_PDF).createNewFile();
        final File thirdAlbumFolder = new File(someArtistFolder, THIRD_ALBUM_FOLDER);
        thirdAlbumFolder.mkdirs();
        thirdAlbumFirstTrackFile = new File(thirdAlbumFolder, THIRD_ALBUM_FIRST_TRACK);
        thirdAlbumFirstTrackFile.createNewFile();
        thirdAlbumSecondTrackFile = new File(thirdAlbumFolder, THIRD_ALBUM_SECOND_TRACK);
        thirdAlbumSecondTrackFile.createNewFile();
        new File(thirdAlbumFolder, THIRD_ALBUM_COVER).createNewFile();
        final File emptyAlbumFolder = new File(someOtherArtistFolder, EMPTY_ALBUM_FOLDER);
        emptyAlbumFolder.mkdirs();

        lenient().when(mockFileSystemProperties.getAlbumCollectionPath()).thenReturn(collectionRootFolder.getAbsolutePath());
        lenient().when(mockFileSystemProperties.getAlbumCollectionBatchSize()).thenReturn(3);
        lenient().when(mockFileSystemProperties.getMusicFileExtensions()).thenReturn(List.of("flac"));
    }

    private void initialiseTracks() {
        final String firstArtistMbid = UUID.randomUUID().toString();
		final String firstArtistName = "Some Artist";
		final Album firstAlbum = new Album(
                UUID.randomUUID().toString(), "First Album",
                List.of(firstArtistMbid), List.of(firstArtistName));
        firstAlbumFirstTrack = new Track(
                UUID.randomUUID().toString(), "1", "1", "Some Track",
                123, Path.of(SOME_ARTIST_FOLDER, FIRST_ALBUM_FOLDER, FIRST_ALBUM_FIRST_TRACK).toString(), firstAlbum);
        firstAlbumSecondTrack = new Track(
                UUID.randomUUID().toString(), "1", "2", "Some Other Track",
                132, Path.of(SOME_ARTIST_FOLDER, FIRST_ALBUM_FOLDER, FIRST_ALBUM_SECOND_TRACK).toString(), firstAlbum);

        final String secondArtistMbid = UUID.randomUUID().toString();
        final String secondArtistName = "Some other Artist";
        final Album secondAlbum = new Album(
                UUID.randomUUID().toString(), "Second Album",
                List.of(secondArtistMbid), List.of(secondArtistName));
        secondAlbumFirstTrack = new Track(
                UUID.randomUUID().toString(), "1", "1", "Something Else",
                111, Path.of(SOME_OTHER_ARTIST_FOLDER, SECOND_ALBUM_FOLDER, SECOND_ALBUM_FIRST_TRACK).toString(), secondAlbum);
        secondAlbumSecondTrack = new Track(
                UUID.randomUUID().toString(), "1", "2", "Yet Another Track",
                222, Path.of(SOME_OTHER_ARTIST_FOLDER, SECOND_ALBUM_FOLDER, SECOND_ALBUM_SECOND_TRACK).toString(), secondAlbum);
        
        final Album thirdAlbum = new Album(
        		UUID.randomUUID().toString(), "Third Album",
        		List.of(firstArtistMbid, secondArtistMbid), List.of(firstArtistName, secondArtistName));
        thirdAlbumFirstTrack = new Track(
        		UUID.randomUUID().toString(), "1", "1", "What is this",
        		121, Path.of(SOME_ARTIST_FOLDER, THIRD_ALBUM_FOLDER, THIRD_ALBUM_FIRST_TRACK).toString(), thirdAlbum);
        thirdAlbumSecondTrack = new Track(
        		UUID.randomUUID().toString(), "1", "2", "And what about this",
        		212, Path.of(SOME_ARTIST_FOLDER, THIRD_ALBUM_FOLDER, THIRD_ALBUM_SECOND_TRACK).toString(), thirdAlbum);

        lenient().when(mockTagInfoReader.getTrackBatchInfo(anyList())).thenCallRealMethod();
        lenient().when(mockTagInfoReader.getTrackInfo(firstAlbumFirstTrackFile)).thenReturn(Optional.of(firstAlbumFirstTrack));
        lenient().when(mockTagInfoReader.getTrackInfo(firstAlbumSecondTrackFile)).thenReturn(Optional.of(firstAlbumSecondTrack));
        lenient().when(mockTagInfoReader.getTrackInfo(secondAlbumFirstTrackFile)).thenReturn(Optional.of(secondAlbumFirstTrack));
        lenient().when(mockTagInfoReader.getTrackInfo(secondAlbumSecondTrackFile)).thenReturn(Optional.of(secondAlbumSecondTrack));
        lenient().when(mockTagInfoReader.getTrackInfo(thirdAlbumFirstTrackFile)).thenReturn(Optional.of(thirdAlbumFirstTrack));
        lenient().when(mockTagInfoReader.getTrackInfo(thirdAlbumSecondTrackFile)).thenReturn(Optional.of(thirdAlbumSecondTrack));
    }

    @Test
    void shouldProcessTrackCollectionFromConfiguredPath() {
        final Set<Track> expected =
                Set.of(firstAlbumFirstTrack, firstAlbumSecondTrack, secondAlbumFirstTrack, secondAlbumSecondTrack, thirdAlbumFirstTrack, thirdAlbumSecondTrack);
        final Set<Track> processed = new HashSet<>();
        trackCollectionCrawler.processTrackCollection(processed::addAll);

        assertEquals(expected, processed);
    }

    @Test
    void shouldProcessTrackCollectionFromGivenPath() {
        final Set<Track> expected =
                Set.of(secondAlbumFirstTrack, secondAlbumSecondTrack);
        final Set<Track> processed = new HashSet<>();
        trackCollectionCrawler.processTrackCollection(someOtherArtistFolder.toPath(), processed::addAll);
        assertEquals(expected, processed);
    }

    @Test
    void shouldNotProcessTrackCollectionWhenRootFolderDoesNotExist() {
        final Set<Track> processed = new HashSet<>();
        trackCollectionCrawler.processTrackCollection(Path.of("/some/non/existing/path"), processed::addAll);
        assertTrue(processed.isEmpty());
    }
}