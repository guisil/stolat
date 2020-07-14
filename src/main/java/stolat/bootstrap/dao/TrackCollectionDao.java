package stolat.bootstrap.dao;

import stolat.bootstrap.model.Album;
import stolat.bootstrap.model.Track;

import java.util.Set;
import java.util.UUID;

public interface TrackCollectionDao {

    void clearTrackCollection();

    void populateTrackCollection(Set<Track> trackCollection, boolean force);
}
