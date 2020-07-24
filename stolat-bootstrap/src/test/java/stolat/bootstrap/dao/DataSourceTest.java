package stolat.bootstrap.dao;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.flywaydb.core.Flyway;
import org.flywaydb.test.annotation.FlywayTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import stolat.dao.AlbumBirthdayDao;
import stolat.dao.TrackCollectionDao;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@FlywayTest
@AutoConfigureEmbeddedDatabase(beanName = "dataSource")
@Import({DataSourceTestConfiguration.class})
public class DataSourceTest {

    @Autowired
    private DataSource dataSource;
    @Autowired
    private Flyway flyway;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AlbumBirthdayDao albumBirthdayDao;
    @Autowired
    private TrackCollectionDao trackCollectionDao;

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
