package stolat.service.dao;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import stolat.dao.AlbumBirthdayDao;
import stolat.dao.JdbcAlbumBirthdayDao;
import stolat.dao.JdbcTrackCollectionDao;
import stolat.dao.TrackCollectionDao;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureEmbeddedDatabase
@TestPropertySource("classpath:test-application.properties")
public class DataSourceTest {

    @Autowired
    private DataSource dataSource;
    @Autowired
    private Flyway flyway;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private AlbumBirthdayDao albumBirthdayDao;
    private TrackCollectionDao trackCollectionDao;

    @BeforeEach
    void setUp() {
        albumBirthdayDao = new JdbcAlbumBirthdayDao(jdbcTemplate);
        trackCollectionDao = new JdbcTrackCollectionDao(jdbcTemplate, namedParameterJdbcTemplate);
    }

    @Test
    void shouldLoadContext() {
        assertNotNull(dataSource);
        assertNotNull(jdbcTemplate);
    }

    @Test
    void shouldCreateAlbumBirthdayTable() {
        String count = "SELECT COUNT(*) FROM stolat.album_birthday";
        assertEquals(0, jdbcTemplate.queryForObject(count, Integer.TYPE));
    }

    @Test
    void shouldCreateLocalCollectionAlbumTable() {
        String count = "SELECT COUNT(*) FROM stolat.local_collection_album";
        assertEquals(0, jdbcTemplate.queryForObject(count, Integer.TYPE));
    }

    @Test
    void shouldCreateLocalCollectionTrackTable() {
        String count = "SELECT COUNT(*) FROM stolat.local_collection_track";
        assertEquals(0, jdbcTemplate.queryForObject(count, Integer.TYPE));
    }
}
