package app.stolat.birthday.internal;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;

@Configuration
@ConditionalOnProperty("stolat.lastfm.api-key")
class LastFmConfig {

    private static final long MIN_INTERVAL_MS = 200; // Last.fm allows 5 req/s

    @Bean
    LastFmClient lastFmClient(
            @Value("${stolat.lastfm.api-key}") String apiKey,
            @Value("${stolat.lastfm.username}") String username,
            @Value("${stolat.user-agent}") String userAgent) {
        var restClient = RestClient.builder()
                .baseUrl("https://ws.audioscrobbler.com/2.0")
                .defaultHeader("User-Agent", userAgent)
                .requestInterceptor(new LastFmRateLimitInterceptor())
                .build();
        return new LastFmClient(restClient, apiKey, username);
    }

    static class LastFmRateLimitInterceptor implements ClientHttpRequestInterceptor {
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
