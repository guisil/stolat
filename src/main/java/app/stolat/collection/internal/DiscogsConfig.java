package app.stolat.collection.internal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@ConditionalOnProperty("stolat.discogs.username")
class DiscogsConfig {

    @Bean
    RestClient discogsRestClient(@Value("${stolat.discogs.token:}") String token) {
        return RestClient.builder()
                .baseUrl("https://api.discogs.com")
                .defaultHeader("User-Agent", "StoLat/0.1.0 (https://github.com/guisil/stolat)")
                .defaultHeader("Authorization", "Discogs token=" + token)
                .build();
    }
}
