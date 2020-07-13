package stolat.bootstrap.cli;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    public boolean albumBirthday;

    @Option(names = {"-c", "--album-collection"}, description = "populate the album collection database structures")
    public boolean albumCollection;

    @Option(names = {"-f", "--force"}, description = "truncate table(s) before starting")
    public boolean force;

    @Option(names = {"-p", "--path"}, description = "overrides the path where the album collection is to be fetched")
    public Path path;

    @Autowired
    private AlbumBirthdayCommand albumBirthdayCommand;

    @Autowired
    private AlbumCollectionCommand albumCollectionCommand;

    @Option(names = "--spring.config.location", hidden = true)
    private String springConfigLocation;

//    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display a help message")
//    private boolean help;

    @Override
    public Integer call() {

        if (albumBirthday) {
            triggerAlbumBirthdayUpdate();
        }
        if (albumCollection) {
            triggerAlbumCollectionUpdate();
        }
        if (!albumBirthday && !albumCollection) {
            triggerAlbumBirthdayUpdate();
            triggerAlbumCollectionUpdate();
        }

        return 0;
    }

    private void triggerAlbumBirthdayUpdate() {
        log.debug("Triggered option to update album birthdays.");
        albumBirthdayCommand.updateAlbumBirthdayDatabase();
    }

    private void triggerAlbumCollectionUpdate() {
        String truncateAnd = force ? "truncate and " : "";
        if (path != null) {
            log.debug("Triggered option to {}update album collection from path {}.", truncateAnd, path);
            albumCollectionCommand.updateAlbumCollectionDatabase(force, path);
        } else {
            log.debug("Triggered option to {}update album collection from root path.", truncateAnd);
            albumCollectionCommand.updateAlbumCollectionDatabase(force);
        }
    }
}
