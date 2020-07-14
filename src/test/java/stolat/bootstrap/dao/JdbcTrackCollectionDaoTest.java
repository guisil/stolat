package stolat.bootstrap.dao;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.test.context.TestPropertySource;
import stolat.bootstrap.model.Album;
import stolat.bootstrap.model.Track;

import java.io.File;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;


@SpringBootTest
@AutoConfigureEmbeddedDatabase
@TestPropertySource("classpath:test-application.properties")
//@ExtendWith(MockitoExtension.class)
class JdbcTrackCollectionDaoTest {

    private static final Instant FIXED_INSTANT = Instant.now();

    private static final String SCHEMA_NAME = "stolat";
    private static final String ALBUM_TABLE_NAME = "local_collection_album";
    private static final String TRACK_TABLE_NAME = "local_collection_track";

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
    private static final String EMPTY_ALBUM_FOLDER = "Empty Album";
    private static final String ALBUM_SOURCE = "local";
    private static final String TRACK_FILE_TYPE = "flac";

    private File someOtherArtistFolder;
    private File firstAlbumFirstTrackFile;
    private File firstAlbumSecondTrackFile;
    private File secondAlbumFirstTrackFile;
    private File secondAlbumSecondTrackFile;

    private Album initialFirstAlbum;
    private Album initialSecondAlbum;
    private Track initialFirstAlbumFirstTrack;
    private Track initialFirstAlbumSecondTrack;
    private Track initialSecondAlbumFirstTrack;
    private Track initialSecondAlbumSecondTrack;

//    @Mock
//    private Clock mockClock;

//    @Mock
//    private NamedParameterJdbcTemplate mockJdbcTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

//    @InjectMocks
    @Autowired
    private JdbcTrackCollectionDao trackCollectionDao;

    @BeforeEach
    void setUp() {
        final Clock fixedClock = Clock.fixed(FIXED_INSTANT, ZoneId.systemDefault());

//        lenient().when(mockClock.instant()).thenReturn(fixedClock.instant());

        initialiseTestData();
    }

    private void initialiseTestData() {

        initialFirstAlbum = new Album(
                UUID.randomUUID().toString(), "First Album",
                UUID.randomUUID().toString(), "Some Artist");
        initialFirstAlbumFirstTrack = new Track(
                UUID.randomUUID().toString(), "1", "1", "Some Track",
                123, Path.of(SOME_ARTIST_FOLDER, FIRST_ALBUM_FOLDER, FIRST_ALBUM_FIRST_TRACK).toString(), initialFirstAlbum);
        initialFirstAlbumSecondTrack = new Track(
                UUID.randomUUID().toString(), "1", "2", "Some Other Track",
                132, Path.of(SOME_ARTIST_FOLDER, FIRST_ALBUM_FOLDER, FIRST_ALBUM_SECOND_TRACK).toString(), initialFirstAlbum);

        initialSecondAlbum = new Album(
                UUID.randomUUID().toString(), "Second Album",
                UUID.randomUUID().toString(), "Some other Artist");
        initialSecondAlbumFirstTrack = new Track(
                UUID.randomUUID().toString(), "1", "1", "Something Else",
                111, Path.of(SOME_OTHER_ARTIST_FOLDER, SECOND_ALBUM_FOLDER, SECOND_ALBUM_FIRST_TRACK).toString(), initialSecondAlbum);
        initialSecondAlbumSecondTrack = new Track(
                UUID.randomUUID().toString(), "1", "2", "Yet Another Track",
                222, Path.of(SOME_OTHER_ARTIST_FOLDER, SECOND_ALBUM_FOLDER, SECOND_ALBUM_SECOND_TRACK).toString(), initialSecondAlbum);

        insertAlbum(initialFirstAlbum);
        insertAlbum(initialSecondAlbum);
        insertTrack(initialFirstAlbumFirstTrack);
        insertTrack(initialFirstAlbumSecondTrack);
        insertTrack(initialSecondAlbumFirstTrack);
        insertTrack(initialSecondAlbumSecondTrack);

        String trackCount = "SELECT COUNT(*) FROM " + SCHEMA_NAME + "." + TRACK_TABLE_NAME;
        String albumCount = "SELECT COUNT(*) FROM " + SCHEMA_NAME + "." + ALBUM_TABLE_NAME;
        assertEquals(4, jdbcTemplate.queryForObject(trackCount, Integer.TYPE));
        assertEquals(2, jdbcTemplate.queryForObject(albumCount, Integer.TYPE));
    }

    private void insertAlbum(Album album) {
        SimpleJdbcInsert albumInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withSchemaName(SCHEMA_NAME)
                .withTableName(ALBUM_TABLE_NAME)
                .usingColumns("album_mbid", "album_name", "album_source", "artist_mbid", "artist_name", "last_updated");
        MapSqlParameterSource albumParameterSource = new MapSqlParameterSource();
        albumParameterSource.addValue("album_mbid", album.getAlbumMusicBrainzId());
        albumParameterSource.addValue("album_name", album.getAlbumName());
        albumParameterSource.addValue("album_source", ALBUM_SOURCE);
        albumParameterSource.addValue("artist_mbid", album.getArtistMusicBrainzId());
        albumParameterSource.addValue("artist_name", album.getArtistName());
        albumParameterSource.addValue("last_updated", Timestamp.from(Instant.now()));
        //TODO Instant.now(mockClock));

        albumInsert.execute(albumParameterSource);
    }

    private void insertTrack(Track track) {
        SimpleJdbcInsert trackInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withSchemaName(SCHEMA_NAME)
                .withTableName(TRACK_TABLE_NAME)
                .usingColumns("track_mbid", "track_number", "track_name", "track_length", "track_file_type", "track_path", "album_mbid", "last_updated");
        MapSqlParameterSource trackParameterSource = new MapSqlParameterSource();
        trackParameterSource.addValue("track_mbid", track.getTrackMusicBrainzId());
        trackParameterSource.addValue("track_number", track.getTrackNumber());
        trackParameterSource.addValue("track_name", track.getTrackName());
        trackParameterSource.addValue("track_length", track.getTrackLength());
        trackParameterSource.addValue("track_file_type", TRACK_FILE_TYPE);
        trackParameterSource.addValue("track_path", track.getTrackRelativePath());
        trackParameterSource.addValue("album_mbid", track.getAlbum().getAlbumMusicBrainzId());
        trackParameterSource.addValue("last_updated", Timestamp.from(Instant.now()));
        //TODO Instant.now(mockClock));

        trackInsert.execute(trackParameterSource);
    }

    @Test
    void clearTrackCollection() {
        trackCollectionDao.clearTrackCollection();
        String trackCount = "SELECT COUNT(*) FROM " + SCHEMA_NAME + "." + TRACK_TABLE_NAME;
        String albumCount = "SELECT COUNT(*) FROM " + SCHEMA_NAME + "." + ALBUM_TABLE_NAME;
        assertEquals(0, jdbcTemplate.queryForObject(trackCount, Integer.TYPE));
        assertEquals(0, jdbcTemplate.queryForObject(albumCount, Integer.TYPE));
    }

    @Test
    void populateTrackCollection() {
        fail("not tested yet");
    }
}