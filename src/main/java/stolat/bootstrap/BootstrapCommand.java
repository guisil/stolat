package stolat.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import stolat.bootstrap.dao.AlbumBirthdayDao;
import stolat.bootstrap.filesystem.TrackCollectionCrawler;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

@Component
@Slf4j
@Command(
        name = "stolat-bootstrap",
        description = "Bootstraps the DB for StoLat",
        mixinStandardHelpOptions = true,
        version = "0.1"
)
public class BootstrapCommand implements Callable<Integer> {

    @Option(names = {"-b", "--album-birthday"}, description = "populate the album birthday database structures")
    boolean albumBirthday;

    @Option(names = {"-c", "--album-collection"}, description = "populate the album collection database structures")
    boolean albumCollection;

    @Option(names = {"-f", "--force"}, description = "truncate table(s) before starting")
    boolean force;

    @Option(names = {"-p", "--path"}, description = "overrides the path where the album collection is to be fetched")
    Path path;

    @Autowired
    private AlbumBirthdayDao albumBirthdayDao;

    @Autowired
    private TrackCollectionCrawler trackCollectionCrawler;

    @Option(names = "--spring.config.location", hidden = true)
    private String springConfigLocation;

//    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display a help message")
//    private boolean help;

    @Override
    public Integer call() {

        if (albumBirthday) {
            log.debug("Triggered option to populate album birthdays.");
            albumBirthdayDao.populateAlbumBirthdays();
        }

        return 0;
    }
}
