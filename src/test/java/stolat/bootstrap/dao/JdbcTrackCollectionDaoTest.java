package stolat.bootstrap.dao;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.test.context.TestPropertySource;
import stolat.bootstrap.model.Album;
import stolat.bootstrap.model.Track;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@AutoConfigureEmbeddedDatabase
@TestPropertySource("classpath:test-application.properties")
class JdbcTrackCollectionDaoTest {

    private static final String SCHEMA_NAME = "stolat";
    private static final String ALBUM_TABLE_NAME = "local_collection_album";
    private static final String TRACK_TABLE_NAME = "local_collection_track";

    private static final String SOME_ARTIST_NAME = "Some Artist";
    private static final String FIRST_ALBUM_NAME = "First Album";
    private static final String FIRST_ALBUM_FIRST_TRACK_NAME = "Some Track";
    private static final String FIRST_ALBUM_SECOND_TRACK_NAME = "Some Other Track";
    private static final String SOME_OTHER_ARTIST_NAME = "Some other Artist";
    private static final String SECOND_ALBUM_NAME = "Second Album";
    private static final String SECOND_ALBUM_FIRST_TRACK_NAME = "Something Else";
    private static final String SECOND_ALBUM_SECOND_TRACK_NAME = "yetanothertrack";

    private static final String YET_ANOTHER_ARTIST_NAME = "Yet Another Artist";
    private static final String THIRD_ALBUM_NAME = "Third Album";
    private static final String THIRD_ALBUM_FIRST_TRACK_NAME = "Now this is the greatest track!";
    private static final String THIRD_ALBUM_SECOND_TRACK_NAME = "This one is not as good as the previous one";
    private static final String THIRD_ALBUM_THIRD_TRACK_NAME = "Completely unnecessary track";

    private static final String ALBUM_SOURCE = "local";
    private static final String TRACK_FILE_TYPE = "flac";

    private Album initialFirstAlbum;
    private Album initialSecondAlbum;
    private Track initialFirstAlbumFirstTrack;
    private Track initialFirstAlbumSecondTrack;
    private Track initialSecondAlbumFirstTrack;
    private Track initialSecondAlbumSecondTrack;

    private Album updatedFirstAlbum;
    private Album updatedSecondAlbum;
    private Album updatedThirdAlbum;
    private Track updatedFirstAlbumFirstTrack;
    private Track updatedFirstAlbumSecondTrack;
    private Track updatedSecondAlbumFirstTrack;
    private Track updatedSecondAlbumSecondTrack;
    private Track updatedThirdAlbumFirstTrack;
    private Track updatedThirdAlbumSecondTrack;
    private Track updatedThirdAlbumThirdTrack;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcTrackCollectionDao trackCollectionDao;

    @BeforeEach
    void setUp() {
        initialiseInitialTestData();
        initialiseUpdatedTestData();
    }

    @AfterEach
    void tearDown() {
        String deleteTracks = "DELETE FROM " + SCHEMA_NAME + "." + TRACK_TABLE_NAME;
        jdbcTemplate.execute(deleteTracks);
        String deleteAlbums = "DELETE FROM " + SCHEMA_NAME + "." + ALBUM_TABLE_NAME;
        jdbcTemplate.execute(deleteAlbums);
    }

    private void initialiseInitialTestData() {

        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);

        initialFirstAlbum = new Album(
                UUID.randomUUID().toString(), FIRST_ALBUM_NAME,
                UUID.randomUUID().toString(), SOME_ARTIST_NAME);
        initialFirstAlbumFirstTrack = new Track(
                UUID.randomUUID().toString(), "1", "1", FIRST_ALBUM_FIRST_TRACK_NAME,
                123,
                Path.of(SOME_ARTIST_NAME, FIRST_ALBUM_NAME, FIRST_ALBUM_FIRST_TRACK_NAME + "." + TRACK_FILE_TYPE).toString(),
                initialFirstAlbum);
        initialFirstAlbumSecondTrack = new Track(
                UUID.randomUUID().toString(), "1", "2", FIRST_ALBUM_SECOND_TRACK_NAME,
                132,
                Path.of(SOME_ARTIST_NAME, FIRST_ALBUM_NAME, FIRST_ALBUM_SECOND_TRACK_NAME + "." + TRACK_FILE_TYPE).toString(),
                initialFirstAlbum);

        initialSecondAlbum = new Album(
                UUID.randomUUID().toString(), SECOND_ALBUM_NAME,
                UUID.randomUUID().toString(), SOME_OTHER_ARTIST_NAME);
        initialSecondAlbumFirstTrack = new Track(
                UUID.randomUUID().toString(), "1", "1", SECOND_ALBUM_FIRST_TRACK_NAME,
                111,
                Path.of(SOME_OTHER_ARTIST_NAME, SECOND_ALBUM_NAME, SECOND_ALBUM_FIRST_TRACK_NAME + "." + TRACK_FILE_TYPE).toString(),
                initialSecondAlbum);
        initialSecondAlbumSecondTrack = new Track(
                UUID.randomUUID().toString(), "1", "2", SECOND_ALBUM_SECOND_TRACK_NAME,
                222,
                Path.of(SOME_OTHER_ARTIST_NAME, SECOND_ALBUM_NAME, SECOND_ALBUM_SECOND_TRACK_NAME + "." + TRACK_FILE_TYPE).toString(),
                initialSecondAlbum);

        insertAlbum(initialFirstAlbum, oneHourAgo);
        insertAlbum(initialSecondAlbum, oneHourAgo);
        insertTrack(initialFirstAlbumFirstTrack, oneHourAgo);
        insertTrack(initialFirstAlbumSecondTrack, oneHourAgo);
        insertTrack(initialSecondAlbumFirstTrack, oneHourAgo);
        insertTrack(initialSecondAlbumSecondTrack, oneHourAgo);

        String selectTrackCount = "SELECT COUNT(*) FROM " + SCHEMA_NAME + "." + TRACK_TABLE_NAME;
        String selectAlbumCount = "SELECT COUNT(*) FROM " + SCHEMA_NAME + "." + ALBUM_TABLE_NAME;
        assertEquals(4, jdbcTemplate.queryForObject(selectTrackCount, Integer.TYPE));
        assertEquals(2, jdbcTemplate.queryForObject(selectAlbumCount, Integer.TYPE));
    }

    private void initialiseUpdatedTestData() {

        updatedFirstAlbum = new Album(
                initialFirstAlbum.getAlbumMusicBrainzId().toString(), "First Album Name Has Changed",
                initialFirstAlbum.getArtistMusicBrainzId().toString(), initialFirstAlbum.getArtistName());
        updatedFirstAlbumFirstTrack = new Track(
                initialFirstAlbumFirstTrack.getTrackMusicBrainzId().toString(),
                Integer.toString(initialFirstAlbumFirstTrack.getDiscNumber()),
                Integer.toString(initialFirstAlbumFirstTrack.getTrackNumber()),
                initialFirstAlbumFirstTrack.getTrackName(),
                124,
                Path.of(SOME_ARTIST_NAME, "First Album Name Has Changed", FIRST_ALBUM_FIRST_TRACK_NAME + "." + TRACK_FILE_TYPE).toString(),
                updatedFirstAlbum);
        updatedFirstAlbumSecondTrack = new Track(
                initialFirstAlbumSecondTrack.getTrackMusicBrainzId().toString(), "1", "2",
                "Some Other Track with a different name", 132,
                Path.of(SOME_ARTIST_NAME, "First Album Name Has Changed", "Some Other Track with a different name" + "." + TRACK_FILE_TYPE).toString(),
                updatedFirstAlbum);

        updatedSecondAlbum = new Album(
                initialSecondAlbum.getAlbumMusicBrainzId().toString(), initialSecondAlbum.getAlbumName(),
                UUID.randomUUID().toString(), "Some totally different Artist");
        updatedSecondAlbumFirstTrack = new Track(
                initialSecondAlbumFirstTrack.getTrackMusicBrainzId().toString(), "1", "1",
                "Something Else entirely",111,
                Path.of(SOME_OTHER_ARTIST_NAME, SECOND_ALBUM_NAME, "Something Else entirely" + "." + TRACK_FILE_TYPE).toString(),
                updatedSecondAlbum);
        updatedSecondAlbumSecondTrack = new Track(
                initialSecondAlbumSecondTrack.getTrackMusicBrainzId().toString(), "1", "4",
                SECOND_ALBUM_SECOND_TRACK_NAME,
                222, Path.of("Some totally different Artist", SECOND_ALBUM_NAME, SECOND_ALBUM_SECOND_TRACK_NAME + "." + TRACK_FILE_TYPE).toString(),
                updatedSecondAlbum);

        updatedThirdAlbum = new Album(
                UUID.randomUUID().toString(), THIRD_ALBUM_NAME,
                UUID.randomUUID().toString(), YET_ANOTHER_ARTIST_NAME);
        updatedThirdAlbumFirstTrack = new Track(
                UUID.randomUUID().toString(), "1", "1",
                THIRD_ALBUM_FIRST_TRACK_NAME, 222,
                Path.of(YET_ANOTHER_ARTIST_NAME, THIRD_ALBUM_NAME, THIRD_ALBUM_FIRST_TRACK_NAME + "." + TRACK_FILE_TYPE).toString(),
                updatedThirdAlbum);
        updatedThirdAlbumSecondTrack = new Track(
                UUID.randomUUID().toString(), "1", "2",
                THIRD_ALBUM_SECOND_TRACK_NAME, 211,
                Path.of(YET_ANOTHER_ARTIST_NAME, THIRD_ALBUM_NAME, THIRD_ALBUM_SECOND_TRACK_NAME + "." + TRACK_FILE_TYPE).toString(),
                updatedThirdAlbum);
        updatedThirdAlbumThirdTrack = new Track(
                UUID.randomUUID().toString(), "2", "1",
                THIRD_ALBUM_THIRD_TRACK_NAME, 131,
                Path.of(YET_ANOTHER_ARTIST_NAME, THIRD_ALBUM_NAME, THIRD_ALBUM_THIRD_TRACK_NAME + "." + TRACK_FILE_TYPE).toString(),
                updatedThirdAlbum);
    }

    private void insertAlbum(Album album, Instant instant) {
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
        albumParameterSource.addValue("last_updated", Timestamp.from(instant));

        albumInsert.execute(albumParameterSource);
    }

    private void insertTrack(Track track, Instant instant) {
        SimpleJdbcInsert trackInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withSchemaName(SCHEMA_NAME)
                .withTableName(TRACK_TABLE_NAME)
                .usingColumns("track_mbid", "disc_number", "track_number", "track_name", "track_length", "track_file_type", "track_path", "album_mbid", "last_updated");
        MapSqlParameterSource trackParameterSource = new MapSqlParameterSource();
        trackParameterSource.addValue("track_mbid", track.getTrackMusicBrainzId());
        trackParameterSource.addValue("disc_number", track.getDiscNumber());
        trackParameterSource.addValue("track_number", track.getTrackNumber());
        trackParameterSource.addValue("track_name", track.getTrackName());
        trackParameterSource.addValue("track_length", track.getTrackLength());
        trackParameterSource.addValue("track_file_type", TRACK_FILE_TYPE);
        trackParameterSource.addValue("track_path", track.getTrackRelativePath());
        trackParameterSource.addValue("album_mbid", track.getAlbum().getAlbumMusicBrainzId());
        trackParameterSource.addValue("last_updated", Timestamp.from(instant));

        trackInsert.execute(trackParameterSource);
    }

    @Test
    void shouldClearTrackCollection() {
        trackCollectionDao.clearTrackCollection();
        String selectTrackCount = "SELECT COUNT(*) FROM " + SCHEMA_NAME + "." + TRACK_TABLE_NAME;
        String selectAlbumCount = "SELECT COUNT(*) FROM " + SCHEMA_NAME + "." + ALBUM_TABLE_NAME;
        assertEquals(0, jdbcTemplate.queryForObject(selectTrackCount, Integer.TYPE));
        assertEquals(0, jdbcTemplate.queryForObject(selectAlbumCount, Integer.TYPE));
    }

    @Test
    void shouldDoNothingWhenTracksAlreadyExistAndForceOptionDisabled() {
        Set<Track> sameTracksWithSomeChanges = Set.of(
                updatedFirstAlbumFirstTrack, updatedFirstAlbumSecondTrack,
                updatedSecondAlbumFirstTrack, updatedSecondAlbumSecondTrack);

        trackCollectionDao.populateTrackCollection(sameTracksWithSomeChanges, false);

        String selectAllAlbums = "SELECT * FROM " + SCHEMA_NAME + "." + ALBUM_TABLE_NAME;
        List<Album> actualAlbums = jdbcTemplate.query(selectAllAlbums, new AlbumRowMapper());
        assertEquals(2, actualAlbums.size());
        assertTrue(actualAlbums.contains(initialFirstAlbum));
        assertTrue(actualAlbums.contains(initialSecondAlbum));

        String selectAllTracks = "SELECT * FROM " + SCHEMA_NAME + "." + ALBUM_TABLE_NAME + ", " + SCHEMA_NAME + "." + TRACK_TABLE_NAME;
        List<Track> actualTracks = jdbcTemplate.query(selectAllTracks, new TrackRowMapper());
        assertEquals(4, actualTracks.size());
        assertTrue(actualTracks.contains(initialFirstAlbumFirstTrack));
        assertTrue(actualTracks.contains(initialFirstAlbumSecondTrack));
        assertTrue(actualTracks.contains(initialSecondAlbumFirstTrack));
        assertTrue(actualTracks.contains(initialSecondAlbumSecondTrack));
    }

    @Test
    void shouldUpdateTracksWhenTracksAlreadyExistButForceOptionEnabled() {
        Set<Track> sameTracksWithSomeChanges = Set.of(
                updatedFirstAlbumFirstTrack, updatedFirstAlbumSecondTrack,
                updatedSecondAlbumFirstTrack, updatedSecondAlbumSecondTrack);

        trackCollectionDao.populateTrackCollection(sameTracksWithSomeChanges, true);

        String selectAllAlbums = "SELECT * FROM " + SCHEMA_NAME + "." + ALBUM_TABLE_NAME;
        List<Album> actualAlbums = jdbcTemplate.query(selectAllAlbums, new AlbumRowMapper());
        assertEquals(2, actualAlbums.size());
        assertTrue(actualAlbums.contains(updatedFirstAlbum));
        assertTrue(actualAlbums.contains(updatedSecondAlbum));

        String selectAllTracks = "SELECT * FROM " + SCHEMA_NAME + "." + ALBUM_TABLE_NAME + ", " + SCHEMA_NAME + "." + TRACK_TABLE_NAME;
        List<Track> actualTracks = jdbcTemplate.query(selectAllTracks, new TrackRowMapper());
        assertEquals(4, actualTracks.size());
        assertTrue(actualTracks.contains(updatedFirstAlbumFirstTrack));
        assertTrue(actualTracks.contains(updatedFirstAlbumSecondTrack));
        assertTrue(actualTracks.contains(updatedSecondAlbumFirstTrack));
        assertTrue(actualTracks.contains(updatedSecondAlbumSecondTrack));
    }

    @Test
    void shouldUpdateTracksWhenSomeTracksAreNew() {
        Set<Track> sameTracksPlusNewTracks = Set.of(
                updatedFirstAlbumFirstTrack, updatedFirstAlbumSecondTrack,
                updatedSecondAlbumFirstTrack, updatedSecondAlbumSecondTrack,
                updatedThirdAlbumFirstTrack, updatedThirdAlbumSecondTrack, updatedThirdAlbumThirdTrack);

        trackCollectionDao.populateTrackCollection(sameTracksPlusNewTracks, false);

        String selectAllAlbums = "SELECT * FROM " + SCHEMA_NAME + "." + ALBUM_TABLE_NAME;
        List<Album> actualAlbums = jdbcTemplate.query(selectAllAlbums, new AlbumRowMapper());
        assertEquals(3, actualAlbums.size());
        assertTrue(actualAlbums.contains(initialFirstAlbum));
        assertTrue(actualAlbums.contains(initialSecondAlbum));
        assertTrue(actualAlbums.contains(updatedThirdAlbum));

        String selectAllTracks = "SELECT * FROM " + SCHEMA_NAME + "." + ALBUM_TABLE_NAME + ", " + SCHEMA_NAME + "." + TRACK_TABLE_NAME;
        List<Track> actualTracks = jdbcTemplate.query(selectAllTracks, new TrackRowMapper());
        assertEquals(7, actualTracks.size());
        assertTrue(actualTracks.contains(initialFirstAlbumFirstTrack));
        assertTrue(actualTracks.contains(initialFirstAlbumSecondTrack));
        assertTrue(actualTracks.contains(initialSecondAlbumFirstTrack));
        assertTrue(actualTracks.contains(initialSecondAlbumSecondTrack));
        assertTrue(actualTracks.contains(updatedThirdAlbumFirstTrack));
        assertTrue(actualTracks.contains(updatedThirdAlbumSecondTrack));
        assertTrue(actualTracks.contains(updatedThirdAlbumThirdTrack));
    }

    private class AlbumRowMapper implements RowMapper<Album> {

        @Override
        public Album mapRow(ResultSet resultSet, int i) throws SQLException {
            final UUID albumMbid = resultSet.getObject("album_mbid", UUID.class);
            final String albumName = resultSet.getString("album_name");
            final UUID artistMbid = resultSet.getObject("artist_mbid", UUID.class);
            final String artistName = resultSet.getString("artist_name");
            return new Album(
                    albumMbid.toString(), albumName,
                    artistMbid.toString(), artistName);
        }
    }

    private class TrackRowMapper implements RowMapper<Track> {

        @Override
        public Track mapRow(ResultSet resultSet, int i) throws SQLException {
            final UUID trackMbid = resultSet.getObject("track_mbid", UUID.class);
            final int discNumber = resultSet.getInt("disc_number");
            final int trackNumber = resultSet.getInt("track_number");
            final String trackName = resultSet.getString("track_name");
            final int trackLength = resultSet.getInt("track_length");
            final Path trackPath = Path.of(resultSet.getString("track_path"));
            final Album album = new AlbumRowMapper().mapRow(resultSet, i);
            return new Track(
                    trackMbid.toString(),
                    Integer.toString(discNumber), Integer.toString(trackNumber),
                    trackName, trackLength, trackPath.toString(), album);
        }
    }
}