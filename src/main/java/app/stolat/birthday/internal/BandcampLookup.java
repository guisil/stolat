package app.stolat.birthday.internal;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class BandcampLookup {

    private static final Pattern BANDCAMP_URL_PATTERN =
            Pattern.compile("https://[\\w-]+\\.bandcamp\\.com/album/.+");
    private static final Pattern JSON_LD_PATTERN =
            Pattern.compile("<script\\s+type=\"application/ld\\+json\">(.*?)</script>", Pattern.DOTALL);
    private static final DateTimeFormatter BANDCAMP_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    BandcampLookup() {
        this(RestClient.create());
    }

    BandcampLookup(RestClient restClient) {
        this.restClient = restClient;
        this.objectMapper = new ObjectMapper();
    }

    public Optional<LocalDate> lookUp(String bandcampUrl) {
        if (!BANDCAMP_URL_PATTERN.matcher(bandcampUrl).matches()) {
            log.warn("Invalid Bandcamp URL: {}", bandcampUrl);
            return Optional.empty();
        }

        try {
            var html = restClient.get()
                    .uri(bandcampUrl)
                    .retrieve()
                    .body(String.class);

            if (html == null) return Optional.empty();

            var matcher = JSON_LD_PATTERN.matcher(html);
            if (!matcher.find()) {
                log.debug("No JSON-LD found on Bandcamp page: {}", bandcampUrl);
                return Optional.empty();
            }

            var jsonLd = objectMapper.readTree(matcher.group(1));
            return extractDatePublished(jsonLd);
        } catch (Exception e) {
            log.warn("Failed to fetch Bandcamp page {}: {}", bandcampUrl, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<LocalDate> extractDatePublished(JsonNode jsonLd) {
        var dateNode = jsonLd.get("datePublished");
        if (dateNode == null || dateNode.isNull()) return Optional.empty();

        try {
            return Optional.of(LocalDate.parse(dateNode.asText(), BANDCAMP_DATE_FORMAT));
        } catch (Exception e) {
            log.warn("Failed to parse Bandcamp date '{}': {}", dateNode.asText(), e.getMessage());
            return Optional.empty();
        }
    }
}
