package stolat.bootstrap.cli;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

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
    @Option(names = {"-t", "--truncate"}, description = "truncate table(s) before starting")
    public boolean truncate;
    @Option(names = {"-p", "--path"}, description = "overrides the path where the album collection is to be fetched")
    public Path path;
    @Option(names = "--spring.config.location", hidden = true)
    private String springConfigLocation;

    private final List<CompletableFuture<Void>> futures = new ArrayList<>();
    private final AlbumBirthdayCommand albumBirthdayCommand;
    private final AlbumCollectionCommand albumCollectionCommand;

    public BootstrapCommand(
            AlbumBirthdayCommand albumBirthdayCommand,
            AlbumCollectionCommand albumCollectionCommand) {
        this.albumBirthdayCommand = albumBirthdayCommand;
        this.albumCollectionCommand = albumCollectionCommand;
    }

    @Override
    public Integer call() {
        final AtomicInteger exitCode = new AtomicInteger(0);

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

        try {
            CompletableFuture
                    .allOf(futures.stream().toArray(CompletableFuture[]::new))
                    .exceptionally(ex -> {
                        log.error("Error during thread execution", ex);
                        exitCode.set(1);
                        return null;
                    }).get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Thread interrupted", ex);
            exitCode.set(1);
        } catch (ExecutionException ex) {
            log.warn("Error occurred while getting the future", ex);
            exitCode.set(1);
        }

        log.info("Exiting command execution");

        return exitCode.get();
    }

    private void triggerAlbumBirthdayUpdate() {
        log.debug("Triggered option to update album birthdays.");
        final Runnable updateAlbumBirthdayDatabase = albumBirthdayCommand::updateAlbumBirthdayDatabase;
        futures.add(CompletableFuture.runAsync(updateAlbumBirthdayDatabase));
    }

    private void triggerAlbumCollectionUpdate() {
        final String truncateAnd = truncate ? "truncate and " : "";
        final String update = "update";
        final Runnable updateAlbumCollectionDatabase;
        if (path != null) {
            log.debug("Triggered option to {}{} album collection from path {}.", truncateAnd, update, path);
            updateAlbumCollectionDatabase = () -> albumCollectionCommand.updateAlbumCollectionDatabase(truncate, path);
        } else {
            log.debug("Triggered option to {}{} album collection from root path.", truncateAnd, update);
            updateAlbumCollectionDatabase = () -> albumCollectionCommand.updateAlbumCollectionDatabase(truncate);
        }
        futures.add(CompletableFuture.runAsync(updateAlbumCollectionDatabase));
    }
}
