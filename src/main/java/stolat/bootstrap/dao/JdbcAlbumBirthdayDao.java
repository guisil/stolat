package stolat.bootstrap.dao;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import stolat.bootstrap.sql.SqlProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static stolat.bootstrap.dao.StolatDatabaseConstants.BIRTHDAY_TABLE_FULL_NAME;

@Profile("jdbc")
@Repository
@AllArgsConstructor
@Slf4j
public class JdbcAlbumBirthdayDao implements AlbumBirthdayDao {

    private SqlProperties sqlProperties;
    private JdbcTemplate jdbcTemplate;

    @Override
    public void clearAlbumBirthdays() {
        log.info("Clearing album birthdays");
        jdbcTemplate.update("TRUNCATE TABLE " + BIRTHDAY_TABLE_FULL_NAME);
    }

    @Override
    public void populateAlbumBirthdays() {
        log.info("Populating album birthdays");
        String sql = null;
        try {
            File sqlFile = new ClassPathResource(
                    sqlProperties.getPopulateAlbumBirthdayScript(),
                    getClass()).getFile();
            jdbcTemplate.update(Files.readString(sqlFile.toPath()));
        } catch (IOException ex) {
            log.error("Error reading SQL script file for populating the album birthdays", ex);
        }
    }
}
