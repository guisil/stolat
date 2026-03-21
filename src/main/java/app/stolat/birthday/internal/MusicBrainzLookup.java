package app.stolat.birthday.internal;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import app.stolat.birthday.ReleaseDateLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class MusicBrainzLookup implements ReleaseDateLookup {

    private static final Logger log = LoggerFactory.getLogger(MusicBrainzLookup.class);

    @Override
    public Optional<LocalDate> lookUp(UUID musicBrainzReleaseGroupId) {
        // TODO: implement MusicBrainz API call
        log.debug("Release date lookup not yet implemented for {}", musicBrainzReleaseGroupId);
        return Optional.empty();
    }
}
