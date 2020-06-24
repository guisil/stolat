package stolat.bootstrap.sql;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = SqlProperties.class)
@TestPropertySource("classpath:test-application.properties")
class SqlPropertiesTest {

    @Autowired
    private SqlProperties sqlProperties;

    @Test
    void shouldGetAlbumBirthdayScripts() {
        assertEquals(
                List.of("album_birthday_script1.sql", "album_birthday_script2.sql", "album_birthday_script3.sql"),
                sqlProperties.getAlbumBirthdayScripts());
    }

    @Test
    void getAlbumCollectionScripts() {
        assertEquals(
                List.of("album_collection_script1.sql"),
                sqlProperties.getAlbumCollectionScripts());
    }
}