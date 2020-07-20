package stolat.bootstrap.cli;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import stolat.bootstrap.dao.TrackCollectionDao;
import stolat.bootstrap.filesystem.TrackCollectionCrawler;
import stolat.bootstrap.model.Track;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

@Component
@AllArgsConstructor
@Slf4j
public class AlbumCollectionCommand {

    private TrackCollectionDao trackCollectionDao;
    private TrackCollectionCrawler trackCollectionCrawler;

    public void updateAlbumCollectionDatabase(boolean truncate, boolean force) {
        log.info("Triggering Album/Track Collection update for configured path{}{}", truncate ? " [truncate]" : "", force ? " [force]" : "");
        initTrackCollection(truncate);
        trackCollectionCrawler.processTrackCollection(getTrackBatchProcessor(force));
        log.info("Album/Track Collection update triggered");
    }

    public void updateAlbumCollectionDatabase(boolean truncate, Path rootPath, boolean force) {
        log.info("Triggering Album/Track Collection update for path '{}'{}{}", rootPath, truncate ? " [truncate]" : "", force ? " [force]" : "");
        initTrackCollection(truncate);
        trackCollectionCrawler.processTrackCollection(rootPath, getTrackBatchProcessor(force));
        log.info("Album/Track Collection update triggered");
    }

    private void initTrackCollection(boolean truncate) {
        if (truncate) {
            log.info("Clearing track collection");
            trackCollectionDao.clearTrackCollection();
        }
    }

    private Consumer<List<Track>> getTrackBatchProcessor(boolean force) {
        return trackBatch -> trackCollectionDao.updateTrackCollection(trackBatch, force);
    }
}
