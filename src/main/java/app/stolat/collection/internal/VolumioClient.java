package app.stolat.collection.internal;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@ConditionalOnProperty("stolat.volumio.url")
public class VolumioClient {

    private final RestClient restClient;

    VolumioClient(@Qualifier("volumioRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @SuppressWarnings("unchecked")
    public void playAlbum(String albumTitle, String artistName) {
        try {
            // Search for the album
            var searchResponse = restClient.get()
                    .uri("/api/v1/search?query={query}", albumTitle)
                    .retrieve()
                    .body(Map.class);

            if (searchResponse == null) {
                log.warn("Volumio search returned null for '{}'", albumTitle);
                return;
            }

            // Find album items in the navigation lists
            var navigation = (Map<String, Object>) searchResponse.get("navigation");
            if (navigation == null) {
                log.warn("No navigation in Volumio search results for '{}'", albumTitle);
                return;
            }

            var lists = (List<Map<String, Object>>) navigation.get("lists");
            if (lists == null || lists.isEmpty()) {
                log.warn("No lists in Volumio search results for '{}'", albumTitle);
                return;
            }

            // Find tracks matching the album - look through all lists
            for (var list : lists) {
                var items = (List<Map<String, Object>>) list.get("items");
                if (items == null) continue;

                // Look for a folder/album match first
                for (var item : items) {
                    var type = (String) item.get("type");
                    var title = (String) item.get("title");
                    if ("folder".equals(type) && albumTitle.equalsIgnoreCase(title)) {
                        // Browse into this album folder to get tracks
                        var uri = (String) item.get("uri");
                        playFromUri(uri);
                        return;
                    }
                }

                // If no folder match, look for song matches from this album
                var albumTracks = items.stream()
                        .filter(i -> "song".equals(i.get("type")))
                        .filter(i -> albumTitle.equalsIgnoreCase((String) i.get("album")))
                        .toList();

                if (!albumTracks.isEmpty()) {
                    replaceAndPlay(albumTracks);
                    return;
                }
            }

            log.warn("Could not find '{}' by '{}' in Volumio", albumTitle, artistName);
        } catch (Exception e) {
            log.error("Failed to play '{}' on Volumio: {}", albumTitle, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void playFromUri(String uri) {
        try {
            var browseResponse = restClient.get()
                    .uri("/api/v1/browse?uri={uri}", uri)
                    .retrieve()
                    .body(Map.class);

            if (browseResponse == null) return;

            var navigation = (Map<String, Object>) browseResponse.get("navigation");
            if (navigation == null) return;

            var lists = (List<Map<String, Object>>) navigation.get("lists");
            if (lists == null || lists.isEmpty()) return;

            var items = (List<Map<String, Object>>) lists.getFirst().get("items");
            if (items == null || items.isEmpty()) return;

            replaceAndPlay(items);
        } catch (Exception e) {
            log.error("Failed to browse Volumio URI '{}': {}", uri, e.getMessage());
        }
    }

    private void replaceAndPlay(List<Map<String, Object>> items) {
        var firstItem = items.getFirst();
        var body = Map.of(
                "item", firstItem,
                "list", items,
                "index", 0
        );

        restClient.post()
                .uri("/api/v1/replaceAndPlay")
                .body(body)
                .retrieve()
                .toBodilessEntity();

        log.info("Playing album on Volumio ({} tracks)", items.size());
    }
}
