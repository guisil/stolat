package stolat.mail.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import stolat.model.BirthdayAlbums;

import java.net.URI;
import java.net.URL;

@Component
@AllArgsConstructor
@Slf4j
public class ServiceClient {

    private RestTemplate restTemplate;
    private URI getBirthdaysUri;

    public BirthdayAlbums getBirthdayAlbums() {
        log.info("Calling the service to fetch today's birthday albums");
        return restTemplate.getForObject(getBirthdaysUri, BirthdayAlbums.class);
    }
}
