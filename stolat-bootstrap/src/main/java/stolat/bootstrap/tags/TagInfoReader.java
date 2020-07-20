package stolat.bootstrap.tags;

import stolat.bootstrap.model.Track;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface TagInfoReader {

    Optional<Track> getTrackInfo(File file);

    default List<Track> getTrackBatchInfo(List<File> files) {
        return files.stream()
                .map(this::getTrackInfo)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
