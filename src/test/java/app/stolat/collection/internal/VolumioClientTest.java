package app.stolat.collection.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class VolumioClientTest {

    private MockRestServiceServer mockServer;
    private VolumioClient volumioClient;

    @BeforeEach
    void setUp() {
        var builder = RestClient.builder().baseUrl("http://volumio.local");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        volumioClient = new VolumioClient(builder.build(), "music-library/NAS");
    }

    @Test
    void shouldPlayAlbumDirectlyWhenFolderPathProvided() {
        var browseResponseJson = """
                {
                    "navigation": {
                        "lists": [
                            {
                                "items": [
                                    {
                                        "type": "song",
                                        "title": "Safe from Harm",
                                        "uri": "music-library/NAS/Massive Attack/[1991] Blue Lines/01 - Safe from Harm.flac",
                                        "service": "mpd",
                                        "album": "Blue Lines",
                                        "artist": "Massive Attack"
                                    },
                                    {
                                        "type": "song",
                                        "title": "One Love",
                                        "uri": "music-library/NAS/Massive Attack/[1991] Blue Lines/02 - One Love.flac",
                                        "service": "mpd",
                                        "album": "Blue Lines",
                                        "artist": "Massive Attack"
                                    }
                                ]
                            }
                        ]
                    }
                }
                """;

        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/api/v1/browse")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(browseResponseJson, MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/api/v1/replaceAndPlay")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        volumioClient.playAlbum("Blue Lines", "Massive Attack", "Massive Attack/[1991] Blue Lines");

        mockServer.verify();
    }

    @Test
    void shouldNotCallVolumioWhenNoFolderPathAvailable() {
        // No server expectations — no HTTP calls should be made
        volumioClient.playAlbum("OK Computer", "Radiohead", null);

        mockServer.verify();
    }

    @Test
    void shouldHandleBrowseError() {
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/api/v1/browse")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        volumioClient.playAlbum("Blue Lines", "Massive Attack", "Massive Attack/[1991] Blue Lines");

        mockServer.verify();
    }
}
