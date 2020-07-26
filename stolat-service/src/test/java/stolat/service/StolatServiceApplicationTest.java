package stolat.service;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.time.Clock;

@SpringBootTest
@AutoConfigureEmbeddedDatabase(beanName = "dataSource")
@TestPropertySource("classpath:test-application.properties")
class StolatServiceApplicationTest {

    @MockBean
    private Clock mockClock;

    @Autowired
    private StolatServiceApplication application;

    @Test
    void shouldLoadApplicationContext() {
    }
}