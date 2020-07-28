package stolat.service.client;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import stolat.model.BirthdayAlbums;

import java.net.URI;

@Component
@AllArgsConstructor
@Slf4j
public class ServiceClient {

    private final RestTemplate restTemplate;
    private final URI getBirthdaysUri;

    public BirthdayAlbums getBirthdayAlbums() {
        log.info("Calling the service to fetch today's birthday albums");
        return restTemplate.getForObject(getBirthdaysUri, BirthdayAlbums.class);
    }
}
