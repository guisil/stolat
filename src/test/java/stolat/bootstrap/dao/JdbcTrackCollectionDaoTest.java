package stolat.bootstrap.dao;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;


@SpringBootTest
@AutoConfigureEmbeddedDatabase
@TestPropertySource("classpath:test-application.properties")
@ExtendWith(MockitoExtension.class)
class JdbcTrackCollectionDaoTest {

    private static final Instant FIXED_INSTANT = Instant.now();

    @Mock
    private Clock mockClock;

//    @Mock
//    private NamedParameterJdbcTemplate mockJdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @InjectMocks
    private JdbcTrackCollectionDao trackCollectionDao;

    @BeforeEach
    void setUp() {
        final Clock fixedClock = Clock.fixed(FIXED_INSTANT, ZoneId.systemDefault());

        lenient().when(mockClock.instant()).thenReturn(fixedClock.instant());

    }

    private void insertTestData() {
        //TODO INSERT TEST DATE...
    }

    @Test
    void clearTrackCollection() {
        fail("not tested yet");
    }

    @Test
    void populateTrackCollection() {
        fail("not tested yet");
    }
}