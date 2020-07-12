package stolat.bootstrap.filesystem;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import stolat.bootstrap.model.Track;
import stolat.bootstrap.tags.TagInfoReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class TrackCollectionCrawler {

    @Autowired
    private FileSystemProperties fileSystemProperties;

    @Autowired
    private TagInfoReader tagInfoReader;

    public List<Track> fetchTrackCollection(Path rootPath) {
        log.info("Fetching track collection from root path '{}'", rootPath);

        try (Stream<Path> walk = Files.walk(rootPath)) {
            return walk.filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .filter(this::fileWithAcceptableExtension)
                    .map(tagInfoReader::getTrackInfo)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            log.warn("Error occurred while fetching the track collection", ex);
        }
        return Collections.emptyList();
    }

    private boolean fileWithAcceptableExtension(File file) {
        boolean acceptable = fileSystemProperties.getMusicFileExtensions().stream()
                .anyMatch(extension -> file.getName().endsWith("." + extension));

        return acceptable;
    }

    public List<Track> fetchTrackCollection() {
        return fetchTrackCollection(Path.of(fileSystemProperties.getAlbumCollectionPath()));
    }
}
