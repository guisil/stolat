package stolat.dao;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static stolat.dao.StolatDatabaseConstants.BIRTHDAY_TABLE_FULL_NAME;

@Profile("jdbc")
@Repository
@Slf4j
public class JdbcAlbumBirthdayDao implements AlbumBirthdayDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcAlbumBirthdayDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void clearAlbumBirthdays() {
        log.info("Clearing album birthdays");
        jdbcTemplate.update("TRUNCATE TABLE " + BIRTHDAY_TABLE_FULL_NAME);
    }

    @Override
    public void populateAlbumBirthdays() {
        log.info("Populating album birthdays");
        try {
            InputStream sqlFileInputStream =
                    new ClassPathResource(DaoConstants.POPULATE_ALBUM_BIRTHDAY_SCRIPT, getClass())
                            .getInputStream();
            String sql =
                    FileCopyUtils.copyToString(
                            new InputStreamReader(sqlFileInputStream));
            jdbcTemplate.update(sql);
        } catch (IOException ex) {
            log.error("Error reading SQL script file for populating the album birthdays", ex);
        }
    }
}
