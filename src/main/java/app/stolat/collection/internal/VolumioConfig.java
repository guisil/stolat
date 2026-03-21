package app.stolat.collection.internal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@ConditionalOnProperty("stolat.volumio.url")
class VolumioConfig {

    @Bean
    RestClient volumioRestClient(@Value("${stolat.volumio.url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new SimpleClientHttpRequestFactory())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
