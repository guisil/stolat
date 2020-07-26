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

import java.sql.Timestamp;
import java.time.Instant;
import java.time.MonthDay;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
                            UUID.fromString("d700b3f5-45af-4d02-95ed-57d301bda93e"),
                            "Mogwai"),
                    1999, 3, 29);
    private final AlbumBirthday expectedOpeth =
            new AlbumBirthday(
                    new Album(
                            UUID.fromString("00f78b7d-bd0a-356a-aec4-925e529023f8"),
                            "Ghost Reveries",
                            UUID.fromString("c14b4180-dc87-481e-b17a-64e4150f90f6"),
                            "Opeth"),
                    2005, 8, 26);
    private final AlbumBirthday expectedDeadCombo =
            new AlbumBirthday(
                    new Album(
                            UUID.fromString("4adf1192-df7a-3967-a8e6-d39963c62994"),
                            "Vol. II - Quando a alma não é pequena",
                            UUID.fromString("092ae9e2-60bf-4b66-aa33-9e31754d1924"),
                            "Dead Combo"),
                    2006, 3, 20);
    private final AlbumBirthday expectedAyreon =
            new AlbumBirthday(
                    new Album(
                            UUID.fromString("6281bcfe-058e-4cd3-85bc-66f47c28960b"),
                            "The Theory of Everything",
                            UUID.fromString("7bbfd77c-1102-4831-9ba8-246fb67460b3"),
                            "Ayreon"),
                    2013, 9, 25);
    private final AlbumBirthday expectedIronMaiden =
            new AlbumBirthday(
                    new Album(
                            UUID.fromString("4ebfe175-e7ed-34cd-8e91-67c7e4a53579"),
                            "The Number of the Beast",
                            UUID.fromString("ca891d65-d9b0-4258-89f7-e6ba29d83767"),
                            "Iron Maiden"),
                    1982, 3, 29);

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
        insertTestData();

        albumBirthdayDao.clearAlbumBirthdays();
        String selectBirthdayCount = "SELECT COUNT(*) FROM " + BIRTHDAY_TABLE_FULL_NAME;
        assertEquals(0, jdbcTemplate.queryForObject(selectBirthdayCount, Integer.TYPE));
    }

    @Test
    void shouldPopulateAlbumBirthdays() {

        initialiseTestVariables();

        Set<AlbumBirthday> expectedAlbumBirthdays =
                Set.of(expectedMogwai, expectedOpeth,
                        expectedDeadCombo, expectedAyreon,
                        expectedIronMaiden);

        albumBirthdayDao.populateAlbumBirthdays();

        String selectAllAlbumBirthdays = "SELECT * FROM " + BIRTHDAY_TABLE_FULL_NAME;
        List<AlbumBirthday> actualAlbumBirthdays = jdbcTemplate.query(selectAllAlbumBirthdays, new AlbumBirthdayRowMapper());
        assertEquals(expectedAlbumBirthdays.size(), actualAlbumBirthdays.size());
        assertTrue(actualAlbumBirthdays.containsAll(expectedAlbumBirthdays));
    }

    @Test
    void shouldGetAlbumBirthdaysForSingleDay() {

        initialiseTestVariables();
        insertTestData();

        List<AlbumBirthday> expected =
                List.of(expectedIronMaiden, expectedMogwai);

        List<AlbumBirthday> actual =
                albumBirthdayDao.getAlbumBirthdays(
                        MonthDay.of(3, 29));

        assertEquals(expected, actual);
    }

    @Test
    void shouldGetAlbumBirthdaysForMultipleDays() {

        initialiseTestVariables();
        insertTestData();

        List<AlbumBirthday> expected =
                List.of(expectedDeadCombo, expectedIronMaiden, expectedMogwai);

        List<AlbumBirthday> actual =
                albumBirthdayDao.getAlbumBirthdays(
                        MonthDay.of(3, 20),
                        MonthDay.of(3, 29));

        assertEquals(expected, actual);
    }

    @Test
    void shouldGetAlbumBirthdaysForMultipleDaysInDifferentMonths() {

        initialiseTestVariables();
        insertTestData();

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
        insertTestData();

        List<AlbumBirthday> expected =
                List.of(expectedDeadCombo, expectedIronMaiden, expectedMogwai,
                        expectedOpeth, expectedAyreon);

        List<AlbumBirthday> actual =
                albumBirthdayDao.getAlbumBirthdays(
                        MonthDay.of(1, 1),
                        MonthDay.of(12, 31));

        assertEquals(expected, actual);
    }

    @Test
    void shouldGetAlbumBirthdaysForMultipleDaysInDifferentMonthsCrossingTheEndOfTheYear() {

        initialiseTestVariables();
        insertTestData();

        List<AlbumBirthday> expected =
                List.of(expectedDeadCombo, expectedAyreon);

        List<AlbumBirthday> actual =
                albumBirthdayDao.getAlbumBirthdays(
                        MonthDay.of(9, 20),
                        MonthDay.of(3, 20));

        assertEquals(expected, actual);
    }

    @Test
    void shouldGetAlbumBirthdaysForMultipleDaysInTheSameMonthCrossingTheEndOfTheYear() {

        initialiseTestVariables();
        insertTestData();

        List<AlbumBirthday> expected =
                List.of(expectedDeadCombo, expectedIronMaiden, expectedMogwai,
                        expectedOpeth, expectedAyreon);

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
        jdbcTemplate.update("DELETE FROM " + ALBUM_TABLE_FULL_NAME);
        jdbcTemplate.update("DELETE FROM " + BIRTHDAY_TABLE_FULL_NAME);
    }

    private void insertTestData() {

        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);

        insertBirthday(expectedMogwai, oneHourAgo);
        insertAlbum(expectedMogwai.getAlbum(), oneHourAgo);
        insertBirthday(expectedOpeth, oneHourAgo);
        insertAlbum(expectedOpeth.getAlbum(), oneHourAgo);
        insertBirthday(expectedDeadCombo, oneHourAgo);
        insertAlbum(expectedDeadCombo.getAlbum(), oneHourAgo);
        insertBirthday(expectedAyreon, oneHourAgo);
        insertAlbum(expectedAyreon.getAlbum(), oneHourAgo);
        insertBirthday(expectedIronMaiden, oneHourAgo);
        insertAlbum(expectedIronMaiden.getAlbum(), oneHourAgo);

        String selectBirthdayCount = "SELECT COUNT(*) FROM " + BIRTHDAY_TABLE_FULL_NAME;
        assertEquals(5, jdbcTemplate.queryForObject(selectBirthdayCount, Integer.TYPE));
    }

    private void insertBirthday(AlbumBirthday birthday, Instant instant) {
        SimpleJdbcInsert birthdayInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withSchemaName(SCHEMA_NAME)
                .withTableName(BIRTHDAY_TABLE_NAME)
                .usingColumns(
                        ALBUM_MBID_COLUMN, ALBUM_NAME_COLUMN, ARTIST_MBID_COLUMN, ARTIST_NAME_COLUMN,
                        ALBUM_YEAR_COLUMN, ALBUM_MONTH_COLUMN, ALBUM_DAY_COLUMN, LAST_UPDATED_COLUMN);
        MapSqlParameterSource albumParameterSource = new MapSqlParameterSource();
        albumParameterSource.addValue(ALBUM_MBID_COLUMN, birthday.getAlbum().getAlbumMusicBrainzId());
        albumParameterSource.addValue(ALBUM_NAME_COLUMN, birthday.getAlbum().getAlbumName());
        albumParameterSource.addValue(ARTIST_MBID_COLUMN, birthday.getAlbum().getArtistMusicBrainzId());
        albumParameterSource.addValue(ARTIST_NAME_COLUMN, birthday.getAlbum().getArtistName());
        albumParameterSource.addValue(ALBUM_YEAR_COLUMN, birthday.getAlbumCompleteDate().getYear());
        albumParameterSource.addValue(ALBUM_MONTH_COLUMN, birthday.getAlbumCompleteDate().getMonthValue());
        albumParameterSource.addValue(ALBUM_DAY_COLUMN, birthday.getAlbumCompleteDate().getDayOfMonth());
        albumParameterSource.addValue(LAST_UPDATED_COLUMN, Timestamp.from(instant));

        birthdayInsert.execute(albumParameterSource);
    }

    private void insertAlbum(Album album, Instant instant) {
        SimpleJdbcInsert birthdayInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withSchemaName(SCHEMA_NAME)
                .withTableName(ALBUM_TABLE_NAME)
                .usingColumns(
                        ALBUM_MBID_COLUMN, ALBUM_NAME_COLUMN, ALBUM_SOURCE_COLUMN,
                        ARTIST_MBID_COLUMN, ARTIST_NAME_COLUMN, LAST_UPDATED_COLUMN);
        MapSqlParameterSource albumParameterSource = new MapSqlParameterSource();
        albumParameterSource.addValue(ALBUM_MBID_COLUMN, album.getAlbumMusicBrainzId());
        albumParameterSource.addValue(ALBUM_NAME_COLUMN, album.getAlbumName());
        albumParameterSource.addValue(ALBUM_SOURCE_COLUMN, LOCAL_ALBUM_SOURCE);
        albumParameterSource.addValue(ARTIST_MBID_COLUMN, album.getArtistMusicBrainzId());
        albumParameterSource.addValue(ARTIST_NAME_COLUMN, album.getArtistName());
        albumParameterSource.addValue(LAST_UPDATED_COLUMN, Timestamp.from(instant));

        birthdayInsert.execute(albumParameterSource);
    }
}