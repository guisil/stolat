package app.stolat.birthday;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface ReleaseDateLookup {

    Optional<LocalDate> lookUp(UUID musicBrainzReleaseGroupId);
}
