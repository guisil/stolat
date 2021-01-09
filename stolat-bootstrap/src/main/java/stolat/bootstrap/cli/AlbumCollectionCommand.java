package stolat.bootstrap.cli;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import stolat.bootstrap.filesystem.TrackCollectionCrawler;
import stolat.dao.TrackCollectionDao;
import stolat.model.Track;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

@Component
@AllArgsConstructor
@Slf4j
public class AlbumCollectionCommand {

    private final TrackCollectionDao trackCollectionDao;
    private final TrackCollectionCrawler trackCollectionCrawler;

    public void updateAlbumCollectionDatabase(boolean truncate) {
        log.info("Triggering Album/Track Collection update for configured path{}", truncate ? " [truncate]" : "");
        initTrackCollection(truncate);
        trackCollectionCrawler.processTrackCollection(getTrackBatchProcessor());
        log.info("Album/Track Collection update triggered");
    }

    public void updateAlbumCollectionDatabase(boolean truncate, Path rootPath) {
        log.info("Triggering Album/Track Collection update for path '{}'{}", rootPath, truncate ? " [truncate]" : "");
        initTrackCollection(truncate);
        trackCollectionCrawler.processTrackCollection(rootPath, getTrackBatchProcessor());
        log.info("Album/Track Collection update triggered");
    }

    private void initTrackCollection(boolean truncate) {
        if (truncate) {
            log.info("Clearing track collection");
            trackCollectionDao.clearTrackCollection();
        }
    }

    private Consumer<List<Track>> getTrackBatchProcessor() {
        return trackBatch -> trackCollectionDao.updateTrackCollection(trackBatch);
    }
}
