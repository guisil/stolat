package app.stolat.birthday.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class DiscogsReleaseDateLookupTest {

    private MockRestServiceServer mockServer;
    private DiscogsReleaseDateLookup discogsLookup;

    @BeforeEach
    void setUp() {
        var builder = RestClient.builder().baseUrl("https://api.discogs.com");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        discogsLookup = new DiscogsReleaseDateLookup(builder.build());
    }

    @Test
    void shouldReturnFullDateWhenReleasedFieldHasDayPrecision() {
        var responseJson = """
                {
                    "id": 12345,
                    "title": "OK Computer",
                    "released": "1997-06-16"
                }
                """;
        mockServer.expect(requestTo("https://api.discogs.com/releases/12345"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        var result = discogsLookup.lookUp(12345L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(LocalDate.of(1997, 6, 16));
        mockServer.verify();
    }

    @Test
    void shouldReturnFirstOfMonthWhenReleasedFieldHasMonthPrecision() {
        var responseJson = """
                {
                    "id": 12345,
                    "title": "Some Album",
                    "released": "1997-06-00"
                }
                """;
        mockServer.expect(requestTo("https://api.discogs.com/releases/12345"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        var result = discogsLookup.lookUp(12345L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(LocalDate.of(1997, 6, 1));
        mockServer.verify();
    }

    @Test
    void shouldReturnJanFirstWhenReleasedFieldHasYearOnly() {
        var responseJson = """
                {
                    "id": 12345,
                    "title": "Some Album",
                    "released": "1997"
                }
                """;
        mockServer.expect(requestTo("https://api.discogs.com/releases/12345"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        var result = discogsLookup.lookUp(12345L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(LocalDate.of(1997, 1, 1));
        mockServer.verify();
    }

    @Test
    void shouldReturnEmptyWhenReleasedFieldIsMissing() {
        var responseJson = """
                {
                    "id": 12345,
                    "title": "Some Album"
                }
                """;
        mockServer.expect(requestTo("https://api.discogs.com/releases/12345"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        var result = discogsLookup.lookUp(12345L);

        assertThat(result).isEmpty();
        mockServer.verify();
    }

    @Test
    void shouldReturnEmptyWhenReleasedFieldIsEmpty() {
        var responseJson = """
                {
                    "id": 12345,
                    "title": "Some Album",
                    "released": ""
                }
                """;
        mockServer.expect(requestTo("https://api.discogs.com/releases/12345"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        var result = discogsLookup.lookUp(12345L);

        assertThat(result).isEmpty();
        mockServer.verify();
    }

    @Test
    void shouldReturnEmptyOnApiError() {
        mockServer.expect(requestTo("https://api.discogs.com/releases/12345"))
                .andRespond(withServerError());

        var result = discogsLookup.lookUp(12345L);

        assertThat(result).isEmpty();
        mockServer.verify();
    }

    @Test
    void shouldReturnEmptyWhenResponseIsNull() {
        mockServer.expect(requestTo("https://api.discogs.com/releases/12345"))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        var result = discogsLookup.lookUp(12345L);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldHaveBetterPrecisionThanYearOnly() {
        var responseJson = """
                {
                    "id": 99999,
                    "title": "Album With Full Date",
                    "released": "2020-03-15"
                }
                """;
        mockServer.expect(requestTo("https://api.discogs.com/releases/99999"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        var result = discogsLookup.lookUp(99999L);

        assertThat(result).isPresent();
        assertThat(result.get().getMonthValue()).isEqualTo(3);
        assertThat(result.get().getDayOfMonth()).isEqualTo(15);
        mockServer.verify();
    }
}
