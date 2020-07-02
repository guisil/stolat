package stolat.bootstrap;

import org.flywaydb.test.FlywayTestExecutionListener;
import org.flywaydb.test.annotation.FlywayTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

import javax.sql.DataSource;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@SpringBootTest
@AutoConfigureEmbeddedDatabase
@TestPropertySource("classpath:test-application.properties")
public class DataSourcePropertiesTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldLoadContext() {
        assertNotNull(dataSource);
        assertNotNull(jdbcTemplate);
    }

    @Test
    @FlywayTest
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
