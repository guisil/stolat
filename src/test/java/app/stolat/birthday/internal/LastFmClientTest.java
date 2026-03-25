package app.stolat.birthday.internal;

import java.util.Map;

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
        var restClientBuilder = RestClient.builder()
                .baseUrl("https://ws.audioscrobbler.com/2.0/");
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        var restClient = restClientBuilder.build();
        lastFmClient = new LastFmClient(restClient, "test-api-key", "testuser");
    }

    @Test
    void shouldReturnPlayCountWhenLastFmReturnsUserPlayCount() {
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
                        "https://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=test-api-key&artist=Radiohead&album=OK%20Computer&username=testuser&format=json"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        var result = lastFmClient.fetchAlbumPlayCount("Radiohead", "OK Computer");

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
                        "https://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=test-api-key&artist=Unknown&album=Unknown%20Album&username=testuser&format=json"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        var result = lastFmClient.fetchAlbumPlayCount("Unknown", "Unknown Album");

        assertThat(result).isEmpty();
        mockServer.verify();
    }

    @Test
    void shouldReturnEmptyWhenServerError() {
        mockServer.expect(requestTo(
                        "https://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=test-api-key&artist=Radiohead&album=OK%20Computer&username=testuser&format=json"))
                .andRespond(withServerError());

        var result = lastFmClient.fetchAlbumPlayCount("Radiohead", "OK Computer");

        assertThat(result).isEmpty();
        mockServer.verify();
    }

    @Test
    void shouldReturnZeroPlayCountWhenUserHasNotListened() {
        var responseJson = """
                {
                    "album": {
                        "name": "Some Album",
                        "artist": "Some Artist",
                        "userplaycount": "0"
                    }
                }
                """;
        mockServer.expect(requestTo(
                        "https://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=test-api-key&artist=Some%20Artist&album=Some%20Album&username=testuser&format=json"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        var result = lastFmClient.fetchAlbumPlayCount("Some Artist", "Some Album");

        assertThat(result).isPresent();
        assertThat(result.getAsInt()).isEqualTo(0);
        mockServer.verify();
    }

    @Test
    void shouldReturnEmptyWhenUserPlayCountMissing() {
        var responseJson = """
                {
                    "album": {
                        "name": "OK Computer",
                        "artist": "Radiohead",
                        "playcount": "5000000"
                    }
                }
                """;
        mockServer.expect(requestTo(
                        "https://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=test-api-key&artist=Radiohead&album=OK%20Computer&username=testuser&format=json"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        var result = lastFmClient.fetchAlbumPlayCount("Radiohead", "OK Computer");

        assertThat(result).isEmpty();
        mockServer.verify();
    }
}
