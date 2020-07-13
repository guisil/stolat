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

    public void updateAlbumCollectionDatabase(boolean recreate) {
        Set<Track> trackCollection = trackCollectionCrawler.fetchTrackCollection();
        updateTrackCollection(recreate, trackCollection);
    }

    public void updateAlbumCollectionDatabase(boolean recreate, Path rootPath) {
        Set<Track> trackCollection = trackCollectionCrawler.fetchTrackCollection(rootPath);
        updateTrackCollection(recreate, trackCollection);
    }

    private void updateTrackCollection(boolean recreate, Set<Track> trackCollection) {
        if (recreate) {
            trackCollectionDao.clearTrackCollection();
        }
        trackCollectionDao.populateTrackCollection(trackCollection);
    }
}
