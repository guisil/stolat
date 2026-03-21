package app.stolat.birthday.internal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
class MusicBrainzConfig {

    @Bean
    RestClient musicBrainzRestClient(
            @Value("${stolat.musicbrainz.base-url:https://musicbrainz.org/ws/2}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", "StoLat/0.1.0 (https://github.com/guisil/stolat)")
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
