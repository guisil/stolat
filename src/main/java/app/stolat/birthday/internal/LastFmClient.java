package app.stolat.birthday.internal;

import java.util.Map;
import java.util.OptionalInt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@ConditionalOnProperty("stolat.lastfm.api-key")
class LastFmClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String username;

    LastFmClient(@Qualifier("lastFmRestClient") RestClient restClient,
                 @Value("${stolat.lastfm.api-key}") String apiKey,
                 @Value("${stolat.lastfm.username}") String username) {
        this.restClient = restClient;
        this.apiKey = apiKey;
        this.username = username;
    }

    @SuppressWarnings("unchecked")
    OptionalInt fetchAlbumPlayCount(String artist, String album) {
        try {
            var response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("method", "album.getinfo")
                            .queryParam("api_key", apiKey)
                            .queryParam("artist", artist)
                            .queryParam("album", album)
                            .queryParam("username", username)
                            .queryParam("format", "json")
                            .build())
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                return OptionalInt.empty();
            }

            var albumData = (Map<String, Object>) response.get("album");
            if (albumData == null) {
                return OptionalInt.empty();
            }

            var userPlayCount = albumData.get("userplaycount");
            if (userPlayCount == null) {
                return OptionalInt.empty();
            }

            return OptionalInt.of(Integer.parseInt(userPlayCount.toString()));
        } catch (Exception e) {
            log.warn("Failed to fetch play count for '{}' by '{}': {}", album, artist, e.getMessage());
            return OptionalInt.empty();
        }
    }
}
