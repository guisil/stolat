package stolat.bootstrap.cli;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import stolat.bootstrap.dao.AlbumBirthdayDao;
import stolat.bootstrap.dao.TrackCollectionDao;
import stolat.bootstrap.filesystem.FileSystemProperties;
import stolat.bootstrap.filesystem.TrackCollectionCrawler;
import stolat.bootstrap.model.Track;

import java.nio.file.Path;
import java.util.Set;

@Component
@Slf4j
public class AlbumCollectionCommand {

    @Autowired
    private TrackCollectionDao trackCollectionDao;

    @Autowired
    private TrackCollectionCrawler trackCollectionCrawler;

    public void updateAlbumCollectionDatabase(boolean truncate, boolean force) {
        log.info("Triggering Album/Track Collection update for configured path{}{}", truncate ? " [truncate]" : "", force ? " [force]" : "");
        Set<Track> trackCollection = trackCollectionCrawler.fetchTrackCollection();
        updateTrackCollection(truncate, trackCollection, force);
        log.info("Album/Track Collection update triggered");
    }

    public void updateAlbumCollectionDatabase(boolean truncate, Path rootPath, boolean force) {
        log.info("Triggering Album/Track Collection update for path '{}'{}{}", rootPath, truncate ? " [truncate]" : "", force ? " [force]" : "");
        Set<Track> trackCollection = trackCollectionCrawler.fetchTrackCollection(rootPath);
        updateTrackCollection(truncate, trackCollection, force);
        log.info("Album/Track Collection update triggered");
    }

    private void updateTrackCollection(boolean truncate, Set<Track> trackCollection, boolean force) {
        if (truncate) {
            trackCollectionDao.clearTrackCollection();
        }
        trackCollectionDao.populateTrackCollection(trackCollection, force);
    }
}
