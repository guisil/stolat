package stolat.mail.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

@Configuration
@ConfigurationProperties("service")
public class ServiceBeans {

    private String getBirthdaysUrl;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public URI getBirthdaysUri() {
        return URI.create(getBirthdaysUrl);
    }
}
