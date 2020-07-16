package stolat.bootstrap.sql;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = SqlProperties.class)
@TestPropertySource("classpath:test-application.properties")
class SqlPropertiesTest {

    @Autowired
    private SqlProperties sqlProperties;

    @Test
    void shouldGetClearAlbumBirthdayScript() {
        assertEquals("album_birthday_script0.sql",
                sqlProperties.getClearAlbumBirthdayScript());
    }

    @Test
    void shouldGetPopulateAlbumBirthdayScript() {
        assertEquals("album_birthday_script1.sql",
                sqlProperties.getPopulateAlbumBirthdayScript());
    }
}