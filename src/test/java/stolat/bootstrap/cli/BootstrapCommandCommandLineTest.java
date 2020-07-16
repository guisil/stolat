package stolat.bootstrap.cli;


import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeEach;
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
class BootstrapCommandCommandLineTest {

    @Autowired
    private IFactory factory;

    @Autowired
    private BootstrapCommand command;

    @BeforeEach
    void setUp() {
        command.albumBirthday = false;
        command.albumCollection = false;
        command.truncate = false;
        command.force = false;
        command.path = null;
    }

    @Test
    void contextLoads() {
    }

    @Test
    void shouldParseAlbumBirthdayCommandLineOption() {
        ParseResult parseResult = new CommandLine(command, factory).parseArgs("-b");
        assertTrue(command.albumBirthday);
    }

    @Test
    void shouldParseVerboseAlbumBirthdayCommandLineOption() {
        ParseResult parseResult = new CommandLine(command, factory).parseArgs("--album-birthday");
        assertTrue(command.albumBirthday);
    }

    @Test
    void shouldParseAlbumCollectionCommandLineOption() {
        ParseResult parseResult = new CommandLine(command, factory).parseArgs("-c");
        assertTrue(command.albumCollection);
    }

    @Test
    void shouldParseVerboseAlbumCollectionCommandLineOption() {
        ParseResult parseResult = new CommandLine(command, factory).parseArgs("--album-collection");
        assertTrue(command.albumCollection);
    }

    @Test
    void shouldParsePathCommandLineOption() {
        String path = Path.of(File.separator, "some", "path", "to", "the", "collection").toString();
        ParseResult parseResult = new CommandLine(command, factory).parseArgs("-p", path);
        assertNotNull(command.path);
    }

    @Test
    void shouldParseVerbosePathCommandLineOption() {
        String path = Path.of(File.separator, "some", "path", "to", "the", "collection").toString();
        ParseResult parseResult = new CommandLine(command, factory).parseArgs("--path", path);
        assertNotNull(command.path);
    }

    @Test
    void shouldParseTruncateCommandLineOption() {
        ParseResult parseResult = new CommandLine(command, factory).parseArgs("-t");
        assertTrue(command.truncate);
    }

    @Test
    void shouldParseVerboseTruncateCommandLineOption() {
        ParseResult parseResult = new CommandLine(command, factory).parseArgs("--truncate");
        assertTrue(command.truncate);
    }

    @Test
    void shouldParseForceCommandLineOption() {
        ParseResult parseResult = new CommandLine(command, factory).parseArgs("-f");
        assertTrue(command.force);
    }

    @Test
    void shouldParseVerboseForceCommandLineOption() {
        ParseResult parseResult = new CommandLine(command, factory).parseArgs("--force");
        assertTrue(command.force);
    }
}
