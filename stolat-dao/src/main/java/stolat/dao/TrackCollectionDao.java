package stolat.dao;

import stolat.model.Track;

import java.util.List;

public interface TrackCollectionDao {

    void clearTrackCollection();

    void updateTrackCollection(List<Track> trackBatch, boolean force);
}
