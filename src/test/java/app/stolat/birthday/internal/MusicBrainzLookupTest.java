package app.stolat.birthday.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;

class MusicBrainzLookupTest {

    private MockRestServiceServer mockServer;
    private MusicBrainzLookup musicBrainzLookup;

    @BeforeEach
    void setUp() {
        var builder = RestClient.builder().baseUrl("https://musicbrainz.org/ws/2");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        musicBrainzLookup = new MusicBrainzLookup(builder.build());
    }

    @Test
    void shouldReturnReleaseDateFromMusicBrainzApi() {
        var mbid = UUID.fromString("b1392450-e666-3926-a536-22c65f834433");
        var responseJson = """
                {
                    "id": "b1392450-e666-3926-a536-22c65f834433",
                    "title": "OK Computer",
                    "primary-type": "Album",
                    "first-release-date": "1997-06-16"
                }
                """;
        mockServer.expect(requestTo("https://musicbrainz.org/ws/2/release-group/" + mbid + "?fmt=json"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        var result = musicBrainzLookup.lookUp(mbid);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(LocalDate.of(1997, 6, 16));
        mockServer.verify();
    }

    @Test
    void shouldReturnEmptyWhenReleaseDateMissing() {
        var mbid = UUID.randomUUID();
        var responseJson = """
                {
                    "id": "%s",
                    "title": "Some Album",
                    "primary-type": "Album",
                    "first-release-date": ""
                }
                """.formatted(mbid);
        mockServer.expect(requestTo("https://musicbrainz.org/ws/2/release-group/" + mbid + "?fmt=json"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        var result = musicBrainzLookup.lookUp(mbid);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyOnApiError() {
        var mbid = UUID.randomUUID();
        mockServer.expect(requestTo("https://musicbrainz.org/ws/2/release-group/" + mbid + "?fmt=json"))
                .andRespond(withServerError());

        var result = musicBrainzLookup.lookUp(mbid);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldHandlePartialDate() {
        var mbid = UUID.randomUUID();
        var responseJson = """
                {
                    "id": "%s",
                    "title": "Some Album",
                    "first-release-date": "1997"
                }
                """.formatted(mbid);
        mockServer.expect(requestTo("https://musicbrainz.org/ws/2/release-group/" + mbid + "?fmt=json"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        var result = musicBrainzLookup.lookUp(mbid);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(LocalDate.of(1997, 1, 1));
    }
}
