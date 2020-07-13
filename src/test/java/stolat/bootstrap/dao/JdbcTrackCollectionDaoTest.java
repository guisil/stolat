package stolat.bootstrap.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class JdbcTrackCollectionDaoTest {

    private static final Instant FIXED_INSTANT = Instant.now();

    @Mock
    private Clock mockClock;

    @Mock
    private NamedParameterJdbcTemplate mockJdbcTemplate;

    @InjectMocks
    private JdbcTrackCollectionDao trackCollectionDao;

    @BeforeEach
    void setUp() {
        final Clock fixedClock = Clock.fixed(FIXED_INSTANT, ZoneId.systemDefault());

        lenient().when(mockClock.instant()).thenReturn(fixedClock.instant());

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