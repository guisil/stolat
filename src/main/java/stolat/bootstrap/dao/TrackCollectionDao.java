package stolat.bootstrap.dao;

import stolat.bootstrap.model.Track;

import java.util.Set;

public interface TrackCollectionDao {

    void clearTrackCollection();

    void populateTrackCollection(Set<Track> trackCollection, boolean force);
}
