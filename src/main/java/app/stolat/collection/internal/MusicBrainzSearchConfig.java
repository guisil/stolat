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
            @Qualifier("musicBrainzRateLimitInterceptor") ClientHttpRequestInterceptor rateLimiter) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", "StoLat/0.1.0 (https://github.com/guisil/stolat)")
                .defaultHeader("Accept", "application/json")
                .requestInterceptor(rateLimiter)
                .build();
    }
}
