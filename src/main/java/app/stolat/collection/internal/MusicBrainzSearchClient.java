package app.stolat.collection.internal;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class MusicBrainzSearchClient {

    private final RestClient restClient;
    private final int scoreThreshold;

    MusicBrainzSearchClient(@Qualifier("musicBrainzSearchRestClient") RestClient restClient,
                            @Value("${stolat.musicbrainz.search-score-threshold:90}") int scoreThreshold) {
        this.restClient = restClient;
        this.scoreThreshold = scoreThreshold;
    }

    @SuppressWarnings("unchecked")
    public Optional<UUID> searchReleaseGroup(String artistName, String albumTitle) {
        try {
            var query = "releasegroup:\"%s\" AND artist:\"%s\"".formatted(albumTitle, artistName);
            var response = restClient.get()
                    .uri("/release-group/?query={query}&fmt=json&limit=1", query)
                    .retrieve()
                    .body(Map.class);

            if (response == null) return Optional.empty();

            var releaseGroups = (List<Map<String, Object>>) response.get("release-groups");
            if (releaseGroups == null || releaseGroups.isEmpty()) return Optional.empty();

            var first = releaseGroups.getFirst();
            var scoreObj = first.get("score");
            if (scoreObj == null) return Optional.empty();
            var score = ((Number) scoreObj).intValue();
            if (score < scoreThreshold) {
                log.debug("MusicBrainz search score too low ({}) for '{}' by '{}'", score, albumTitle, artistName);
                return Optional.empty();
            }

            var id = (String) first.get("id");
            log.info("MusicBrainz search matched '{}' by '{}' → {}", albumTitle, artistName, id);
            return Optional.of(UUID.fromString(id));
        } catch (Exception e) {
            log.warn("MusicBrainz search failed for '{}' by '{}': {}", albumTitle, artistName, e.getMessage());
            return Optional.empty();
        }
    }
}
