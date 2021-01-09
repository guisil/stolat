package stolat.service.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import stolat.model.Album;
import stolat.model.AlbumBirthday;
import stolat.model.Artist;
import stolat.model.BirthdayAlbums;

import java.net.URI;
import java.time.MonthDay;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceClientTest {

    private static final URI GET_BIRTHDAYS_URI = URI.create("http://localhost/stolat/birthdays");

    @Mock
    private RestTemplate mockRestTemplate;

    private ServiceClient serviceClient;

    @BeforeEach
    void setUp() {
        serviceClient = new ServiceClient(mockRestTemplate, GET_BIRTHDAYS_URI);
    }

    @Test
    void shouldCallGetBirthdayAlbumsService() {
        final var expected = new BirthdayAlbums(MonthDay.of(7, 25),
                MonthDay.of(7, 27),
                List.of(new AlbumBirthday(
                        new Album(
                                UUID.randomUUID(), "Some Album",
                                List.of(new Artist(UUID.randomUUID(), "Some Artist"))),
                        2000, 12, 22)));

        when(mockRestTemplate.getForObject(GET_BIRTHDAYS_URI, BirthdayAlbums.class)).thenReturn(expected);

        final var actual = serviceClient.getBirthdayAlbums();
        assertEquals(expected, actual);
    }
}