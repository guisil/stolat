package app.stolat.birthday.internal;

import java.util.Map;
import java.util.OptionalInt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;

@Slf4j
public class LastFmClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String username;

    LastFmClient(RestClient restClient, String apiKey, String username) {
        this.restClient = restClient;
        this.apiKey = apiKey;
        this.username = username;
    }

    @SuppressWarnings("unchecked")
    public OptionalInt fetchPlayCount(String artistName, String albumTitle) {
        try {
            var response = restClient.get()
                    .uri("?method=album.getinfo&api_key={apiKey}&artist={artist}&album={album}&username={username}&format=json",
                            apiKey, artistName, albumTitle, username)
                    .retrieve()
                    .body(Map.class);

            if (response == null || response.containsKey("error")) {
                return OptionalInt.empty();
            }

            var album = (Map<String, Object>) response.get("album");
            if (album == null) {
                return OptionalInt.empty();
            }

            var playCountValue = album.get("userplaycount");
            if (playCountValue == null) {
                return OptionalInt.empty();
            }

            return OptionalInt.of(Integer.parseInt(String.valueOf(playCountValue)));
        } catch (Exception e) {
            log.warn("Failed to fetch Last.fm play count for '{}' by '{}': {}", albumTitle, artistName, e.getMessage());
            return OptionalInt.empty();
        }
    }
}
