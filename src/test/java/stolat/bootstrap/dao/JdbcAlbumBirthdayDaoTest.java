package stolat.bootstrap.dao;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.test.context.TestPropertySource;
import stolat.bootstrap.model.Album;
import stolat.bootstrap.model.AlbumBirthday;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static stolat.bootstrap.dao.StolatDatabaseConstants.*;

@SpringBootTest
@AutoConfigureEmbeddedDatabase
@TestPropertySource("classpath:test-application.properties")
class JdbcAlbumBirthdayDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcAlbumBirthdayDao albumBirthdayDao;

    @BeforeEach
    void setUp() {
        initialiseInitialTestData();
        initialiseUpdatedTestData();
    }


    private void initialiseInitialTestData() {

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

    private void initialiseUpdatedTestData() {

        //TODO ...
    }

    private void insertBirthday(AlbumBirthday birthday, Instant instant) {
        SimpleJdbcInsert birthdayInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withSchemaName(SCHEMA_NAME)
                .withTableName(BIRTHDAY_TABLE_NAME)
                .usingColumns(ALBUM_MBID_COLUMN, ARTIST_MBID_COLUMN, ALBUM_YEAR_COLUMN, ALBUM_MONTH_COLUMN, ALBUM_DAY_COLUMN, LAST_UPDATED_COLUMN);
        MapSqlParameterSource albumParameterSource = new MapSqlParameterSource();
        albumParameterSource.addValue(ALBUM_MBID_COLUMN, birthday.getAlbumMusicBrainzId());
        albumParameterSource.addValue(ARTIST_MBID_COLUMN, birthday.getArtistMusicBrainzId());
        albumParameterSource.addValue(ALBUM_YEAR_COLUMN, birthday.getAlbumYear());
        albumParameterSource.addValue(ALBUM_MONTH_COLUMN, birthday.getAlbumMonth());
        albumParameterSource.addValue(ALBUM_DAY_COLUMN, birthday.getAlbumDay());
        albumParameterSource.addValue(LAST_UPDATED_COLUMN, Timestamp.from(instant));

        birthdayInsert.execute(albumParameterSource);
    }

    @AfterEach
    void tearDown() {
        String deleteBirthdays = "DELETE FROM " + BIRTHDAY_TABLE_FULL_NAME;
        jdbcTemplate.execute(deleteBirthdays);
    }

    @Test
    void shouldClearAlbumBirthdays() {
        albumBirthdayDao.clearAlbumBirthdays();
        String selectBirthdayCount = "SELECT COUNT(*) FROM " + BIRTHDAY_TABLE_FULL_NAME;
        assertEquals(0, jdbcTemplate.queryForObject(selectBirthdayCount, Integer.TYPE));
    }

    @Test
    void shouldPopulateAlbumBirthdays() {
        fail("not tested yet");
    }
}