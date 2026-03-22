package app.stolat.collection.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MusicBrainzSearchClientTest {

    private MockRestServiceServer mockServer;
    private MusicBrainzSearchClient client;

    @BeforeEach
    void setUp() {
        var builder = RestClient.builder().baseUrl("https://musicbrainz.org/ws/2");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new MusicBrainzSearchClient(builder.build(), 90);
    }

    @Test
    void shouldFindReleaseGroupByArtistAndTitle() {
        var mbid = UUID.fromString("b1392450-e666-3926-a536-22c65f834433");
        var responseJson = """
                {
                    "release-groups": [
                        {
                            "id": "b1392450-e666-3926-a536-22c65f834433",
                            "title": "OK Computer",
                            "score": 100
                        }
                    ]
                }
                """;
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/release-group/")))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        var result = client.searchReleaseGroup("Radiohead", "OK Computer");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(mbid);
        mockServer.verify();
    }

    @Test
    void shouldReturnEmptyWhenScoreTooLow() {
        var responseJson = """
                {
                    "release-groups": [
                        {
                            "id": "b1392450-e666-3926-a536-22c65f834433",
                            "title": "OK Computer",
                            "score": 50
                        }
                    ]
                }
                """;
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/release-group/")))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        var result = client.searchReleaseGroup("Radiohead", "OK Computer");

        assertThat(result).isEmpty();
        mockServer.verify();
    }

    @Test
    void shouldUseConfigurableScoreThreshold() {
        var builder2 = RestClient.builder().baseUrl("https://musicbrainz.org/ws/2");
        var mockServer2 = MockRestServiceServer.bindTo(builder2).build();
        var clientWith75 = new MusicBrainzSearchClient(builder2.build(), 75);

        var responseJson = """
                {
                    "release-groups": [
                        {
                            "id": "b1392450-e666-3926-a536-22c65f834433",
                            "title": "OK Computer",
                            "score": 80
                        }
                    ]
                }
                """;
        mockServer2.expect(requestTo(org.hamcrest.Matchers.containsString("/release-group/")))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        var result = clientWith75.searchReleaseGroup("Radiohead", "OK Computer");

        assertThat(result).isPresent();
        mockServer2.verify();
    }

    @Test
    void shouldReturnEmptyOnError() {
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/release-group/")))
                .andRespond(withServerError());

        var result = client.searchReleaseGroup("Radiohead", "OK Computer");

        assertThat(result).isEmpty();
    }
}
