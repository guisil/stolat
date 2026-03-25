package app.stolat.birthday.internal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@ConditionalOnProperty("stolat.discogs.token")
class DiscogsReleaseDateConfig {

    @Bean
    RestClient discogsReleaseDateRestClient(
            @Value("${stolat.discogs.token}") String token,
            @Value("${stolat.user-agent}") String userAgent) {
        return RestClient.builder()
                .baseUrl("https://api.discogs.com")
                .defaultHeader("User-Agent", userAgent)
                .defaultHeader("Authorization", "Discogs token=" + token)
                .build();
    }
}
