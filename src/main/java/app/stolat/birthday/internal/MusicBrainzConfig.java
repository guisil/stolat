package app.stolat.birthday.internal;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
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
                .requestInterceptor(new RateLimitingInterceptor())
                .build();
    }

    static class RateLimitingInterceptor implements ClientHttpRequestInterceptor {

        private static final long MIN_INTERVAL_MS = 1100; // MusicBrainz requires max 1 req/sec
        private long lastRequestTime = 0;

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                            ClientHttpRequestExecution execution) throws IOException {
            synchronized (this) {
                long elapsed = System.currentTimeMillis() - lastRequestTime;
                if (elapsed < MIN_INTERVAL_MS) {
                    try {
                        Thread.sleep(MIN_INTERVAL_MS - elapsed);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                lastRequestTime = System.currentTimeMillis();
            }
            return execution.execute(request, body);
        }
    }
}
