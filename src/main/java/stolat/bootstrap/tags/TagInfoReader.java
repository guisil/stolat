package stolat.bootstrap.tags;

import stolat.bootstrap.model.Track;

import java.io.File;
import java.util.Optional;

public interface TagInfoReader {

    Optional<Track> getTrackInfo(File file);
}
