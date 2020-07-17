package stolat.bootstrap.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import stolat.bootstrap.sql.SqlProperties;
import stolat.bootstrap.sql.SqlScriptRunner;

import java.sql.SQLException;

@Profile("sql_scripts")
@Repository
@Slf4j
public class SqlScriptAlbumBirthdayDao implements AlbumBirthdayDao {

    @Autowired
    private SqlProperties sqlProperties;

    @Autowired
    private SqlScriptRunner sqlScriptRunner;

    @Override
    public void clearAlbumBirthdays() {
        final var clearAlbumBirthdayScript = sqlProperties.getClearAlbumBirthdayScript();
        log.info("Executing SQL script ({}) for clearing album birthdays", clearAlbumBirthdayScript);
        executeSqlScript(clearAlbumBirthdayScript);
    }

    @Override
    public void populateAlbumBirthdays() {
        final var populateAlbumBirthdayScript = sqlProperties.getPopulateAlbumBirthdayScript();
        log.info("Executing SQL script ({}) for populating album birthdays", populateAlbumBirthdayScript);
        executeSqlScript(populateAlbumBirthdayScript);
    }

    private void executeSqlScript(String populateAlbumBirthdayScript) {
        try {
            sqlScriptRunner.runSqlScript(populateAlbumBirthdayScript);
        } catch (SQLException ex) {
            log.error("Error occurred when running SQL script {}", populateAlbumBirthdayScript, ex);
        }
    }
}
