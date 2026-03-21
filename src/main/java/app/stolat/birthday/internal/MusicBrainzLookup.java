package app.stolat.birthday.internal;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import app.stolat.birthday.ReleaseDateLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
class MusicBrainzLookup implements ReleaseDateLookup {

    private static final Logger log = LoggerFactory.getLogger(MusicBrainzLookup.class);

    private final RestClient restClient;

    MusicBrainzLookup(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Optional<LocalDate> lookUp(UUID musicBrainzReleaseGroupId) {
        try {
            @SuppressWarnings("unchecked")
            var response = restClient.get()
                    .uri("/release-group/{mbid}?fmt=json", musicBrainzReleaseGroupId)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                return Optional.empty();
            }

            var dateStr = (String) response.get("first-release-date");
            return parseReleaseDate(dateStr);
        } catch (Exception e) {
            log.warn("Failed to look up release date for {}: {}", musicBrainzReleaseGroupId, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<LocalDate> parseReleaseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(switch (dateStr.length()) {
                case 4 -> LocalDate.of(Integer.parseInt(dateStr), 1, 1);
                case 7 -> LocalDate.parse(dateStr + "-01");
                default -> LocalDate.parse(dateStr);
            });
        } catch (Exception e) {
            log.warn("Failed to parse release date '{}': {}", dateStr, e.getMessage());
            return Optional.empty();
        }
    }
}
