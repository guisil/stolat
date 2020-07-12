package stolat.bootstrap.dao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JdbcTrackCollectionDaoTest {

    @Mock
    private NamedParameterJdbcTemplate mockJdbcTemplate;

    @InjectMocks
    private JdbcTrackCollectionDao trackCollectionDao;

    @Test
    void clearTrackCollection() {
        fail("not tested yet");
    }

    @Test
    void populateTrackCollection() {
        fail("not tested yet");
    }
}