package stolat.bootstrap;


import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import picocli.CommandLine;
import stolat.bootstrap.cli.BootstrapCommand;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static picocli.CommandLine.IFactory;
import static picocli.CommandLine.ParseResult;

@SpringBootTest
@AutoConfigureEmbeddedDatabase
@TestPropertySource("classpath:test-application.properties")
class StoLatBootstrapApplicationTests {

    @Autowired
    private IFactory factory;

    @Autowired
    private BootstrapCommand command;

    @Test
    void contextLoads() {
    }

    @Test
    void shouldParseAlbumBirthdayCommandLineOption() {
        ParseResult parseResult = new CommandLine(command, factory)
                .parseArgs("-b");
        assertTrue(command.albumBirthday);
    }

    @Test
    void shouldParseAlbumCollectionCommandLineOption() {
        ParseResult parseResult = new CommandLine(command, factory)
                .parseArgs("-c");
        assertTrue(command.albumCollection);
    }

    @Test
    void shouldParsePathCommandLineOption() {
        String path = Path.of(File.separator, "some", "path", "to", "the", "collection").toString();
        ParseResult parseResult = new CommandLine(command, factory)
                .parseArgs("-p", path);
        assertNotNull(command.path);
    }

    @Test
    void shouldParseForceCommandLineOption() {
        ParseResult parseResult = new CommandLine(command, factory)
                .parseArgs("-f");
        assertTrue(command.force);
    }

    @Test
    void otherCommandLineArgs() {
        fail("not tested yet");
    }
}
