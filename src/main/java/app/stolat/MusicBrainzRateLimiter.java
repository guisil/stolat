package app.stolat;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

@Configuration
public class MusicBrainzRateLimiter {

    @Bean
    public ClientHttpRequestInterceptor musicBrainzRateLimitInterceptor() {
        return new RateLimitingInterceptor();
    }

    static class RateLimitingInterceptor implements ClientHttpRequestInterceptor {
        private static final long MIN_INTERVAL_MS = 1100;
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
