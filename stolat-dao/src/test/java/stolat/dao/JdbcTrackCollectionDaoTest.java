package stolat.dao;

import io.zonky.test.db.postgres.junit5.EmbeddedPostgresExtension;
import io.zonky.test.db.postgres.junit5.PreparedDbExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import stolat.model.Album;
import stolat.model.Artist;
import stolat.model.Track;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static stolat.dao.StolatDatabaseConstants.*;

class JdbcTrackCollectionDaoTest {

    private static final String SOME_ARTIST_NAME = "A Some Artist";
    private static final String FIRST_ALBUM_NAME = "A First Album";
    private static final String FIRST_ALBUM_FIRST_TRACK_NAME = "Some Track";
    private static final String FIRST_ALBUM_SECOND_TRACK_NAME = "Some Other Track";
    private static final String SOME_OTHER_ARTIST_NAME = "B Some other Artist";
    private static final String SECOND_ALBUM_NAME = "B Second Album";
    private static final String SECOND_ALBUM_FIRST_TRACK_NAME = "Something Else";
    private static final String SECOND_ALBUM_SECOND_TRACK_NAME = "yetanothertrack";
    private static final String YET_ANOTHER_ARTIST_NAME = "C Yet Another Artist";
    private static final String THIRD_ALBUM_NAME = "C Third Album";
    private static final String THIRD_ALBUM_FIRST_TRACK_NAME = "Now this is the greatest track!";
    private static final String THIRD_ALBUM_SECOND_TRACK_NAME = "This one is not as good as the previous one";
    private static final String THIRD_ALBUM_THIRD_TRACK_NAME = "Completely unnecessary track";
    private static final String AND_NOW_THE_LAST_ARTIST_NAME = "Z And Now The Last Artist";
    private static final String FOURTH_ALBUM_NAME = "D Fourth Album";
    private static final String FOURTH_ALBUM_FIRST_TRACK_NAME = "This is not such a great track";
    private static final String FOURTH_ALBUM_SECOND_TRACK_NAME = "And this is really a dreadful track";
    private static final String TRACK_FILE_TYPE = "flac";

    @RegisterExtension
    public static PreparedDbExtension db =
            EmbeddedPostgresExtension.preparedDatabase(
                    ConfiguredFlywayPreparer.forClasspathLocationAndSchemas(
                            List.of("db/migration"),
                            List.of("stolat", "musicbrainz"),
                            "stolat"));

    private Album initialFirstAlbum;
    private Album initialSecondAlbum;
    private Track initialFirstAlbumFirstTrack;
    private Track initialFirstAlbumSecondTrack;
    private Track initialSecondAlbumFirstTrack;
    private Track initialSecondAlbumSecondTrack;
    private Album updatedFirstAlbum;
    private Album updatedSecondAlbum;
    private Album updatedThirdAlbum;
    private Album updatedFourthAlbum;
    private Track updatedFirstAlbumFirstTrack;
    private Track updatedFirstAlbumSecondTrack;
    private Track updatedSecondAlbumFirstTrack;
    private Track updatedSecondAlbumSecondTrack;
    private Track updatedThirdAlbumFirstTrack;
    private Track updatedThirdAlbumSecondTrack;
    private Track updatedThirdAlbumThirdTrack;
    private Track updatedFourthAlbumFirstTrack;
    private Track updatedFourthAlbumSecondTrack;
    private JdbcTemplate jdbcTemplate;

    private JdbcTrackCollectionDao trackCollectionDao;

    @Test
    void shouldClearTrackCollection() {

        initialiseTestVariables();
        initialiseInitialTestData();
        initialiseUpdatedTestData();

        trackCollectionDao.clearTrackCollection();
        String selectTrackCount = "SELECT COUNT(*) FROM " + TRACK_TABLE_FULL_NAME;
        String selectAlbumCount = "SELECT COUNT(*) FROM " + ALBUM_TABLE_FULL_NAME;
        String selectArtistCount = "SELECT COUNT(*) FROM " + ARTIST_TABLE_FULL_NAME;
        String selectAlbumArtistCount = "SELECT COUNT(*) FROM " + ALBUM_ARTIST_TABLE_FULL_NAME;
        assertEquals(0, jdbcTemplate.queryForObject(selectTrackCount, Integer.TYPE));
        assertEquals(0, jdbcTemplate.queryForObject(selectAlbumCount, Integer.TYPE));
        assertEquals(0, jdbcTemplate.queryForObject(selectArtistCount, Integer.TYPE));
        assertEquals(0, jdbcTemplate.queryForObject(selectAlbumArtistCount, Integer.TYPE));

        clearTestData();
    }

    @Test
    void shouldUpdateTracksWhenTracksAlreadyExist() {

        initialiseTestVariables();
        initialiseInitialTestData();
        initialiseUpdatedTestData();

        List<Track> sameTracksWithSomeChanges = List.of(
                updatedFirstAlbumFirstTrack, updatedFirstAlbumSecondTrack,
                updatedSecondAlbumFirstTrack, updatedSecondAlbumSecondTrack);

        trackCollectionDao.updateTrackCollection(sameTracksWithSomeChanges);

        List<Album> actualAlbums = jdbcTemplate.query(getSelectAllAlbumsSql(), new AlbumListExtractor());
        assertEquals(2, actualAlbums.size());
        assertTrue(actualAlbums.contains(updatedFirstAlbum));
        assertTrue(actualAlbums.contains(updatedSecondAlbum));

        List<Track> actualTracks = jdbcTemplate.query(getSelectAllTracksSql(), new TrackListExtractor());
        assertEquals(4, actualTracks.size());
        assertTrue(actualTracks.contains(updatedFirstAlbumFirstTrack));
        assertTrue(actualTracks.contains(updatedFirstAlbumSecondTrack));
        assertTrue(actualTracks.contains(updatedSecondAlbumFirstTrack));
        assertTrue(actualTracks.contains(updatedSecondAlbumSecondTrack));

        clearTestData();
    }

    @Test
    void shouldUpdateTracksWhenSomeTracksAreNew() {

        initialiseTestVariables();
        initialiseInitialTestData();
        initialiseUpdatedTestData();

        List<Track> sameTracksPlusNewTracks = List.of(
                updatedFirstAlbumFirstTrack, updatedFirstAlbumSecondTrack,
                updatedSecondAlbumFirstTrack, updatedSecondAlbumSecondTrack,
                updatedThirdAlbumFirstTrack, updatedThirdAlbumSecondTrack, updatedThirdAlbumThirdTrack,
                updatedFourthAlbumFirstTrack, updatedFourthAlbumSecondTrack);

        trackCollectionDao.updateTrackCollection(sameTracksPlusNewTracks);

        List<Album> actualAlbums = jdbcTemplate.query(getSelectAllAlbumsSql(), new AlbumListExtractor());
        assertEquals(4, actualAlbums.size());
        assertTrue(actualAlbums.contains(updatedFirstAlbum));
        assertTrue(actualAlbums.contains(updatedSecondAlbum));
        assertTrue(actualAlbums.contains(updatedThirdAlbum));

        List<Track> actualTracks = jdbcTemplate.query(getSelectAllTracksSql(), new TrackListExtractor());
        assertEquals(9, actualTracks.size());
        assertTrue(actualTracks.contains(updatedFirstAlbumFirstTrack));
        assertTrue(actualTracks.contains(updatedFirstAlbumSecondTrack));
        assertTrue(actualTracks.contains(updatedSecondAlbumFirstTrack));
        assertTrue(actualTracks.contains(updatedSecondAlbumSecondTrack));
        assertTrue(actualTracks.contains(updatedThirdAlbumFirstTrack));
        assertTrue(actualTracks.contains(updatedThirdAlbumSecondTrack));
        assertTrue(actualTracks.contains(updatedThirdAlbumThirdTrack));
        assertTrue(actualTracks.contains(updatedFourthAlbumFirstTrack));
        assertTrue(actualTracks.contains(updatedFourthAlbumSecondTrack));

        clearTestData();
    }

    private void initialiseTestVariables() {
        // This is not done in a @BeforeEach method
        // because the Extension used here is only called after that,
        // so the data source would not be available yet
        jdbcTemplate = new JdbcTemplate(db.getTestDatabase());
        final NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(db.getTestDatabase());
        trackCollectionDao = new JdbcTrackCollectionDao(
                jdbcTemplate, namedParameterJdbcTemplate);
    }

    private void initialiseInitialTestData() {

        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);

        initialFirstAlbum = new Album(
                UUID.randomUUID().toString(), FIRST_ALBUM_NAME,
                List.of(UUID.randomUUID().toString()), List.of(SOME_ARTIST_NAME), SOME_ARTIST_NAME);
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
                List.of(UUID.randomUUID().toString()), List.of(SOME_OTHER_ARTIST_NAME), SOME_OTHER_ARTIST_NAME);
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
        assertEquals(4, jdbcTemplate.queryForObject(selectTrackCount, Integer.TYPE));
        String selectAlbumCount = "SELECT COUNT(*) FROM " + ALBUM_TABLE_FULL_NAME;
        assertEquals(2, jdbcTemplate.queryForObject(selectAlbumCount, Integer.TYPE));
        String selectArtistCount = "SELECT COUNT(*) FROM " + ARTIST_TABLE_FULL_NAME;
        assertEquals(2, jdbcTemplate.queryForObject(selectArtistCount, Integer.TYPE));
        String selectAlbumArtistCount = "SELECT COUNT(*) FROM " + ALBUM_ARTIST_TABLE_FULL_NAME;
        assertEquals(2, jdbcTemplate.queryForObject(selectAlbumArtistCount, Integer.TYPE));
    }

    private void initialiseUpdatedTestData() {

        updatedFirstAlbum = new Album(
                initialFirstAlbum.getAlbumMbId().toString(), "First Album Name Has Changed",
                List.of(initialFirstAlbum.getArtists().get(0).getArtistMbId().toString()), List.of(initialFirstAlbum.getArtists().get(0).getArtistName()), initialFirstAlbum.getDisplayArtist());
        updatedFirstAlbumFirstTrack = new Track(
                initialFirstAlbumFirstTrack.getTrackMbId().toString(),
                Integer.toString(initialFirstAlbumFirstTrack.getDiscNumber()),
                Integer.toString(initialFirstAlbumFirstTrack.getTrackNumber()),
                initialFirstAlbumFirstTrack.getTrackName(),
                124,
                Path.of(SOME_ARTIST_NAME, "First Album Name Has Changed", FIRST_ALBUM_FIRST_TRACK_NAME + "." + TRACK_FILE_TYPE).toString(),
                updatedFirstAlbum);
        updatedFirstAlbumSecondTrack = new Track(
                initialFirstAlbumSecondTrack.getTrackMbId().toString(), "1", "2",
                "Some Other Track with a different name", 132,
                Path.of(SOME_ARTIST_NAME, "First Album Name Has Changed", "Some Other Track with a different name" + "." + TRACK_FILE_TYPE).toString(),
                updatedFirstAlbum);

        updatedSecondAlbum = new Album(
                initialSecondAlbum.getAlbumMbId().toString(), initialSecondAlbum.getAlbumName(),
                List.of(UUID.randomUUID().toString()), List.of("Some totally different Artist"), "Some totally different Artist");
        updatedSecondAlbumFirstTrack = new Track(
                initialSecondAlbumFirstTrack.getTrackMbId().toString(), "1", "1",
                "Something Else entirely", 111,
                Path.of(SOME_OTHER_ARTIST_NAME, SECOND_ALBUM_NAME, "Something Else entirely" + "." + TRACK_FILE_TYPE).toString(),
                updatedSecondAlbum);
        updatedSecondAlbumSecondTrack = new Track(
                initialSecondAlbumSecondTrack.getTrackMbId().toString(), "1", "4",
                SECOND_ALBUM_SECOND_TRACK_NAME,
                222, Path.of("Some totally different Artist", SECOND_ALBUM_NAME, SECOND_ALBUM_SECOND_TRACK_NAME + "." + TRACK_FILE_TYPE).toString(),
                updatedSecondAlbum);

        var yetAnotherArtistUuid = UUID.randomUUID().toString();
        updatedThirdAlbum = new Album(
                UUID.randomUUID().toString(), THIRD_ALBUM_NAME,
                List.of(yetAnotherArtistUuid), List.of(YET_ANOTHER_ARTIST_NAME), YET_ANOTHER_ARTIST_NAME);
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

        updatedFourthAlbum = new Album(
                UUID.randomUUID().toString(), FOURTH_ALBUM_NAME,
                List.of(yetAnotherArtistUuid, UUID.randomUUID().toString()), List.of(YET_ANOTHER_ARTIST_NAME, AND_NOW_THE_LAST_ARTIST_NAME), YET_ANOTHER_ARTIST_NAME + " & " + AND_NOW_THE_LAST_ARTIST_NAME);
        updatedFourthAlbumFirstTrack = new Track(
                UUID.randomUUID().toString(), "1", "1",
                FOURTH_ALBUM_FIRST_TRACK_NAME, 145,
                Path.of(YET_ANOTHER_ARTIST_NAME + " & " + AND_NOW_THE_LAST_ARTIST_NAME, FOURTH_ALBUM_NAME, FOURTH_ALBUM_FIRST_TRACK_NAME + "." + TRACK_FILE_TYPE).toString(),
                updatedFourthAlbum);
        updatedFourthAlbumSecondTrack = new Track(
                UUID.randomUUID().toString(), "1", "2",
                FOURTH_ALBUM_SECOND_TRACK_NAME, 156,
                Path.of(YET_ANOTHER_ARTIST_NAME + " & " + AND_NOW_THE_LAST_ARTIST_NAME, FOURTH_ALBUM_NAME, FOURTH_ALBUM_SECOND_TRACK_NAME + "." + TRACK_FILE_TYPE).toString(),
                updatedFourthAlbum);
    }

    private void insertAlbum(Album album, Instant instant) {

        SimpleJdbcInsert albumInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withSchemaName(SCHEMA_NAME)
                .withTableName(ALBUM_TABLE_NAME)
                .usingColumns(ALBUM_MBID_COLUMN, ALBUM_NAME_COLUMN, ALBUM_SOURCE_COLUMN, ALBUM_ARTIST_DISPLAY_NAME_COLUMN, LAST_UPDATED_COLUMN);
        MapSqlParameterSource albumParameterSource = new MapSqlParameterSource();
        albumParameterSource.addValue(ALBUM_MBID_COLUMN, album.getAlbumMbId());
        albumParameterSource.addValue(ALBUM_NAME_COLUMN, album.getAlbumName());
        albumParameterSource.addValue(ALBUM_SOURCE_COLUMN, LOCAL_ALBUM_SOURCE);
        albumParameterSource.addValue(ALBUM_ARTIST_DISPLAY_NAME_COLUMN, album.getDisplayArtist());
        albumParameterSource.addValue(LAST_UPDATED_COLUMN, Timestamp.from(instant));

        albumInsert.execute(albumParameterSource);

        IntStream.range(0, album.getArtists().size()).forEach(i -> {
            Artist artist = album.getArtists().get(i);
            SimpleJdbcInsert artistInsert = new SimpleJdbcInsert(jdbcTemplate)
                    .withSchemaName(SCHEMA_NAME)
                    .withTableName(ARTIST_TABLE_NAME)
                    .usingColumns(ARTIST_MBID_COLUMN, ARTIST_NAME_COLUMN, LAST_UPDATED_COLUMN);
            MapSqlParameterSource artistParameterSource = new MapSqlParameterSource();
            artistParameterSource.addValue(ARTIST_MBID_COLUMN, artist.getArtistMbId());
            artistParameterSource.addValue(ARTIST_NAME_COLUMN, artist.getArtistName());
            artistParameterSource.addValue(LAST_UPDATED_COLUMN, Timestamp.from(instant));

            artistInsert.execute(artistParameterSource);

            SimpleJdbcInsert albumArtistInsert = new SimpleJdbcInsert(jdbcTemplate)
                    .withSchemaName(SCHEMA_NAME)
                    .withTableName(ALBUM_ARTIST_TABLE_NAME)
                    .usingColumns(ALBUM_MBID_COLUMN, ARTIST_MBID_COLUMN, ARTIST_POSITION_COLUMN);
            MapSqlParameterSource albumArtistParameterSource = new MapSqlParameterSource();
            albumArtistParameterSource.addValue(ALBUM_MBID_COLUMN, album.getAlbumMbId());
            albumArtistParameterSource.addValue(ARTIST_MBID_COLUMN, artist.getArtistMbId());
            albumArtistParameterSource.addValue(ARTIST_POSITION_COLUMN, i);

            albumArtistInsert.execute(albumArtistParameterSource);
        });
    }

    private void insertTrack(Track track, Instant instant) {
        MapSqlParameterSource trackParameterSource = new MapSqlParameterSource();
        SimpleJdbcInsert trackInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withSchemaName(SCHEMA_NAME)
                .withTableName(TRACK_TABLE_NAME)
                .usingColumns(TRACK_MBID_COLUMN, DISC_NUMBER_COLUMN, TRACK_NUMBER_COLUMN, TRACK_NAME_COLUMN, TRACK_LENGTH_COLUMN, TRACK_FILE_TYPE_COLUMN, TRACK_PATH_COLUMN, ALBUM_MBID_COLUMN, LAST_UPDATED_COLUMN);
        trackParameterSource.addValue(TRACK_MBID_COLUMN, track.getTrackMbId());
        trackParameterSource.addValue(DISC_NUMBER_COLUMN, track.getDiscNumber());
        trackParameterSource.addValue(TRACK_NUMBER_COLUMN, track.getTrackNumber());
        trackParameterSource.addValue(TRACK_NAME_COLUMN, track.getTrackName());
        trackParameterSource.addValue(TRACK_LENGTH_COLUMN, track.getTrackLength());
        trackParameterSource.addValue(TRACK_FILE_TYPE_COLUMN, TRACK_FILE_TYPE);
        trackParameterSource.addValue(TRACK_PATH_COLUMN, track.getTrackRelativePath());
        trackParameterSource.addValue(ALBUM_MBID_COLUMN, track.getAlbum().getAlbumMbId());
        trackParameterSource.addValue(LAST_UPDATED_COLUMN, Timestamp.from(instant));

        trackInsert.execute(trackParameterSource);
    }

    private String getSelectAllAlbumsSql() {
        return "SELECT " +
                "al." + ALBUM_MBID_COLUMN + " AS " + ALBUM_MBID_COLUMN + "," +
                "al." + ALBUM_NAME_COLUMN + " AS " + ALBUM_NAME_COLUMN + "," +
                "al." + ALBUM_ARTIST_DISPLAY_NAME_COLUMN + " AS " + ALBUM_ARTIST_DISPLAY_NAME_COLUMN + "," +
                "ar." + ARTIST_MBID_COLUMN + " AS " + ARTIST_MBID_COLUMN + "," +
                "ar." + ARTIST_NAME_COLUMN + " AS " + ARTIST_NAME_COLUMN + " " +
                "FROM " + ALBUM_TABLE_FULL_NAME + " al" +
                " INNER JOIN " + ALBUM_ARTIST_TABLE_FULL_NAME + " alar ON al." + ALBUM_MBID_COLUMN + " = alar." + ALBUM_MBID_COLUMN +
                " INNER JOIN " + ARTIST_TABLE_FULL_NAME + " ar ON alar." + ARTIST_MBID_COLUMN + " = ar." + ARTIST_MBID_COLUMN;
    }

    private String getSelectAllTracksSql() {
        return "SELECT " +
                "tr." + TRACK_MBID_COLUMN + " AS " + TRACK_MBID_COLUMN + "," +
                "tr." + DISC_NUMBER_COLUMN + " AS " + DISC_NUMBER_COLUMN + "," +
                "tr." + TRACK_NUMBER_COLUMN + " AS " + TRACK_NUMBER_COLUMN + "," +
                "tr." + TRACK_NAME_COLUMN + " AS " + TRACK_NAME_COLUMN + "," +
                "tr." + TRACK_LENGTH_COLUMN + " AS " + TRACK_LENGTH_COLUMN + "," +
                "tr." + TRACK_PATH_COLUMN + " AS " + TRACK_PATH_COLUMN + "," +
                "al." + ALBUM_MBID_COLUMN + " AS " + ALBUM_MBID_COLUMN + "," +
                "al." + ALBUM_NAME_COLUMN + " AS " + ALBUM_NAME_COLUMN + " ," +
                "al." + ALBUM_ARTIST_DISPLAY_NAME_COLUMN + " AS " + ALBUM_ARTIST_DISPLAY_NAME_COLUMN + "," +
                "ar." + ARTIST_MBID_COLUMN + " AS " + ARTIST_MBID_COLUMN + "," +
                "ar." + ARTIST_NAME_COLUMN + " AS " + ARTIST_NAME_COLUMN + " " +
                " FROM " + TRACK_TABLE_FULL_NAME + " tr" +
                " INNER JOIN " + ALBUM_TABLE_FULL_NAME + " al" + " ON tr." + ALBUM_MBID_COLUMN + " = al." + ALBUM_MBID_COLUMN +
                " INNER JOIN " + ALBUM_ARTIST_TABLE_FULL_NAME + " alar ON al." + ALBUM_MBID_COLUMN + " = alar." + ALBUM_MBID_COLUMN +
                " INNER JOIN " + ARTIST_TABLE_FULL_NAME + " ar ON alar." + ARTIST_MBID_COLUMN + " = ar." + ARTIST_MBID_COLUMN +
                " ORDER BY al." + ALBUM_NAME_COLUMN + ",tr." + TRACK_NUMBER_COLUMN + ",alar." + ARTIST_POSITION_COLUMN;
    }

    private void clearTestData() {
        jdbcTemplate.update("DELETE FROM " + TRACK_TABLE_FULL_NAME);
        jdbcTemplate.update("DELETE FROM " + ALBUM_ARTIST_TABLE_FULL_NAME);
        jdbcTemplate.update("DELETE FROM " + ALBUM_TABLE_FULL_NAME);
        jdbcTemplate.update("DELETE FROM " + ARTIST_TABLE_FULL_NAME);
    }

    private class AlbumListExtractor implements ResultSetExtractor<List<Album>> {

        @Override
        public List<Album> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            List<Album> albums = new ArrayList<>();
            UUID currentAlbumMbid = null;
            String currentAlbumName = null;
            String currentArtistDisplayName = null;
            List<Artist> currentArtists = new ArrayList<>();
            while (resultSet.next()) {
                UUID mbid = resultSet.getObject(ALBUM_MBID_COLUMN, UUID.class);
                String name = resultSet.getString(ALBUM_NAME_COLUMN);
                String artistDisplayName = resultSet.getString(ALBUM_ARTIST_DISPLAY_NAME_COLUMN);
                Artist artist = new Artist(
                        resultSet.getObject(ARTIST_MBID_COLUMN, UUID.class),
                        resultSet.getString(ARTIST_NAME_COLUMN));
                if (!mbid.equals(currentAlbumMbid)) {
                    if (currentAlbumMbid != null) { // new object
                        albums.add(new Album(
                                currentAlbumMbid,
                                currentAlbumName,
                                currentArtists,
                                currentArtistDisplayName));
                    }
                    currentAlbumMbid = mbid;
                    currentAlbumName = name;
                    currentArtistDisplayName = artistDisplayName;
                    currentArtists = new ArrayList<>();
                }
                currentArtists.add(artist);
            }
            if (currentAlbumMbid != null) { // last object
                albums.add(new Album(
                        currentAlbumMbid,
                        currentAlbumName,
                        currentArtists,
                        currentArtistDisplayName));
            }

            return albums;
        }
    }

    private class TrackListExtractor implements ResultSetExtractor<List<Track>> {

        @Override
        public List<Track> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            List<Track> tracks = new ArrayList<>();
            UUID currentTrackMbid = null;
            int currentDiscNumber = -1;
            int currentTrackNumber = -1;
            String currentTrackName = null;
            int currentTrackLength = -1;
            Path currentTrackPath = null;
            UUID currentAlbumMbid = null;
            String currentAlbumName = null;
            String currentArtistDisplayName = null;
            List<Artist> currentArtists = new ArrayList<>();
            while (resultSet.next()) {
                UUID trackMbid = resultSet.getObject(TRACK_MBID_COLUMN, UUID.class);
                int discNumber = resultSet.getInt(DISC_NUMBER_COLUMN);
                int trackNumber = resultSet.getInt(TRACK_NUMBER_COLUMN);
                String trackName = resultSet.getString(TRACK_NAME_COLUMN);
                int trackLength = resultSet.getInt(TRACK_LENGTH_COLUMN);
                Path trackPath = Path.of(resultSet.getString(TRACK_PATH_COLUMN));
                UUID albumMbid = resultSet.getObject(ALBUM_MBID_COLUMN, UUID.class);
                String albumName = resultSet.getString(ALBUM_NAME_COLUMN);
                String artistDisplayName = resultSet.getString(ALBUM_ARTIST_DISPLAY_NAME_COLUMN);
                Artist artist = new Artist(
                        resultSet.getObject(ARTIST_MBID_COLUMN, UUID.class),
                        resultSet.getString(ARTIST_NAME_COLUMN));
                if (!trackMbid.equals(currentTrackMbid)) {
                    if (currentTrackMbid != null) { // new object
                        tracks.add(new Track(
                                currentTrackMbid,
                                currentDiscNumber,
                                currentTrackNumber,
                                currentTrackName,
                                currentTrackLength,
                                currentTrackPath.toString(),
                                new Album(currentAlbumMbid,
                                        currentAlbumName,
                                        currentArtists,
                                        currentArtistDisplayName)));
                    }
                    currentTrackMbid = trackMbid;
                    currentDiscNumber = discNumber;
                    currentTrackNumber = trackNumber;
                    currentTrackName = trackName;
                    currentTrackLength = trackLength;
                    currentTrackPath = trackPath;
                    currentAlbumMbid = albumMbid;
                    currentAlbumName = albumName;
                    currentArtistDisplayName = artistDisplayName;
                    currentArtists = new ArrayList<>();
                }
                currentArtists.add(artist);
            }
            if (currentTrackMbid != null) { // last object
                tracks.add(new Track(
                        currentTrackMbid,
                        currentDiscNumber,
                        currentTrackNumber,
                        currentTrackName,
                        currentTrackLength,
                        currentTrackPath.toString(),
                        new Album(
                                currentAlbumMbid,
                                currentAlbumName,
                                currentArtists,
                                currentArtistDisplayName)));
            }

            return tracks;
        }
    }
}