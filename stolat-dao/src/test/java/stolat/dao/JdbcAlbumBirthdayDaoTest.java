package stolat.dao;

import io.zonky.test.db.postgres.junit5.EmbeddedPostgresExtension;
import io.zonky.test.db.postgres.junit5.PreparedDbExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import stolat.model.Album;
import stolat.model.AlbumBirthday;
import stolat.model.Artist;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.MonthDay;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static stolat.dao.StolatDatabaseConstants.*;

class JdbcAlbumBirthdayDaoTest {

    @RegisterExtension
    public static PreparedDbExtension db =
            EmbeddedPostgresExtension.preparedDatabase(
                    ConfiguredFlywayPreparer.forClasspathLocationAndSchemas(
                            List.of("db/migration"),
                            List.of("stolat", "musicbrainz"),
                            "stolat"));

    private final AlbumBirthday expectedMogwai =
            new AlbumBirthday(
                    new Album(
                            UUID.fromString("68b9f75b-34b5-3228-9972-82efea767eca"),
                            "Come On Die Young",
                            List.of(new Artist(UUID.fromString("d700b3f5-45af-4d02-95ed-57d301bda93e"),
                                    "Mogwai")), "Mogwai"),
                    1999, 3, 29);
    private final AlbumBirthday expectedOpeth =
            new AlbumBirthday(
                    new Album(
                            UUID.fromString("00f78b7d-bd0a-356a-aec4-925e529023f8"),
                            "Ghost Reveries",
                            List.of(new Artist(UUID.fromString("c14b4180-dc87-481e-b17a-64e4150f90f6"),
                                    "Opeth")), "Opeth"),
                    2005, 8, 26);
    private final AlbumBirthday expectedDeadCombo =
            new AlbumBirthday(
                    new Album(
                            UUID.fromString("4adf1192-df7a-3967-a8e6-d39963c62994"),
                            "Vol. II - Quando a alma não é pequena",
                            List.of(new Artist(UUID.fromString("092ae9e2-60bf-4b66-aa33-9e31754d1924"),
                                    "Dead Combo")), "Dead Combo"),
                    2006, 3, 20);
    private final AlbumBirthday expectedAyreon =
            new AlbumBirthday(
                    new Album(
                            UUID.fromString("6281bcfe-058e-4cd3-85bc-66f47c28960b"),
                            "The Theory of Everything",
                            List.of(new Artist(UUID.fromString("7bbfd77c-1102-4831-9ba8-246fb67460b3"),
                                    "Ayreon")), "Ayreon"),
                    2013, 9, 25);
    private final AlbumBirthday expectedIronMaiden =
            new AlbumBirthday(
                    new Album(
                            UUID.fromString("4ebfe175-e7ed-34cd-8e91-67c7e4a53579"),
                            "The Number of the Beast",
                            List.of(new Artist(UUID.fromString("ca891d65-d9b0-4258-89f7-e6ba29d83767"),
                                    "Iron Maiden")), "Iron Maiden"),
                    1982, 3, 29);
    private final AlbumBirthday expectedEels =
            new AlbumBirthday(
                    new Album(
                            UUID.fromString("119a9488-9980-3645-be68-f50210a35a26"),
                            "Beautiful Freak",
                            List.of(new Artist(UUID.fromString("14387b0f-765c-4852-852f-135335790466"),
                                    "EELS")), "EELS"),
                    1996, 8, 5);
    private final AlbumBirthday expectedRodrigoLeao =
            new AlbumBirthday(
                    new Album(
                            UUID.fromString("8ecbcc35-69eb-4233-af8a-a351eb3cadb6"),
                            "O retiro",
                            List.of(new Artist(UUID.fromString("f3df0b46-d21a-4230-9a94-4082e8d6860b"),
                                            "Rodrigo Leão"),
                                    new Artist(UUID.fromString("925842f9-d48a-42d9-9597-d99a775ac45b"),
                                            "Orquestra Gulbenkian"),
                                    new Artist(UUID.fromString("6c245d32-4efc-4aaa-9ef2-0f355a323088"),
                                            "Coro Gulbenkian")), "Rodrigo Leão, Orquestra Gulbenkian & Coro Gulbenkian"),
                    2015, 10, 30);

    private JdbcTemplate jdbcTemplate;

    private AlbumBirthdayDao albumBirthdayDao;

    @AfterEach
    void tearDown() {
        clearTestData();
    }

    @Test
    void shouldClearAlbumBirthdays() {
        initialiseTestVariables();
        // the album birthdays need to be filled in first
        insertTestData(true);

        albumBirthdayDao.clearAlbumBirthdays();
        String selectBirthdayCount = "SELECT COUNT(*) FROM " + BIRTHDAY_TABLE_FULL_NAME;
        assertEquals(0, jdbcTemplate.queryForObject(selectBirthdayCount, Integer.TYPE));
        String selectBirthdayIntermediateCount = "SELECT COUNT(*) FROM " + BIRTHDAY_TABLE_INTERMEDIATE_FULL_NAME;
        assertEquals(0, jdbcTemplate.queryForObject(selectBirthdayIntermediateCount, Integer.TYPE));
    }

    @Test
    void shouldPopulateAlbumBirthdays() {

        initialiseTestVariables();
        insertTestData(false);

        Set<AlbumBirthday> expectedAlbumBirthdays =
                Set.of(expectedMogwai, expectedOpeth,
                        expectedDeadCombo, expectedAyreon,
                        expectedIronMaiden, expectedEels, expectedRodrigoLeao);

        albumBirthdayDao.populateAlbumBirthdays();

        String selectAllAlbumBirthdays = "SELECT " +
                "al." + ALBUM_MBID_COLUMN + " AS " + ALBUM_MBID_COLUMN + "," +
                "al." + ALBUM_NAME_COLUMN + " AS " + ALBUM_NAME_COLUMN + "," +
                "al." + ALBUM_ARTIST_DISPLAY_NAME_COLUMN + " AS " + ALBUM_ARTIST_DISPLAY_NAME_COLUMN + "," +
                "ar." + ARTIST_MBID_COLUMN + " AS " + ARTIST_MBID_COLUMN + "," +
                "ar." + ARTIST_NAME_COLUMN + " AS " + ARTIST_NAME_COLUMN + "," +
                "b." + ALBUM_YEAR_COLUMN + " AS " + ALBUM_YEAR_COLUMN + "," +
                "b." + ALBUM_MONTH_COLUMN + " AS " + ALBUM_MONTH_COLUMN + "," +
                "b." + ALBUM_DAY_COLUMN + " AS " + ALBUM_DAY_COLUMN +
                " FROM " + BIRTHDAY_TABLE_FULL_NAME + " b" +
                " INNER JOIN " + ALBUM_TABLE_FULL_NAME + " al" + " ON " + " b." + ALBUM_MBID_COLUMN + " = " + "al." + ALBUM_MBID_COLUMN +
                " INNER JOIN " + ALBUM_ARTIST_TABLE_FULL_NAME + " alar ON al." + ALBUM_MBID_COLUMN + " = alar." + ALBUM_MBID_COLUMN +
                " INNER JOIN " + ARTIST_TABLE_FULL_NAME + " ar ON alar." + ARTIST_MBID_COLUMN + " = ar." + ARTIST_MBID_COLUMN;
        List<AlbumBirthday> actualAlbumBirthdays = jdbcTemplate.query(selectAllAlbumBirthdays, new AlbumBirthdayListExtractor());
        assertEquals(expectedAlbumBirthdays.size(), actualAlbumBirthdays.size());
        assertTrue(actualAlbumBirthdays.containsAll(expectedAlbumBirthdays));

        String selectBirthdayIntermediateCount = "SELECT COUNT(*) FROM " + BIRTHDAY_TABLE_INTERMEDIATE_FULL_NAME;
        assertNotEquals(0, jdbcTemplate.queryForObject(selectBirthdayIntermediateCount, Integer.TYPE));
    }

    @Test
    void shouldGetAlbumBirthdaysForSingleDay() {

        initialiseTestVariables();
        insertTestData(true);

        List<AlbumBirthday> expected =
                List.of(expectedMogwai, expectedIronMaiden);

        List<AlbumBirthday> actual =
                albumBirthdayDao.getAlbumBirthdays(
                        MonthDay.of(3, 29));

        assertEquals(expected, actual);
    }

    @Test
    void shouldGetAlbumBirthdaysForMultipleDays() {

        initialiseTestVariables();
        insertTestData(true);

        List<AlbumBirthday> expected =
                List.of(expectedDeadCombo, expectedMogwai, expectedIronMaiden);

        List<AlbumBirthday> actual =
                albumBirthdayDao.getAlbumBirthdays(
                        MonthDay.of(3, 20),
                        MonthDay.of(3, 29));

        assertEquals(expected, actual);
    }

    @Test
    void shouldGetAlbumBirthdaysForMultipleDaysInDifferentMonths() {

        initialiseTestVariables();
        insertTestData(true);

        List<AlbumBirthday> expected =
                List.of(expectedOpeth, expectedAyreon);

        List<AlbumBirthday> actual =
                albumBirthdayDao.getAlbumBirthdays(
                        MonthDay.of(8, 26),
                        MonthDay.of(9, 26));

        assertEquals(expected, actual);
    }

    @Test
    void shouldGetAlbumBirthdaysForWholeYear() {

        initialiseTestVariables();
        insertTestData(true);

        List<AlbumBirthday> expected =
                List.of(expectedDeadCombo, expectedMogwai, expectedIronMaiden,
                        expectedEels, expectedOpeth, expectedAyreon, expectedRodrigoLeao);

        List<AlbumBirthday> actual =
                albumBirthdayDao.getAlbumBirthdays(
                        MonthDay.of(1, 1),
                        MonthDay.of(12, 31));

        assertEquals(expected, actual);
    }

    @Test
    void shouldGetAlbumBirthdaysForMultipleDaysInDifferentMonthsCrossingTheEndOfTheYear() {

        initialiseTestVariables();
        insertTestData(true);

        List<AlbumBirthday> expected =
                List.of(expectedDeadCombo, expectedAyreon, expectedRodrigoLeao);

        List<AlbumBirthday> actual =
                albumBirthdayDao.getAlbumBirthdays(
                        MonthDay.of(9, 20),
                        MonthDay.of(3, 20));

        assertEquals(expected, actual);
    }

    @Test
    void shouldGetAlbumBirthdaysForMultipleDaysInTheSameMonthCrossingTheEndOfTheYear() {

        initialiseTestVariables();
        insertTestData(true);

        List<AlbumBirthday> expected =
                List.of(expectedDeadCombo, expectedMogwai, expectedIronMaiden,
                        expectedEels, expectedOpeth, expectedAyreon, expectedRodrigoLeao);

        List<AlbumBirthday> actual =
                albumBirthdayDao.getAlbumBirthdays(
                        MonthDay.of(2, 1),
                        MonthDay.of(1, 1));

        assertEquals(expected, actual);
    }

    private void initialiseTestVariables() {
        // This is not done in a @BeforeEach method
        // because the Extension used here is only called after that,
        // so the data source would not be available yet
        jdbcTemplate = new JdbcTemplate(db.getTestDatabase());
        albumBirthdayDao = new JdbcAlbumBirthdayDao(jdbcTemplate);
    }

    private void clearTestData() {
        jdbcTemplate.update("DELETE FROM " + ALBUM_ARTIST_TABLE_FULL_NAME);
        jdbcTemplate.update("DELETE FROM " + ARTIST_TABLE_FULL_NAME);
        jdbcTemplate.update("DELETE FROM " + ALBUM_TABLE_FULL_NAME);
        jdbcTemplate.update("DELETE FROM " + BIRTHDAY_TABLE_FULL_NAME);
    }

    private void insertTestData(boolean insertBirthdays) {

        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        if (insertBirthdays) {
            insertBirthday(expectedMogwai, oneHourAgo);
            insertBirthday(expectedOpeth, oneHourAgo);
            insertBirthday(expectedDeadCombo, oneHourAgo);
            insertBirthday(expectedAyreon, oneHourAgo);
            insertBirthday(expectedIronMaiden, oneHourAgo);
            insertBirthday(expectedEels, oneHourAgo);
            insertBirthday(expectedRodrigoLeao, oneHourAgo);
            String selectBirthdayCount = "SELECT COUNT(*) FROM " + BIRTHDAY_TABLE_FULL_NAME;
            assertEquals(7, jdbcTemplate.queryForObject(selectBirthdayCount, Integer.TYPE));
        }
        insertAlbum(expectedMogwai.getAlbum(), oneHourAgo);
        insertAlbum(expectedOpeth.getAlbum(), oneHourAgo);
        insertAlbum(expectedDeadCombo.getAlbum(), oneHourAgo);
        insertAlbum(expectedAyreon.getAlbum(), oneHourAgo);
        insertAlbum(expectedIronMaiden.getAlbum(), oneHourAgo);
        insertAlbum(expectedEels.getAlbum(), oneHourAgo);
        insertAlbum(expectedRodrigoLeao.getAlbum(), oneHourAgo);
        String selectAlbumCount = "SELECT COUNT(*) FROM " + ALBUM_TABLE_FULL_NAME;
        assertEquals(7, jdbcTemplate.queryForObject(selectAlbumCount, Integer.TYPE));
    }

    private void insertBirthday(AlbumBirthday birthday, Instant instant) {
        SimpleJdbcInsert birthdayInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withSchemaName(SCHEMA_NAME)
                .withTableName(BIRTHDAY_TABLE_NAME)
                .usingColumns(
                        ALBUM_MBID_COLUMN,
                        ALBUM_YEAR_COLUMN, ALBUM_MONTH_COLUMN, ALBUM_DAY_COLUMN, LAST_UPDATED_COLUMN);
        MapSqlParameterSource albumParameterSource = new MapSqlParameterSource();
        albumParameterSource.addValue(ALBUM_MBID_COLUMN, birthday.getAlbum().getAlbumMbId());
        albumParameterSource.addValue(ALBUM_YEAR_COLUMN, birthday.getAlbumYear());
        albumParameterSource.addValue(ALBUM_MONTH_COLUMN, birthday.getAlbumMonth());
        albumParameterSource.addValue(ALBUM_DAY_COLUMN, birthday.getAlbumDay());
        albumParameterSource.addValue(LAST_UPDATED_COLUMN, Timestamp.from(instant));

        birthdayInsert.execute(albumParameterSource);
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
}