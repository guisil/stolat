package app.stolat.birthday.internal;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@ConditionalOnProperty("stolat.discogs.token")
public class DiscogsReleaseDateLookup {

    private final RestClient restClient;

    DiscogsReleaseDateLookup(@Qualifier("discogsReleaseDateRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @SuppressWarnings("unchecked")
    public Optional<LocalDate> lookUp(long discogsId) {
        try {
            var response = restClient.get()
                    .uri("/releases/{id}", discogsId)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                return Optional.empty();
            }

            var dateStr = (String) response.get("released");
            return parseReleaseDate(dateStr);
        } catch (Exception e) {
            log.warn("Failed to look up Discogs release date for {}: {}", discogsId, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<LocalDate> parseReleaseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(switch (dateStr.length()) {
                case 4 -> // YYYY
                        LocalDate.of(Integer.parseInt(dateStr), 1, 1);
                case 7 -> // YYYY-MM (shouldn't happen for Discogs but handle it)
                        LocalDate.parse(dateStr + "-01");
                case 10 -> {
                    // YYYY-MM-DD or YYYY-MM-00
                    if (dateStr.endsWith("-00")) {
                        yield LocalDate.parse(dateStr.substring(0, 7) + "-01");
                    }
                    yield LocalDate.parse(dateStr);
                }
                default -> throw new IllegalArgumentException("Unexpected date format: " + dateStr);
            });
        } catch (Exception e) {
            log.warn("Failed to parse Discogs release date '{}': {}", dateStr, e.getMessage());
            return Optional.empty();
        }
    }
}
