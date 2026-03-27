package app.stolat.birthday.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BandcampLookupTest {

    private MockRestServiceServer mockServer;
    private BandcampLookup bandcampLookup;

    @BeforeEach
    void setUp() {
        var builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        bandcampLookup = new BandcampLookup(builder.build());
    }

    @Test
    void shouldExtractReleaseDateFromJsonLd() {
        var html = """
                <html><head>
                <script type="application/ld+json">
                {
                    "@type": "MusicAlbum",
                    "name": "Kisses",
                    "datePublished": "19 Jan 2015 00:00:00 GMT",
                    "dateModified": "19 Jan 2015 12:58:13 GMT"
                }
                </script>
                </head><body></body></html>
                """;
        mockServer.expect(requestTo("https://anushka.bandcamp.com/album/kisses"))
                .andRespond(withSuccess(html, MediaType.TEXT_HTML));

        var result = bandcampLookup.lookUp("https://anushka.bandcamp.com/album/kisses");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(LocalDate.of(2015, 1, 19));
        mockServer.verify();
    }

    @Test
    void shouldReturnEmptyWhenNoJsonLd() {
        var html = "<html><head></head><body>No structured data</body></html>";
        mockServer.expect(requestTo("https://example.bandcamp.com/album/test"))
                .andRespond(withSuccess(html, MediaType.TEXT_HTML));

        var result = bandcampLookup.lookUp("https://example.bandcamp.com/album/test");

        assertThat(result).isEmpty();
        mockServer.verify();
    }

    @Test
    void shouldRejectNonBandcampUrl() {
        var result = bandcampLookup.lookUp("https://example.com/some-page");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFollowRedirectToCustomDomain() {
        var redirectUrl = "https://store.loversandlollypops.net/album/escabroso";
        var html = """
                <html><head>
                <script type="application/ld+json">
                {
                    "@type": "MusicAlbum",
                    "name": "Escabroso",
                    "datePublished": "16 Nov 2015 00:00:00 GMT"
                }
                </script>
                </head><body></body></html>
                """;
        mockServer.expect(requestTo("https://loversandlollypops.bandcamp.com/album/escabroso"))
                .andRespond(withStatus(HttpStatus.MOVED_PERMANENTLY)
                        .header(HttpHeaders.LOCATION, redirectUrl));
        mockServer.expect(requestTo(redirectUrl))
                .andRespond(withSuccess(html, MediaType.TEXT_HTML));

        var result = bandcampLookup.lookUp("https://loversandlollypops.bandcamp.com/album/escabroso");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(LocalDate.of(2015, 11, 16));
        mockServer.verify();
    }

    @Test
    void shouldReturnEmptyOnFetchError() {
        mockServer.expect(requestTo("https://example.bandcamp.com/album/test"))
                .andRespond(withServerError());

        var result = bandcampLookup.lookUp("https://example.bandcamp.com/album/test");

        assertThat(result).isEmpty();
    }
}
