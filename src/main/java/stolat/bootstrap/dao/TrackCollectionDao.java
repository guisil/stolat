package stolat.bootstrap.dao;

import stolat.bootstrap.model.Track;

import java.util.List;

public interface TrackCollectionDao {

    void clearTrackCollection();

    void populateTrackCollection(List<Track> trackCollection);
}
