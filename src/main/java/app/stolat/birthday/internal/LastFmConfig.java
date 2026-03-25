package app.stolat.birthday.internal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@ConditionalOnProperty("stolat.lastfm.api-key")
class LastFmConfig {

    @Bean
    LastFmClient lastFmClient(
            @Value("${stolat.lastfm.api-key}") String apiKey,
            @Value("${stolat.lastfm.username}") String username,
            @Value("${stolat.user-agent}") String userAgent) {
        var restClient = RestClient.builder()
                .baseUrl("https://ws.audioscrobbler.com/2.0")
                .defaultHeader("User-Agent", userAgent)
                .build();
        return new LastFmClient(restClient, apiKey, username);
    }
}
