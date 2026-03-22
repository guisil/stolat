package app.stolat.collection.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@ConditionalOnProperty("stolat.discogs.username")
public class DiscogsClient {

    private final RestClient restClient;

    DiscogsClient(@Qualifier("discogsRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @SuppressWarnings("unchecked")
    public List<DiscogsRelease> fetchCollection(String username) {
        var allReleases = new ArrayList<DiscogsRelease>();
        int page = 1;
        int totalPages = 1;

        while (page <= totalPages) {
            try {
                var response = restClient.get()
                        .uri("/users/{username}/collection/folders/0/releases?page={page}&per_page=100",
                                username, page)
                        .retrieve()
                        .body(Map.class);

                if (response == null) break;

                var pagination = (Map<String, Object>) response.get("pagination");
                totalPages = ((Number) pagination.get("pages")).intValue();

                var releases = (List<Map<String, Object>>) response.get("releases");
                for (var release : releases) {
                    var basicInfo = (Map<String, Object>) release.get("basic_information");
                    var title = (String) basicInfo.get("title");
                    var artists = (List<Map<String, Object>>) basicInfo.get("artists");
                    var artistName = artists.isEmpty() ? "Unknown" : (String) artists.getFirst().get("name");
                    // Discogs appends disambiguation suffixes like "(2)", "(UK)", etc.
                    artistName = artistName.replaceAll("\\s*\\([^)]+\\)$", "");
                    var discogsId = ((Number) basicInfo.get("id")).longValue();
                    var yearObj = basicInfo.get("year");
                    Integer year = null;
                    if (yearObj instanceof Number n && n.intValue() > 0) {
                        year = n.intValue();
                    }
                    allReleases.add(new DiscogsRelease(discogsId, artistName, title, year));
                }

                log.info("Fetched Discogs page {}/{} ({} releases)", page, totalPages, releases.size());
                page++;
            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch Discogs collection page " + page, e);
            }
        }

        log.info("Fetched {} total releases from Discogs", allReleases.size());
        return allReleases;
    }
}
