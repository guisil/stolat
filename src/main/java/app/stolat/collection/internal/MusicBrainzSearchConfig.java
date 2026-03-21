package app.stolat.collection.internal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
class MusicBrainzSearchConfig {

    @Bean
    RestClient musicBrainzSearchRestClient(
            @Value("${stolat.musicbrainz.base-url:https://musicbrainz.org/ws/2}") String baseUrl,
            @Value("${stolat.user-agent}") String userAgent,
            @Qualifier("musicBrainzRateLimitInterceptor") ClientHttpRequestInterceptor rateLimiter) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", userAgent)
                .defaultHeader("Accept", "application/json")
                .requestInterceptor(rateLimiter)
                .build();
    }
}
