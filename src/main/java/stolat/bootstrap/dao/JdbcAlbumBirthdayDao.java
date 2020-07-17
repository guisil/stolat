package stolat.bootstrap.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import static stolat.bootstrap.dao.StolatDatabaseConstants.ALBUM_TABLE_FULL_NAME;
import static stolat.bootstrap.dao.StolatDatabaseConstants.BIRTHDAY_TABLE_FULL_NAME;

@Profile("jdbc")
@Repository
@Slf4j
public class JdbcAlbumBirthdayDao implements AlbumBirthdayDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public void clearAlbumBirthdays() {
        log.info("Clearing album birthdays");
        jdbcTemplate.execute("TRUNCATE TABLE " + BIRTHDAY_TABLE_FULL_NAME);
        log.info("Cleared album birthdays");
    }

    @Override
    public void populateAlbumBirthdays() {
        throw new UnsupportedOperationException("not implemented yet");
    }
}
