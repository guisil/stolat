package stolat.bootstrap.dao;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.flywaydb.test.annotation.FlywayTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import stolat.bootstrap.model.AlbumBirthday;
import stolat.bootstrap.sql.SqlProperties;

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
import static org.mockito.Mockito.when;
import static stolat.bootstrap.dao.StolatDatabaseConstants.*;

@ExtendWith(SpringExtension.class)
@FlywayTest
@AutoConfigureEmbeddedDatabase(beanName = "dataSource")
@Import(DataSourceTestConfiguration.class)
class JdbcAlbumBirthdayDaoTest {

    @MockBean
    private SqlProperties mockSqlProperties;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcAlbumBirthdayDao albumBirthdayDao;

    @BeforeEach
    void setUp() {
        when(mockSqlProperties.getClearAlbumBirthdayScript())
                .thenReturn("clear_album_birthday_table.sql");
        when(mockSqlProperties.getPopulateAlbumBirthdayScript())
                .thenReturn("populate_album_birthday_table.sql");
        albumBirthdayDao = new JdbcAlbumBirthdayDao(mockSqlProperties, jdbcTemplate);
        String deleteBirthdays = "TRUNCATE TABLE " + BIRTHDAY_TABLE_FULL_NAME;
        jdbcTemplate.execute(deleteBirthdays);
    }

    @Test
    void shouldClearAlbumBirthdays() {
        insertSomeTestData();
        albumBirthdayDao.clearAlbumBirthdays();
        String selectBirthdayCount = "SELECT COUNT(*) FROM " + BIRTHDAY_TABLE_FULL_NAME;
        assertEquals(0, jdbcTemplate.queryForObject(selectBirthdayCount, Integer.TYPE));
    }

    private void insertSomeTestData() {

        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);

        AlbumBirthday firstBirthday = new AlbumBirthday(UUID.randomUUID(), UUID.randomUUID(), 2000, 12, 22);
        AlbumBirthday secondBirthday = new AlbumBirthday(UUID.randomUUID(), UUID.randomUUID(), 1979, 8, 12);
        AlbumBirthday thirdBirthday = new AlbumBirthday(UUID.randomUUID(), UUID.randomUUID(), 1995, 2, 9);
        AlbumBirthday fourthBirthday = new AlbumBirthday(UUID.randomUUID(), UUID.randomUUID(), 1982, 4, 30);

        insertBirthday(firstBirthday, oneHourAgo);
        insertBirthday(secondBirthday, oneHourAgo);
        insertBirthday(thirdBirthday, oneHourAgo);
        insertBirthday(fourthBirthday, oneHourAgo);

        String selectBirthdayCount = "SELECT COUNT(*) FROM " + BIRTHDAY_TABLE_FULL_NAME;
        assertEquals(4, jdbcTemplate.queryForObject(selectBirthdayCount, Integer.TYPE));
    }

    private void insertBirthday(AlbumBirthday birthday, Instant instant) {
        SimpleJdbcInsert birthdayInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withSchemaName(SCHEMA_NAME)
                .withTableName(BIRTHDAY_TABLE_NAME)
                .usingColumns(ALBUM_MBID_COLUMN, ARTIST_MBID_COLUMN, ALBUM_YEAR_COLUMN, ALBUM_MONTH_COLUMN, ALBUM_DAY_COLUMN, LAST_UPDATED_COLUMN);
        MapSqlParameterSource albumParameterSource = new MapSqlParameterSource();
        albumParameterSource.addValue(ALBUM_MBID_COLUMN, birthday.getAlbumMusicBrainzId());
        albumParameterSource.addValue(ARTIST_MBID_COLUMN, birthday.getArtistMusicBrainzId());
        albumParameterSource.addValue(ALBUM_YEAR_COLUMN, birthday.getAlbumCompleteDate().getYear());
        albumParameterSource.addValue(ALBUM_MONTH_COLUMN, birthday.getAlbumCompleteDate().getMonthValue());
        albumParameterSource.addValue(ALBUM_DAY_COLUMN, birthday.getAlbumCompleteDate().getDayOfMonth());
        albumParameterSource.addValue(LAST_UPDATED_COLUMN, Timestamp.from(instant));

        birthdayInsert.execute(albumParameterSource);
    }

    @Test
    void shouldPopulateAlbumBirthdays() {

        AlbumBirthday expectedMogwai =
                new AlbumBirthday(
                        UUID.fromString("68b9f75b-34b5-3228-9972-82efea767eca"),
                        UUID.fromString("d700b3f5-45af-4d02-95ed-57d301bda93e"),
                        1999, 3, 29);
        AlbumBirthday expectedOpeth =
                new AlbumBirthday(
                        UUID.fromString("00f78b7d-bd0a-356a-aec4-925e529023f8"),
                        UUID.fromString("c14b4180-dc87-481e-b17a-64e4150f90f6"),
                        2005, 8, 26);
        AlbumBirthday expectedDeadCombo =
                new AlbumBirthday(
                        UUID.fromString("4adf1192-df7a-3967-a8e6-d39963c62994"),
                        UUID.fromString("092ae9e2-60bf-4b66-aa33-9e31754d1924"),
                        2006, 3, 20);
        AlbumBirthday expectedAyreon =
                new AlbumBirthday(
                        UUID.fromString("6281bcfe-058e-4cd3-85bc-66f47c28960b"),
                        UUID.fromString("7bbfd77c-1102-4831-9ba8-246fb67460b3"),
                        2013, 9, 25);
        Set<AlbumBirthday> expectedAlbumBirthdays =
                Set.of(expectedMogwai, expectedOpeth, expectedDeadCombo, expectedAyreon);

        albumBirthdayDao.populateAlbumBirthdays();

        String selectAllAlbumBirthdays = "SELECT * FROM " + BIRTHDAY_TABLE_FULL_NAME;
        List<AlbumBirthday> actualAlbumBirthdays = jdbcTemplate.query(selectAllAlbumBirthdays, new AlbumBirthdayRowMapper());
        assertEquals(4, actualAlbumBirthdays.size());
        assertTrue(actualAlbumBirthdays.containsAll(expectedAlbumBirthdays));
    }

    private class AlbumBirthdayRowMapper implements RowMapper<AlbumBirthday> {

        @Override
        public AlbumBirthday mapRow(ResultSet resultSet, int i) throws SQLException {
            final UUID albumMbid = resultSet.getObject(ALBUM_MBID_COLUMN, UUID.class);
            final UUID artistMbid = resultSet.getObject(ARTIST_MBID_COLUMN, UUID.class);
            final int albumYear = resultSet.getInt(ALBUM_YEAR_COLUMN);
            final int albumMonth = resultSet.getInt(ALBUM_MONTH_COLUMN);
            final int albumDay = resultSet.getInt(ALBUM_DAY_COLUMN);
            return new AlbumBirthday(
                    albumMbid, artistMbid,
                    albumYear, albumMonth, albumDay);
        }
    }
}