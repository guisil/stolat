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
    private final String musicLibraryUri;

    VolumioClient(@Qualifier("volumioRestClient") RestClient restClient,
                  @org.springframework.beans.factory.annotation.Value("${stolat.volumio.music-library-uri:music-library}") String musicLibraryUri) {
        this.restClient = restClient;
        this.musicLibraryUri = musicLibraryUri;
    }

    public void playAlbum(String albumTitle, String artistName, String folderPath) {
        try {
            if (folderPath != null && !folderPath.isBlank()) {
                playFromUri(musicLibraryUri + "/" + folderPath);
                return;
            }
            log.warn("Could not find '{}' by '{}' in Volumio: no folder path available", albumTitle, artistName);
        } catch (Exception e) {
            log.error("Failed to play '{}' on Volumio: {}", albumTitle, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void playFromUri(String uri) {
        log.debug("Browsing Volumio URI: {}", uri);
        try {
            var browseResponse = restClient.get()
                    .uri("/api/v1/browse?uri=" + uri)
                    .retrieve()
                    .body(Map.class);

            if (browseResponse == null) {
                log.warn("Volumio browse returned null for URI '{}'", uri);
                return;
            }

            var navigation = (Map<String, Object>) browseResponse.get("navigation");
            if (navigation == null) {
                log.warn("Volumio browse response has no 'navigation' for URI '{}'", uri);
                return;
            }

            var lists = (List<Map<String, Object>>) navigation.get("lists");
            if (lists == null || lists.isEmpty()) {
                log.warn("Volumio browse response has no lists for URI '{}'", uri);
                return;
            }

            var items = (List<Map<String, Object>>) lists.getFirst().get("items");
            if (items == null || items.isEmpty()) {
                log.warn("Volumio browse response has no items for URI '{}'", uri);
                return;
            }

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
