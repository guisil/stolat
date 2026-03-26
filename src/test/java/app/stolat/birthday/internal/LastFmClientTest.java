package app.stolat.birthday.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class LastFmClientTest {

    private MockRestServiceServer mockServer;
    private LastFmClient lastFmClient;

    @BeforeEach
    void setUp() {
        var builder = RestClient.builder().baseUrl("https://ws.audioscrobbler.com/2.0");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        lastFmClient = new LastFmClient(builder.build(), "test-api-key", "testuser");
    }

    @Test
    void shouldReturnPlayCountWhenAlbumFound() {
        var responseJson = """
                {
                    "album": {
                        "name": "OK Computer",
                        "artist": "Radiohead",
                        "userplaycount": "142"
                    }
                }
                """;
        mockServer.expect(requestTo(
                "https://ws.audioscrobbler.com/2.0?method=album.getinfo&api_key=test-api-key&artist=Radiohead&album=OK%20Computer&username=testuser&format=json"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        var result = lastFmClient.fetchPlayCount("Radiohead", "OK Computer");

        assertThat(result).isPresent();
        assertThat(result.getAsInt()).isEqualTo(142);
        mockServer.verify();
    }

    @Test
    void shouldReturnEmptyWhenAlbumNotFound() {
        var responseJson = """
                {
                    "error": 6,
                    "message": "Album not found"
                }
                """;
        mockServer.expect(requestTo(
                "https://ws.audioscrobbler.com/2.0?method=album.getinfo&api_key=test-api-key&artist=Unknown&album=Nothing&username=testuser&format=json"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        var result = lastFmClient.fetchPlayCount("Unknown", "Nothing");

        assertThat(result).isEmpty();
        mockServer.verify();
    }

    @Test
    void shouldReturnEmptyOnApiError() {
        mockServer.expect(requestTo(
                "https://ws.audioscrobbler.com/2.0?method=album.getinfo&api_key=test-api-key&artist=Radiohead&album=OK%20Computer&username=testuser&format=json"))
                .andRespond(withServerError());

        var result = lastFmClient.fetchPlayCount("Radiohead", "OK Computer");

        assertThat(result).isEmpty();
        mockServer.verify();
    }

    @Test
    void shouldReturnEmptyWhenPlayCountIsZero() {
        var responseJson = """
                {
                    "album": {
                        "name": "OK Computer",
                        "artist": "Radiohead",
                        "userplaycount": "0"
                    }
                }
                """;
        mockServer.expect(requestTo(
                "https://ws.audioscrobbler.com/2.0?method=album.getinfo&api_key=test-api-key&artist=Radiohead&album=OK%20Computer&username=testuser&format=json"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        var result = lastFmClient.fetchPlayCount("Radiohead", "OK Computer");

        assertThat(result).isPresent();
        assertThat(result.getAsInt()).isEqualTo(0);
        mockServer.verify();
    }

    @Test
    void shouldReturnPlayCountWhenApiReturnsInteger() {
        var responseJson = """
                {
                    "album": {
                        "name": "The Album of the Soundtrack of the Trailer of the Film of Monty Python and the Holy Grail: Executive Version",
                        "artist": "Monty Python",
                        "userplaycount": 7
                    }
                }
                """;
        mockServer.expect(requestTo(
                "https://ws.audioscrobbler.com/2.0?method=album.getinfo&api_key=test-api-key&artist=Monty%20Python&album=Executive%20Version&username=testuser&format=json"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        var result = lastFmClient.fetchPlayCount("Monty Python", "Executive Version");

        assertThat(result).isPresent();
        assertThat(result.getAsInt()).isEqualTo(7);
        mockServer.verify();
    }

    @Test
    void shouldHandleSpecialCharactersInArtistAndAlbum() {
        var responseJson = """
                {
                    "album": {
                        "name": "Back in Black",
                        "artist": "AC/DC",
                        "userplaycount": "50"
                    }
                }
                """;
        mockServer.expect(requestTo(
                "https://ws.audioscrobbler.com/2.0?method=album.getinfo&api_key=test-api-key&artist=AC%2FDC&album=Back%20in%20Black&username=testuser&format=json"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        var result = lastFmClient.fetchPlayCount("AC/DC", "Back in Black");

        assertThat(result).isPresent();
        assertThat(result.getAsInt()).isEqualTo(50);
        mockServer.verify();
    }
}
