package stolat.bootstrap;


import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
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
    void shouldParsingCommandLineArgs() {
        ParseResult parseResult = new CommandLine(command, factory)
                .parseArgs("-b", "-c");
        assertTrue(command.albumBirthday);
        assertTrue(command.create);
    }

	@Test
	void otherCommandLineArgs() {
		fail("not tested yet");
	}
}
