package stolat.bootstrap.filesystem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = FileSystemProperties.class)
@TestPropertySource("classpath:test-application.properties")
class FileSystemPropertiesTest {

    @Autowired
    private FileSystemProperties fileSystemProperties;

    @Test
    void shouldGetAlbumCollectionPath() {
        assertEquals(
                "testpath",
                fileSystemProperties.getAlbumCollectionPath());
    }

    @Test
    void shouldGetMusicFileExtensions() {
        assertEquals(List.of("flac", "ogg"), fileSystemProperties.getMusicFileExtensions());
    }
}