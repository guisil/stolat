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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stolat.bootstrap.filesystem.FileSystemProperties;
import stolat.bootstrap.model.Album;
import stolat.bootstrap.model.Track;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class JAudioTaggerFlacTagInfoReaderTest {

    private static final String COLLECTION_ROOT_PATH = Path.of(File.separator, "some", "path", "music").toString();
    private static final String AUDIO_FILE_RELATIVE_PATH = Path.of("The Awesome Musicians", "That Great Album", "Great to Listen.flac").toString();
    private static final Instant FIXED_INSTANT = Instant.now();

    private static final UUID ALBUM_MBID = UUID.randomUUID();
    private static final String ALBUM_NAME = "That Great Album";
    private static final UUID ARTIST_MBID = UUID.randomUUID();
    private static final String ARTIST_NAME = "The Awesome Musicians";
    private static final UUID TRACK_MBID = UUID.randomUUID();
    private static final int DISC_NUMBER = 1;
    private static final int TRACK_NUMBER = 7;
    private static final String TRACK_NAME = "Great to Listen";
    private static final int TRACK_LENGTH = 234;

    @Mock
    private Clock mockClock;

    @Mock
    private FileSystemProperties mockFileSystemProperties;

    @Mock
    private AudioFile mockAudioFile;

    @Mock
    private Tag mockTag;

    @Mock
    private AudioHeader mockAudioHeader;

    @Mock
    private JAudioTaggerAudioFileProvider mockAudioFileProvider;

    private File audioFile;

    @InjectMocks
    private JAudioTaggerFlacTagInfoReader tagInfoReader;

    @BeforeEach
    void setUp() throws ReadOnlyFileException, IOException, TagException, InvalidAudioFrameException, CannotReadException {

        final Clock fixedClock = Clock.fixed(FIXED_INSTANT, ZoneId.systemDefault());
        audioFile = new File(COLLECTION_ROOT_PATH, AUDIO_FILE_RELATIVE_PATH);

        lenient().when(mockAudioFileProvider.getAudioFile(audioFile)).thenReturn(mockAudioFile);
        lenient().when(mockAudioFile.getTag()).thenReturn(mockTag);
        lenient().when(mockAudioFile.getAudioHeader()).thenReturn(mockAudioHeader);

        lenient().when(mockClock.instant()).thenReturn(fixedClock.instant());
        lenient().when(mockFileSystemProperties.getAlbumCollectionPath()).thenReturn(COLLECTION_ROOT_PATH);
    }

    private void initialiseTagMocks(String trackMbidTag, String discNumberTag, String trackNumberTag, String trackNameTag,
                                    int trackLength, String albumMbidTag, String albumNameTag,
                                    String artistMbidTag, String artistNameTag) {
        lenient().when(mockTag.getFirst(FieldKey.MUSICBRAINZ_TRACK_ID)).thenReturn(trackMbidTag);
        lenient().when(mockTag.getFirst(FieldKey.DISC_NO)).thenReturn(discNumberTag);
        lenient().when(mockTag.getFirst(FieldKey.TRACK)).thenReturn(trackNumberTag);
        lenient().when(mockTag.getFirst(FieldKey.TITLE)).thenReturn(trackNameTag);
        lenient().when(mockAudioHeader.getTrackLength()).thenReturn(trackLength);
        lenient().when(mockTag.getFirst(FieldKey.MUSICBRAINZ_RELEASE_GROUP_ID)).thenReturn(albumMbidTag);
        lenient().when(mockTag.getFirst(FieldKey.ALBUM)).thenReturn(albumNameTag);
        lenient().when(mockTag.getFirst(FieldKey.MUSICBRAINZ_ARTISTID)).thenReturn(artistMbidTag);
        lenient().when(mockTag.getFirst(FieldKey.ARTIST)).thenReturn(artistNameTag);
    }

    @Test
    void shouldGetTrackWithCorrectTags() {
        final Album album = new Album(ALBUM_MBID.toString(), ALBUM_NAME, ARTIST_MBID.toString(), ARTIST_NAME);
        final Track expected = new Track(
                TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(TRACK_NUMBER), TRACK_NAME,
                TRACK_LENGTH, AUDIO_FILE_RELATIVE_PATH, album, FIXED_INSTANT);
        initialiseTagMocks(TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(TRACK_NUMBER), TRACK_NAME,
                TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, ARTIST_MBID.toString(), ARTIST_NAME);

        final Optional<Track> actual = tagInfoReader.getTrackInfo(audioFile);

        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
    }

    @Test
    void shouldNotGetTrackWhenTrackMbidNull() {
        initialiseTagMocks(null, Integer.toString(DISC_NUMBER), Integer.toString(TRACK_NUMBER), TRACK_NAME,
                TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, ARTIST_MBID.toString(), ARTIST_NAME);

        final Optional<Track> actual = tagInfoReader.getTrackInfo(audioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenTrackMbidEmpty() {
        initialiseTagMocks("", Integer.toString(DISC_NUMBER), Integer.toString(TRACK_NUMBER), TRACK_NAME,
                TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, ARTIST_MBID.toString(), ARTIST_NAME);

        final Optional<Track> actual = tagInfoReader.getTrackInfo(audioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenTrackMbidInvalid() {
        initialiseTagMocks("1234", Integer.toString(DISC_NUMBER), Integer.toString(TRACK_NUMBER), TRACK_NAME,
                TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, ARTIST_MBID.toString(), ARTIST_NAME);

        final Optional<Track> actual = tagInfoReader.getTrackInfo(audioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldStillGetTrackWithCorrectTagsWhenDiscNumberNull() {
        final Album album = new Album(ALBUM_MBID.toString(), ALBUM_NAME, ARTIST_MBID.toString(), ARTIST_NAME);
        final Track expected = new Track(
                TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(TRACK_NUMBER), TRACK_NAME,
                TRACK_LENGTH, AUDIO_FILE_RELATIVE_PATH, album, FIXED_INSTANT);
        initialiseTagMocks(TRACK_MBID.toString(), null, Integer.toString(TRACK_NUMBER), TRACK_NAME,
                TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, ARTIST_MBID.toString(), ARTIST_NAME);

        final Optional<Track> actual = tagInfoReader.getTrackInfo(audioFile);

        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
    }

    @Test
    void shouldStillGetTrackWithCorrectTagsWhenDiscNumberEmpty() {
        final Album album = new Album(ALBUM_MBID.toString(), ALBUM_NAME, ARTIST_MBID.toString(), ARTIST_NAME);
        final Track expected = new Track(
                TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(TRACK_NUMBER), TRACK_NAME,
                TRACK_LENGTH, AUDIO_FILE_RELATIVE_PATH, album, FIXED_INSTANT);
        initialiseTagMocks(TRACK_MBID.toString(), "", Integer.toString(TRACK_NUMBER), TRACK_NAME,
                TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, ARTIST_MBID.toString(), ARTIST_NAME);

        final Optional<Track> actual = tagInfoReader.getTrackInfo(audioFile);

        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
    }

    @Test
    void shouldNotGetTrackWhenDiscNumberInvalid() {
        initialiseTagMocks(TRACK_MBID.toString(), Integer.toString(-1), Integer.toString(TRACK_NUMBER), TRACK_NAME,
                TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, ARTIST_MBID.toString(), ARTIST_NAME);

        final Optional<Track> actual = tagInfoReader.getTrackInfo(audioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenTrackNumberNull() {
        initialiseTagMocks(TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), null, TRACK_NAME,
                TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, ARTIST_MBID.toString(), ARTIST_NAME);

        final Optional<Track> actual = tagInfoReader.getTrackInfo(audioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenTrackNumberEmpty() {
        initialiseTagMocks(TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), "", TRACK_NAME,
                TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, ARTIST_MBID.toString(), ARTIST_NAME);

        final Optional<Track> actual = tagInfoReader.getTrackInfo(audioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenTrackNumberInvalid() {
        initialiseTagMocks(TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(-1), TRACK_NAME,
                TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, ARTIST_MBID.toString(), ARTIST_NAME);

        final Optional<Track> actual = tagInfoReader.getTrackInfo(audioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenTrackNameNull() {
        initialiseTagMocks(TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(TRACK_NUMBER), null,
                TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, ARTIST_MBID.toString(), ARTIST_NAME);

        final Optional<Track> actual = tagInfoReader.getTrackInfo(audioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenTrackNameEmpty() {
        initialiseTagMocks(TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(TRACK_NUMBER), "",
                TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, ARTIST_MBID.toString(), ARTIST_NAME);

        final Optional<Track> actual = tagInfoReader.getTrackInfo(audioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenTrackLengthInvalid() {
        initialiseTagMocks(TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(TRACK_NUMBER), TRACK_NAME,
                -1, ALBUM_MBID.toString(), ALBUM_NAME, ARTIST_MBID.toString(), ARTIST_NAME);

        final Optional<Track> actual = tagInfoReader.getTrackInfo(audioFile);

        assertFalse(actual.isPresent());
    }


    @Test
    void shouldNotGetTrackWhenAlbumMbidNull() {
        initialiseTagMocks(TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(TRACK_NUMBER), TRACK_NAME,
                TRACK_LENGTH, null, ALBUM_NAME, ARTIST_MBID.toString(), ARTIST_NAME);

        final Optional<Track> actual = tagInfoReader.getTrackInfo(audioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenAlbumMbidEmpty() {
        initialiseTagMocks(TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(TRACK_NUMBER), TRACK_NAME,
                TRACK_LENGTH, "", ALBUM_NAME, ARTIST_MBID.toString(), ARTIST_NAME);

        final Optional<Track> actual = tagInfoReader.getTrackInfo(audioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenAlbumMbidInvalid() {
        initialiseTagMocks(TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(TRACK_NUMBER), TRACK_NAME,
                TRACK_LENGTH, "1234", ALBUM_NAME, ARTIST_MBID.toString(), ARTIST_NAME);

        final Optional<Track> actual = tagInfoReader.getTrackInfo(audioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenAlbumNameNull() {
        initialiseTagMocks(TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(TRACK_NUMBER), TRACK_NAME,
                TRACK_LENGTH, ALBUM_MBID.toString(), null, ARTIST_MBID.toString(), ARTIST_NAME);

        final Optional<Track> actual = tagInfoReader.getTrackInfo(audioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenAlbumNameEmpty() {
        initialiseTagMocks(TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(TRACK_NUMBER), TRACK_NAME,
                TRACK_LENGTH, ALBUM_MBID.toString(), "", ARTIST_MBID.toString(), ARTIST_NAME);

        final Optional<Track> actual = tagInfoReader.getTrackInfo(audioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenArtistMbidNull() {
        initialiseTagMocks(TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(TRACK_NUMBER), TRACK_NAME,
                TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, null, ARTIST_NAME);

        final Optional<Track> actual = tagInfoReader.getTrackInfo(audioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenArtistMbidEmpty() {
        initialiseTagMocks(TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(TRACK_NUMBER), TRACK_NAME,
                TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, "", ARTIST_NAME);

        final Optional<Track> actual = tagInfoReader.getTrackInfo(audioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenArtistMbidInvalid() {
        initialiseTagMocks(TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(TRACK_NUMBER), TRACK_NAME,
                TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, "1234", ARTIST_NAME);

        final Optional<Track> actual = tagInfoReader.getTrackInfo(audioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenArtistNameNull() {
        initialiseTagMocks(TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(TRACK_NUMBER), TRACK_NAME,
                TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, ARTIST_MBID.toString(), null);

        final Optional<Track> actual = tagInfoReader.getTrackInfo(audioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldNotGetTrackWhenArtistNameEmpty() {
        initialiseTagMocks(TRACK_MBID.toString(), Integer.toString(DISC_NUMBER), Integer.toString(TRACK_NUMBER), TRACK_NAME,
                TRACK_LENGTH, ALBUM_MBID.toString(), ALBUM_NAME, ARTIST_MBID.toString(), null);

        final Optional<Track> actual = tagInfoReader.getTrackInfo(audioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldFailWhenTagExceptionThrown()
            throws ReadOnlyFileException, IOException, TagException, InvalidAudioFrameException, CannotReadException {
        lenient().when(mockAudioFileProvider.getAudioFile(audioFile)).thenThrow(new TagException("something happened"));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(audioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldFailWhenReadOnlyFileExceptionThrown()
            throws ReadOnlyFileException, IOException, TagException, InvalidAudioFrameException, CannotReadException {
        lenient().when(mockAudioFileProvider.getAudioFile(audioFile)).thenThrow(new ReadOnlyFileException("something happened"));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(audioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldFailWhenCannotReadExceptionThrown()
            throws ReadOnlyFileException, IOException, TagException, InvalidAudioFrameException, CannotReadException {
        lenient().when(mockAudioFileProvider.getAudioFile(audioFile)).thenThrow(new CannotReadException("something happened"));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(audioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldFailWhenInvalidAudioFrameExceptionThrown()
            throws ReadOnlyFileException, IOException, TagException, InvalidAudioFrameException, CannotReadException {
        lenient().when(mockAudioFileProvider.getAudioFile(audioFile)).thenThrow(new InvalidAudioFrameException("something happened"));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(audioFile);

        assertFalse(actual.isPresent());
    }

    @Test
    void shouldFailWhenIOExceptionThrown()
            throws ReadOnlyFileException, IOException, TagException, InvalidAudioFrameException, CannotReadException {
        lenient().when(mockAudioFileProvider.getAudioFile(audioFile)).thenThrow(new IOException("something happened"));

        final Optional<Track> actual = tagInfoReader.getTrackInfo(audioFile);

        assertFalse(actual.isPresent());
    }
}