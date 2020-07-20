package stolat.bootstrap;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import stolat.bootstrap.cli.BootstrapCommand;
import stolat.bootstrap.filesystem.TrackCollectionCrawler;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Future;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.fieldIn;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(args = {"-c", "-t", "-f", "-ptestpath"})
@AutoConfigureEmbeddedDatabase
@TestPropertySource("classpath:test-application.properties")
public class StolatBootstrapApplicationAlbumCollectionTruncateForcePathTest {

    @MockBean
    private TrackCollectionCrawler mockCrawler;

    @Autowired
    private BootstrapCommand command;

    @Autowired
    private StoLatBootstrapApplication application;

    @Test
    void shouldUpdateAlbumBirthdayDatabaseWhenBirthdayOptionSelected() throws Exception {
        waitForExecutorsToFinish();
        assertFalse(command.albumBirthday);
        assertTrue(command.albumCollection);
        assertTrue(command.truncate);
        assertTrue(command.force);
        assertEquals(Path.of("testpath"), command.path);
    }

    private void waitForExecutorsToFinish() {
        await().until(() -> {
            final List<Future> futures = fieldIn(command).ofType(List.class).andWithName("futures").call();
            return futures.stream().allMatch(Future::isDone);
        });
    }
}
