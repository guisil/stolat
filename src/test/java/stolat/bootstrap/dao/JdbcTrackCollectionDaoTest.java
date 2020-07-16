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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static stolat.bootstrap.dao.DatabaseConstants.*;


@SpringBootTest
@AutoConfigureEmbeddedDatabase
@TestPropertySource("classpath:test-application.properties")
class JdbcTrackCollectionDaoTest {

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
        String deleteTracks = "DELETE FROM " + TRACK_TABLE_FULL_NAME;
        jdbcTemplate.execute(deleteTracks);
        String deleteAlbums = "DELETE FROM " + ALBUM_TABLE_FULL_NAME;
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

        String selectTrackCount = "SELECT COUNT(*) FROM " + TRACK_TABLE_FULL_NAME;
        String selectAlbumCount = "SELECT COUNT(*) FROM " + ALBUM_TABLE_FULL_NAME;
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
                "Something Else entirely", 111,
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
                .usingColumns(ALBUM_MBID_COLUMN, ALBUM_NAME_COLUMN, ALBUM_SOURCE_COLUMN, ARTIST_MBID_COLUMN, ARTIST_NAME_COLUMN, LAST_UPDATED_COLUMN);
        MapSqlParameterSource albumParameterSource = new MapSqlParameterSource();
        albumParameterSource.addValue(ALBUM_MBID_COLUMN, album.getAlbumMusicBrainzId());
        albumParameterSource.addValue(ALBUM_NAME_COLUMN, album.getAlbumName());
        albumParameterSource.addValue(ALBUM_SOURCE_COLUMN, LOCAL_ALBUM_SOURCE);
        albumParameterSource.addValue(ARTIST_MBID_COLUMN, album.getArtistMusicBrainzId());
        albumParameterSource.addValue(ARTIST_NAME_COLUMN, album.getArtistName());
        albumParameterSource.addValue(LAST_UPDATED_COLUMN, Timestamp.from(instant));

        albumInsert.execute(albumParameterSource);
    }

    private void insertTrack(Track track, Instant instant) {
        MapSqlParameterSource trackParameterSource = new MapSqlParameterSource();
        SimpleJdbcInsert trackInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withSchemaName(SCHEMA_NAME)
                .withTableName(TRACK_TABLE_NAME)
                .usingColumns(TRACK_MBID_COLUMN, DISC_NUMBER_COLUMN, TRACK_NUMBER_COLUMN, TRACK_NAME_COLUMN, TRACK_LENGTH_COLUMN, TRACK_FILE_TYPE_COLUMN, TRACK_PATH_COLUMN, ALBUM_MBID_COLUMN, LAST_UPDATED_COLUMN);
        trackParameterSource.addValue(TRACK_MBID_COLUMN, track.getTrackMusicBrainzId());
        trackParameterSource.addValue(DISC_NUMBER_COLUMN, track.getDiscNumber());
        trackParameterSource.addValue(TRACK_NUMBER_COLUMN, track.getTrackNumber());
        trackParameterSource.addValue(TRACK_NAME_COLUMN, track.getTrackName());
        trackParameterSource.addValue(TRACK_LENGTH_COLUMN, track.getTrackLength());
        trackParameterSource.addValue(TRACK_FILE_TYPE_COLUMN, TRACK_FILE_TYPE);
        trackParameterSource.addValue(TRACK_PATH_COLUMN, track.getTrackRelativePath());
        trackParameterSource.addValue(ALBUM_MBID_COLUMN, track.getAlbum().getAlbumMusicBrainzId());
        trackParameterSource.addValue(LAST_UPDATED_COLUMN, Timestamp.from(instant));

        trackInsert.execute(trackParameterSource);
    }

    @Test
    void shouldClearTrackCollection() {
        trackCollectionDao.clearTrackCollection();
        String selectTrackCount = "SELECT COUNT(*) FROM " + TRACK_TABLE_FULL_NAME;
        String selectAlbumCount = "SELECT COUNT(*) FROM " + ALBUM_TABLE_FULL_NAME;
        assertEquals(0, jdbcTemplate.queryForObject(selectTrackCount, Integer.TYPE));
        assertEquals(0, jdbcTemplate.queryForObject(selectAlbumCount, Integer.TYPE));
    }

    @Test
    void shouldDoNothingWhenTracksAlreadyExistAndForceOptionDisabled() {
        Set<Track> sameTracksWithSomeChanges = Set.of(
                updatedFirstAlbumFirstTrack, updatedFirstAlbumSecondTrack,
                updatedSecondAlbumFirstTrack, updatedSecondAlbumSecondTrack);

        trackCollectionDao.populateTrackCollection(sameTracksWithSomeChanges, false);

        String selectAllAlbums = "SELECT * FROM " + ALBUM_TABLE_FULL_NAME;
        List<Album> actualAlbums = jdbcTemplate.query(selectAllAlbums, new AlbumRowMapper());
        assertEquals(2, actualAlbums.size());
        assertTrue(actualAlbums.contains(initialFirstAlbum));
        assertTrue(actualAlbums.contains(initialSecondAlbum));

        String selectAllTracks = "SELECT * FROM " + TRACK_TABLE_FULL_NAME + " t" +
                " INNER JOIN " + ALBUM_TABLE_FULL_NAME + " a" + " ON t." + ALBUM_MBID_COLUMN + " = a." + ALBUM_MBID_COLUMN;
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

        String selectAllAlbums = "SELECT * FROM " + ALBUM_TABLE_FULL_NAME;
        List<Album> actualAlbums = jdbcTemplate.query(selectAllAlbums, new AlbumRowMapper());
        assertEquals(2, actualAlbums.size());
        assertTrue(actualAlbums.contains(updatedFirstAlbum));
        assertTrue(actualAlbums.contains(updatedSecondAlbum));

        String selectAllTracks = "SELECT * FROM " + TRACK_TABLE_FULL_NAME + " t" +
                " INNER JOIN " + ALBUM_TABLE_FULL_NAME + " a" + " ON t." + ALBUM_MBID_COLUMN + " = a." + ALBUM_MBID_COLUMN;
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

        String selectAllAlbums = "SELECT * FROM " + ALBUM_TABLE_FULL_NAME;
        List<Album> actualAlbums = jdbcTemplate.query(selectAllAlbums, new AlbumRowMapper());
        assertEquals(3, actualAlbums.size());
        assertTrue(actualAlbums.contains(initialFirstAlbum));
        assertTrue(actualAlbums.contains(initialSecondAlbum));
        assertTrue(actualAlbums.contains(updatedThirdAlbum));

        String selectAllTracks = "SELECT * FROM " + TRACK_TABLE_FULL_NAME + " t" +
                " INNER JOIN " + ALBUM_TABLE_FULL_NAME + " a" + " ON t." + ALBUM_MBID_COLUMN + " = a." + ALBUM_MBID_COLUMN;
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
            final UUID albumMbid = resultSet.getObject(ALBUM_MBID_COLUMN, UUID.class);
            final String albumName = resultSet.getString(ALBUM_NAME_COLUMN);
            final UUID artistMbid = resultSet.getObject(ARTIST_MBID_COLUMN, UUID.class);
            final String artistName = resultSet.getString(ARTIST_NAME_COLUMN);
            return new Album(
                    albumMbid.toString(), albumName,
                    artistMbid.toString(), artistName);
        }
    }

    private class TrackRowMapper implements RowMapper<Track> {

        @Override
        public Track mapRow(ResultSet resultSet, int i) throws SQLException {
            final UUID trackMbid = resultSet.getObject(TRACK_MBID_COLUMN, UUID.class);
            final int discNumber = resultSet.getInt(DISC_NUMBER_COLUMN);
            final int trackNumber = resultSet.getInt(TRACK_NUMBER_COLUMN);
            final String trackName = resultSet.getString(TRACK_NAME_COLUMN);
            final int trackLength = resultSet.getInt(TRACK_LENGTH_COLUMN);
            final Path trackPath = Path.of(resultSet.getString(TRACK_PATH_COLUMN));
            final Album album = new AlbumRowMapper().mapRow(resultSet, i);
            return new Track(
                    trackMbid.toString(),
                    Integer.toString(discNumber), Integer.toString(trackNumber),
                    trackName, trackLength, trackPath.toString(), album);
        }
    }
}