package stolat.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
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

    @Option(names = {"-b", "--album-birthday"}, description = "refer to album birthday database structures")
    boolean albumBirthday;

    @Option(names = {"-a", "--album-collection"}, description = "refer to album collection database structures")
    boolean albumCollection;

    @Option(names = {"-c", "--create"}, description = "create and populate selected database structures")
    boolean create;

    @Option(names = {"-u", "--update"}, description = "update selected database structures")
    boolean update;

    @Option(names = {"-r", "--remove"}, description = "remove albums from database when not present in filesystem anymore")
    boolean remove;

    @Option(names = {"-f", "--folder"}, description = "overrides the folder where the album collections is to be fetched")
    File folder;

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
