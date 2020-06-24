package stolat.bootstrap.filesystem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import stolat.bootstrap.sql.SqlProperties;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = FileSystemProperties.class)
@TestPropertySource("classpath:test-application.properties")
class FileSystemPropertiesTest {

    @Autowired
    private FileSystemProperties fileSystemProperties;

    @Test
    void shouldGetAlbumCollectionPath() {
        assertEquals(
                "/path/to/music/collection",
                fileSystemProperties.getAlbumCollectionPath());
    }
}