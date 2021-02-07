package stolat.bootstrap.tags;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stolat.bootstrap.filesystem.FileSystemProperties;
import stolat.model.Album;
import stolat.model.Track;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class JAudioTaggerTagInfoReaderTest {

    private static final String COLLECTION_ROOT_PATH = Path.of(File.separator, "some", "path", "music").toString();
    private static final String FIRST_AUDIO_FILE_RELATIVE_PATH = Path.of("The Awesome Musicians", "That Great Album", "Great to Listen.flac").toString();
    private static final String SECOND_AUDIO_FILE_RELATIVE_PATH = Path.of("The Awesome Musicians", "That Great Album", "Not so Great to Listen.flac").toString();
    private static final String THIRD_AUDIO_FILE_RELATIVE_PATH = Path.of("The Awesome Musicians and The Mediocre Musicians", "That Mediocre Album", "Definitely not worth it.flac").toString();
    private static final Instant FIXED_INSTANT = Instant.now();

    private static final UUID ALBUM_MBID = UUID.randomUUID();
    private static final String ALBUM_NAME = "That Great Album";
    private static final UUID ANOTHER_ALBUM_MBID = UUID.randomUUID();
    private static final String ANOTHER_ALBUM_NAME = "That Mediocre Album";
    private static final UUID ARTIST_MBID = UUID.randomUUID();
    private static final String ARTIST_NAME = "The Awesome Musicians";
    private static final UUID ANOTHER_ARTIST_MBID = UUID.randomUUID();
    private static final String ANOTHER_ARTIST_NAME = "The Mediocre Musicians";
    private static final UUID FIRST_TRACK_MBID = UUID.randomUUID();
    private static final int DISC_NUMBER = 1;
    private static final int FIRST_TRACK_NUMBER = 7;
    private static final String FIRST_TRACK_NAME = "Great to Listen";
    private static final int FIRST_TRACK_LENGTH = 234;
    private static final UUID SECOND_TRACK_MBID = UUID.randomUUID();
    private static final int SECOND_TRACK_NUMBER = 9;
    private static final String SECOND_TRACK_NAME = "Not so Great to Listen";
    private static final int SECOND_TRACK_LENGTH = 111;
    private static final UUID THIRD_TRACK_MBID = UUID.randomUUID();
    private static final int THIRD_TRACK_NUMBER = 3;
    private static final String THIRD_TRACK_NAME = "Definitely not worth it";
    private static final int THIRD_TRACK_LENGTH = 333;

    @Mock
    private FileSystemProperties mockFileSystemProperties;

    @Mock
    private AudioFile mockFirstAudioFile;

    @Mock
    private AudioFile mockSecondAudioFile;
    
    @Mock
    private AudioFile mockThirdAudioFile;

    @Mock
    private Tag mockFirstTag;

    @Mock
    private Tag mockSecondTag;
    
    @Mock
    private Tag mockThirdTag;

    @Mock
    private AudioHeader mockFirstAudioHeader;

    @Mock
    private AudioHeader mockSecondAudioHeader;
    
    @Mock
    private AudioHeader mockThirdAudioHeader;

    @Mock
    private JAudioTaggerAudioFileProvider mockAudioFileProvider;

    private File firstAudioFile;
    private File secondAudioFile;
    private File thirdAudioFile;

    private JAudioTaggerTagInfoReader tagInfoReader;

    @BeforeEach
    void setUp() throws ReadOnlyFileException, IOException, TagException, InvalidAudioFrameException, CannotReadException {

        tagInfoReader = new JAudioTaggerTagInfoReader(mockFileSystemProperties, mockAudioFileProvider);

        firstAudioFile = new File(COLLECTION_ROOT_PATH, FIRST_AUDIO_FILE_RELATIVE_PATH);
        secondAudioFile = new File(COLLECTION_ROOT_PATH, SECOND_AUDIO_FILE_RELATIVE_PATH);
        thirdAudioFile = new File(COLLECTION_ROOT_PATH, THIRD_AUDIO_FILE_RELATIVE_PATH);

        lenient().when(mockAudioFileProvider.getAudioFile(firstAudioFile)).thenReturn(mockFirstAudioFile);
        lenient().when(mockAudioFileProvider.getAudioFile(secondAudioFile)).thenReturn(mockSecondAudioFile);
        lenient().when(mockAudioFileProvider.getAudioFile(thirdAudioFile)).thenReturn(mockThirdAudioFile);
        lenient().when(mockFirstAudioFile.getTag()).thenReturn(mockFirstTag);
        lenient().when(mockSecondAudioFile.getTag()).thenReturn(mockSecondTag);
        lenient().when(mockThirdAudioFile.getTag()).thenReturn(mockThirdTag);
        lenient().when(mockFirstAudioFile.getAudioHeader()).thenReturn(mockFirstAudioHeader);
        lenient().when(mockSecondAudioFile.getAudioHeader()).thenReturn(mockSecondAudioHeader);
        lenient().when(mockThirdAudioFile.getAudioHeader()).thenReturn(mockThirdAudioHeader);

        lenient().when(mockFileSystemProperties.getAlbumCollectionPath()).thenReturn(COLLECTION_ROOT_PATH);
    }

    private void initialiseTagMocks(Tag mockTag, AudioHeader mockAudioHeader,
                                    String trackMbidTag, String discNumberTag, String trackNumberTag, String trackNameTag,
                                    int trackLength, String albumMbidTag, String albumNameTag,
                                    List<String> artistMbidTags, List<String> artistNameTags) {
        lenient().when(mockTag.getFirst(FieldKey.MUSICBRAINZ_TRACK_ID)).thenReturn(trackMbidTag);
        lenient().when(mockTag.getFirst(FieldKey.DISC_NO)).thenReturn(discNumberTag);
        lenient().when(mockTag.getFirst(FieldKey.TRACK)).thenReturn(trackNumberTag);
        lenient().when(mockTag.getFirst(FieldKey.TITLE)).thenReturn(trackNameTag);
        lenient().when(mockAudioHeader.getTrackLength()).thenReturn(trackLength);
        lenient().when(mockTag.getFirst(FieldKey.MUSICBRAINZ_RELEASE_GROUP_ID)).thenReturn(albumMbidTag);
        lenient().when(mockTag.getFirst(FieldKey.ALBUM)).thenReturn(albumNameTag);
        lenient().when(mockTag.getAll(FieldKey.MUSICBRAINZ_ARTISTID)).thenReturn(artistMbidTags);
        lenient().when(mockTag.getAll(FieldKey.ARTISTS)).thenReturn(artistNameTags);
    }

    @Test
    void shouldGetTrackWithCorrectTags() {
        final Album album = new Album(ALBUM_MBID.toString(), ALBUM_NAME, List.of(ARTIST_MBID.toString()), List.of(ARTIST_NAME));
        final Track expected = new Track(
                FIRST_TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(FIRST_TRACK_NUMBER), FIRST_TRACK_NAME,
                FIRST_TRACK_LENGTH, FIRST_AUDIO_FILE_RELATIVE_PATH, album);
        initialiseTagMocks(mockFirstTag, mockFirstAudioHeader,
                FIRST_TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(FIRST_TRACK_NUMBER), FIRST_TRACK_NAME,
                FIRST_TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, List.of(ARTIST_MBID.toString()), List.of(ARTIST_NAME));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(firstAudioFile);

        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
    }
    
    @Test
    void shouldGetMultipleArtistTrackWithCorrectTags() {
        final Album album = new Album(ANOTHER_ALBUM_MBID.toString(), ANOTHER_ALBUM_NAME, List.of(ARTIST_MBID.toString(), ANOTHER_ARTIST_MBID.toString()), List.of(ARTIST_NAME, ANOTHER_ARTIST_NAME));
        final Track expected = new Track(
                THIRD_TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(THIRD_TRACK_NUMBER), THIRD_TRACK_NAME,
                THIRD_TRACK_LENGTH, THIRD_AUDIO_FILE_RELATIVE_PATH, album);
        initialiseTagMocks(mockThirdTag, mockThirdAudioHeader,
                THIRD_TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(THIRD_TRACK_NUMBER), THIRD_TRACK_NAME,
                THIRD_TRACK_LENGTH, ANOTHER_ALBUM_MBID.toString(), ANOTHER_ALBUM_NAME, List.of(ARTIST_MBID.toString(), ANOTHER_ARTIST_MBID.toString()), List.of(ARTIST_NAME, ANOTHER_ARTIST_NAME));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(thirdAudioFile);

        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
    }

    @Test
    void shouldGetTrackBatchWithCorrectTags() {
        final Album album = new Album(ALBUM_MBID.toString(), ALBUM_NAME, List.of(ARTIST_MBID.toString()), List.of(ARTIST_NAME));
        final Track firstExpected = new Track(
                FIRST_TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(FIRST_TRACK_NUMBER), FIRST_TRACK_NAME,
                FIRST_TRACK_LENGTH, FIRST_AUDIO_FILE_RELATIVE_PATH, album);
        final Track secondExpected = new Track(
                SECOND_TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(SECOND_TRACK_NUMBER), SECOND_TRACK_NAME,
                SECOND_TRACK_LENGTH, SECOND_AUDIO_FILE_RELATIVE_PATH, album);
        initialiseTagMocks(mockFirstTag, mockFirstAudioHeader,
                FIRST_TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(FIRST_TRACK_NUMBER), FIRST_TRACK_NAME,
                FIRST_TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, List.of(ARTIST_MBID.toString()), List.of(ARTIST_NAME));
        initialiseTagMocks(mockSecondTag, mockSecondAudioHeader,
                SECOND_TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(SECOND_TRACK_NUMBER), SECOND_TRACK_NAME,
                SECOND_TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, List.of(ARTIST_MBID.toString()), List.of(ARTIST_NAME));

        final List<Track> actual = tagInfoReader.getTrackBatchInfo(List.of(firstAudioFile, secondAudioFile));

        assertEquals(2, actual.size());
        assertTrue(actual.contains(firstExpected));
        assertTrue(actual.contains(secondExpected));
    }

    @Test
    void shouldNotGetTrackWhenTrackMbidNull() {
        initialiseTagMocks(mockFirstTag, mockFirstAudioHeader,
                null, Integer.toString(DISC_NUMBER), Integer.toString(FIRST_TRACK_NUMBER), FIRST_TRACK_NAME,
                FIRST_TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, List.of(ARTIST_MBID.toString()), List.of(ARTIST_NAME));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(firstAudioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenTrackMbidEmpty() {
        initialiseTagMocks(mockFirstTag, mockFirstAudioHeader,
                "", Integer.toString(DISC_NUMBER), Integer.toString(FIRST_TRACK_NUMBER), FIRST_TRACK_NAME,
                FIRST_TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, List.of(ARTIST_MBID.toString()), List.of(ARTIST_NAME));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(firstAudioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenTrackMbidInvalid() {
        initialiseTagMocks(mockFirstTag, mockFirstAudioHeader,
                "1234", Integer.toString(DISC_NUMBER), Integer.toString(FIRST_TRACK_NUMBER), FIRST_TRACK_NAME,
                FIRST_TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, List.of(ARTIST_MBID.toString()), List.of(ARTIST_NAME));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(firstAudioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldStillGetTrackWithCorrectTagsWhenDiscNumberNull() {
        final Album album = new Album(ALBUM_MBID.toString(), ALBUM_NAME, List.of(ARTIST_MBID.toString()), List.of(ARTIST_NAME));
        final Track expected = new Track(
                FIRST_TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(FIRST_TRACK_NUMBER), FIRST_TRACK_NAME,
                FIRST_TRACK_LENGTH, FIRST_AUDIO_FILE_RELATIVE_PATH, album);
        initialiseTagMocks(mockFirstTag, mockFirstAudioHeader,
                FIRST_TRACK_MBID.toString(), null, Integer.toString(FIRST_TRACK_NUMBER), FIRST_TRACK_NAME,
                FIRST_TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, List.of(ARTIST_MBID.toString()), List.of(ARTIST_NAME));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(firstAudioFile);

        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
    }

    @Test
    void shouldStillGetTrackWithCorrectTagsWhenDiscNumberEmpty() {
        final Album album = new Album(ALBUM_MBID.toString(), ALBUM_NAME, List.of(ARTIST_MBID.toString()), List.of(ARTIST_NAME));
        final Track expected = new Track(
                FIRST_TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(FIRST_TRACK_NUMBER), FIRST_TRACK_NAME,
                FIRST_TRACK_LENGTH, FIRST_AUDIO_FILE_RELATIVE_PATH, album);
        initialiseTagMocks(mockFirstTag, mockFirstAudioHeader,
                FIRST_TRACK_MBID.toString(), "", Integer.toString(FIRST_TRACK_NUMBER), FIRST_TRACK_NAME,
                FIRST_TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, List.of(ARTIST_MBID.toString()), List.of(ARTIST_NAME));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(firstAudioFile);

        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
    }

    @Test
    void shouldNotGetTrackWhenDiscNumberInvalid() {
        initialiseTagMocks(mockFirstTag, mockFirstAudioHeader,
                FIRST_TRACK_MBID.toString(), Integer.toString(-1), Integer.toString(FIRST_TRACK_NUMBER), FIRST_TRACK_NAME,
                FIRST_TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, List.of(ARTIST_MBID.toString()), List.of(ARTIST_NAME));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(firstAudioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenTrackNumberNull() {
        initialiseTagMocks(mockFirstTag, mockFirstAudioHeader,
                FIRST_TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), null, FIRST_TRACK_NAME,
                FIRST_TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, List.of(ARTIST_MBID.toString()), List.of(ARTIST_NAME));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(firstAudioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenTrackNumberEmpty() {
        initialiseTagMocks(mockFirstTag, mockFirstAudioHeader,
                FIRST_TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), "", FIRST_TRACK_NAME,
                FIRST_TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, List.of(ARTIST_MBID.toString()), List.of(ARTIST_NAME));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(firstAudioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenTrackNumberInvalid() {
        initialiseTagMocks(mockFirstTag, mockFirstAudioHeader,
                FIRST_TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(-1), FIRST_TRACK_NAME,
                FIRST_TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, List.of(ARTIST_MBID.toString()), List.of(ARTIST_NAME));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(firstAudioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenTrackNameNull() {
        initialiseTagMocks(mockFirstTag, mockFirstAudioHeader,
                FIRST_TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(FIRST_TRACK_NUMBER), null,
                FIRST_TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, List.of(ARTIST_MBID.toString()), List.of(ARTIST_NAME));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(firstAudioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenTrackNameEmpty() {
        initialiseTagMocks(mockFirstTag, mockFirstAudioHeader,
                FIRST_TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(FIRST_TRACK_NUMBER), "",
                FIRST_TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, List.of(ARTIST_MBID.toString()), List.of(ARTIST_NAME));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(firstAudioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenTrackLengthInvalid() {
        initialiseTagMocks(mockFirstTag, mockFirstAudioHeader,
                FIRST_TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(FIRST_TRACK_NUMBER), FIRST_TRACK_NAME,
                -1, ALBUM_MBID.toString(), ALBUM_NAME, List.of(ARTIST_MBID.toString()), List.of(ARTIST_NAME));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(firstAudioFile);

        assertFalse(actual.isPresent());
    }


    @Test
    void shouldNotGetTrackWhenAlbumMbidNull() {
        initialiseTagMocks(mockFirstTag, mockFirstAudioHeader,
                FIRST_TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(FIRST_TRACK_NUMBER), FIRST_TRACK_NAME,
                FIRST_TRACK_LENGTH, null, ALBUM_NAME, List.of(ARTIST_MBID.toString()), List.of(ARTIST_NAME));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(firstAudioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenAlbumMbidEmpty() {
        initialiseTagMocks(mockFirstTag, mockFirstAudioHeader,
                FIRST_TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(FIRST_TRACK_NUMBER), FIRST_TRACK_NAME,
                FIRST_TRACK_LENGTH, "", ALBUM_NAME, List.of(ARTIST_MBID.toString()), List.of(ARTIST_NAME));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(firstAudioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenAlbumMbidInvalid() {
        initialiseTagMocks(mockFirstTag, mockFirstAudioHeader,
                FIRST_TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(FIRST_TRACK_NUMBER), FIRST_TRACK_NAME,
                FIRST_TRACK_LENGTH, "1234", ALBUM_NAME, List.of(ARTIST_MBID.toString()), List.of(ARTIST_NAME));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(firstAudioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenAlbumNameNull() {
        initialiseTagMocks(mockFirstTag, mockFirstAudioHeader,
                FIRST_TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(FIRST_TRACK_NUMBER), FIRST_TRACK_NAME,
                FIRST_TRACK_LENGTH, ALBUM_MBID.toString(), null, List.of(ARTIST_MBID.toString()), List.of(ARTIST_NAME));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(firstAudioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenAlbumNameEmpty() {
        initialiseTagMocks(mockFirstTag, mockFirstAudioHeader,
                FIRST_TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(FIRST_TRACK_NUMBER), FIRST_TRACK_NAME,
                FIRST_TRACK_LENGTH, ALBUM_MBID.toString(), "", List.of(ARTIST_MBID.toString()), List.of(ARTIST_NAME));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(firstAudioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenArtistMbidEmpty() {
        initialiseTagMocks(mockFirstTag, mockFirstAudioHeader,
                FIRST_TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(FIRST_TRACK_NUMBER), FIRST_TRACK_NAME,
                FIRST_TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, List.of(""), List.of(ARTIST_NAME));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(firstAudioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenArtistMbidInvalid() {
        initialiseTagMocks(mockFirstTag, mockFirstAudioHeader,
                FIRST_TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(FIRST_TRACK_NUMBER), FIRST_TRACK_NAME,
                FIRST_TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, List.of("1234"), List.of(ARTIST_NAME));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(firstAudioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldFailWhenTagExceptionThrown()
            throws ReadOnlyFileException, IOException, TagException, InvalidAudioFrameException, CannotReadException {
        lenient().when(mockAudioFileProvider.getAudioFile(firstAudioFile)).thenThrow(new TagException("something happened"));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(firstAudioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldFailWhenReadOnlyFileExceptionThrown()
            throws ReadOnlyFileException, IOException, TagException, InvalidAudioFrameException, CannotReadException {
        lenient().when(mockAudioFileProvider.getAudioFile(firstAudioFile)).thenThrow(new ReadOnlyFileException("something happened"));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(firstAudioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldFailWhenCannotReadExceptionThrown()
            throws ReadOnlyFileException, IOException, TagException, InvalidAudioFrameException, CannotReadException {
        lenient().when(mockAudioFileProvider.getAudioFile(firstAudioFile)).thenThrow(new CannotReadException("something happened"));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(firstAudioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldFailWhenInvalidAudioFrameExceptionThrown()
            throws ReadOnlyFileException, IOException, TagException, InvalidAudioFrameException, CannotReadException {
        lenient().when(mockAudioFileProvider.getAudioFile(firstAudioFile)).thenThrow(new InvalidAudioFrameException("something happened"));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(firstAudioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldFailWhenIOExceptionThrown()
            throws ReadOnlyFileException, IOException, TagException, InvalidAudioFrameException, CannotReadException {
        lenient().when(mockAudioFileProvider.getAudioFile(firstAudioFile)).thenThrow(new IOException("something happened"));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(firstAudioFile);

        assertFalse(actual.isPresent());
    }
}