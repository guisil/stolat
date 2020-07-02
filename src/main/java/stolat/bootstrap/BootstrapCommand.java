package stolat.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

@Component
@Command(
        name = "stolat-bootstrap",
        description = "Bootstraps the DB for StoLat",
        mixinStandardHelpOptions = true,
        version = "0.1"
)
public class BootstrapCommand implements Callable<Integer> {

    private static final Logger LOG = LoggerFactory.getLogger(BootstrapCommand.class);

    @Option(names = "--spring.config.location", hidden = true)
    private String springConfigLocation;

    @Option(names = {"-b", "--album-birthday"}, description = "populate the album birthday database structures")
    boolean albumBirthday;

    @Option(names = {"-c", "--album-collection"}, description = "populate the album collection database structures")
    boolean albumCollection;

    @Option(names = {"-f", "--force"}, description = "truncate table(s) before starting")
    boolean force;

    @Option(names = {"-p", "--path"}, description = "overrides the path where the album collection is to be fetched")
    Path path;

//    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display a help message")
//    private boolean help;

    @Override
    public Integer call() {

        LOG.warn("CALLING!!!!!!");

        if (albumBirthday) {
            LOG.warn("ALBUM BIRTHDAY!!!!!!");
        }

        return 0;
    }
}
