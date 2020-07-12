package stolat.bootstrap.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import stolat.bootstrap.sql.SqlProperties;
import stolat.bootstrap.sql.SqlScriptRunner;

import java.sql.SQLException;

@Repository
@Slf4j
public class SqlScriptAlbumBirthdayDao implements AlbumBirthdayDao {

    @Autowired
    private SqlProperties sqlProperties;

    @Autowired
    private SqlScriptRunner sqlScriptRunner;

    @Override
    public void clearAlbumBirthdays() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public void populateAlbumBirthdays() {
        String populateAlbumBirthdayScript = sqlProperties.getPopulateAlbumBirthdayScript();
        try {
            sqlScriptRunner.runSqlScript(populateAlbumBirthdayScript);
        } catch (SQLException ex) {
            log.error("Error occurred when running SQL script {}", populateAlbumBirthdayScript, ex);
        }
    }
}
