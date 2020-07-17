package stolat.bootstrap;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import stolat.bootstrap.cli.BootstrapCommand;

import java.util.List;
import java.util.concurrent.Future;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.fieldIn;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(args = "--album-collection")
@AutoConfigureEmbeddedDatabase
@TestPropertySource("classpath:test-application.properties")
public class StolatBootstrapApplicationAlbumCollectionVerboseTest {

    @Autowired
    private BootstrapCommand command;

    @Autowired
    private StoLatBootstrapApplication application;

    @Test
    void shouldUpdateAlbumBirthdayDatabaseWhenBirthdayOptionSelected() throws Exception {
        waitForExecutorsToFinish();
        assertFalse(command.albumBirthday);
        assertTrue(command.albumCollection);
        assertFalse(command.truncate);
        assertFalse(command.force);
        assertNull(command.path);

        assertEquals(0, application.getExitCode());
    }

    private void waitForExecutorsToFinish() {
        await().until(() -> {
            final List<Future> futures =  fieldIn(command).ofType(List.class).andWithName("futures").call();
            return futures.stream().allMatch(Future::isDone);
        });
    }
}
