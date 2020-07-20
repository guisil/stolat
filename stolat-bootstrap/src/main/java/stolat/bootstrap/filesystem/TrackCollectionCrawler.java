package stolat.bootstrap.filesystem;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import stolat.bootstrap.model.Track;
import stolat.bootstrap.tags.TagInfoReader;
import stolat.bootstrap.utils.BatchingIterator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
@Slf4j
public class TrackCollectionCrawler {

    private FileSystemProperties fileSystemProperties;
    private TagInfoReader tagInfoReader;

    public void processTrackCollection(Consumer<List<Track>> processTracks) {
        processTrackCollection(
                Path.of(fileSystemProperties.getAlbumCollectionPath()),
                processTracks);
    }

    public void processTrackCollection(Path rootPath, Consumer<List<Track>> processTracks) {
        log.info("Processing track collection from root path '{}'", rootPath);

        AtomicInteger processed = new AtomicInteger();
        AtomicInteger accepted = new AtomicInteger();
        AtomicInteger ignored = new AtomicInteger();

        try (Stream<File> walk =
                     Files.walk(rootPath)
                             .filter(Files::isRegularFile)
                             .map(Path::toFile)
                             .filter(file -> fileWithAcceptableExtension(file, accepted, ignored))) {

            BatchingIterator
                    .batchedStreamOf(walk, fileSystemProperties.getAlbumCollectionBatchSize())
                    .map(tagInfoReader::getTrackBatchInfo)
                    .forEach(tracksToProcess -> {
                        log.info("Processing {} tracks", tracksToProcess.size());
                        processTracks.accept(tracksToProcess);
                        processed.addAndGet(tracksToProcess.size());
                    });
        } catch (IOException | SecurityException ex) {
            log.warn("Error occurred while fetching the track collection", ex);
        } finally {
            log.info("Processed {} tracks from {} accepted '{}' files ({} files were ignored)",
                    processed.get(),
                    accepted.get(),
                    String.join(",", fileSystemProperties.getMusicFileExtensions()),
                    ignored.get());
        }
    }

    private boolean fileWithAcceptableExtension(File file, AtomicInteger accepted, AtomicInteger ignored) {
        return fileSystemProperties.getMusicFileExtensions().stream()
                .anyMatch(extension -> {
                    if (file.getName().endsWith("." + extension)) {
                        accepted.incrementAndGet();
                        return true;
                    } else {
                        ignored.incrementAndGet();
                        return false;
                    }
                });
    }
}
