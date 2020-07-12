package stolat.bootstrap.dao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stolat.bootstrap.sql.SqlProperties;
import stolat.bootstrap.sql.SqlScriptRunner;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SqlScriptAlbumBirthdayDaoTest {

    private static final String POPULATE_ALBUM_BIRTHDAY_SCRIPT = "populate_album_birthday_script.sql";

    @Mock
    private SqlProperties mockSqlProperties;

    @Mock
    private SqlScriptRunner mockSqlScriptRunner;

    @InjectMocks
    private SqlScriptAlbumBirthdayDao albumBirthdayDao;

    @Test
    void shouldClearAlbumBirthdays() {
        fail("not tested yet");
    }

    @Test
    void shouldPopulateAlbumBirthdays() throws SQLException {
        when(mockSqlProperties.getPopulateAlbumBirthdayScript()).thenReturn(POPULATE_ALBUM_BIRTHDAY_SCRIPT);
        albumBirthdayDao.populateAlbumBirthdays();
        verify(mockSqlScriptRunner).runSqlScript(POPULATE_ALBUM_BIRTHDAY_SCRIPT);
    }
}