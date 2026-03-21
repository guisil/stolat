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
        volumioClient = new VolumioClient(builder.build());
    }

    @Test
    void shouldPlayAlbumWhenFoundAsFolder() {
        var searchResponseJson = """
                {
                    "navigation": {
                        "lists": [
                            {
                                "items": [
                                    {
                                        "type": "folder",
                                        "title": "OK Computer",
                                        "uri": "music-library/FLAC/Radiohead/OK Computer"
                                    }
                                ]
                            }
                        ]
                    }
                }
                """;
        var browseResponseJson = """
                {
                    "navigation": {
                        "lists": [
                            {
                                "items": [
                                    {
                                        "type": "song",
                                        "title": "Airbag",
                                        "uri": "music-library/FLAC/Radiohead/OK Computer/01 - Airbag.flac",
                                        "service": "mpd",
                                        "album": "OK Computer",
                                        "artist": "Radiohead"
                                    },
                                    {
                                        "type": "song",
                                        "title": "Paranoid Android",
                                        "uri": "music-library/FLAC/Radiohead/OK Computer/02 - Paranoid Android.flac",
                                        "service": "mpd",
                                        "album": "OK Computer",
                                        "artist": "Radiohead"
                                    }
                                ]
                            }
                        ]
                    }
                }
                """;

        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/api/v1/search")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(searchResponseJson, MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/api/v1/browse")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(browseResponseJson, MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/api/v1/replaceAndPlay")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        volumioClient.playAlbum("OK Computer", "Radiohead");

        mockServer.verify();
    }

    @Test
    void shouldPlayAlbumWhenFoundAsSongs() {
        var searchResponseJson = """
                {
                    "navigation": {
                        "lists": [
                            {
                                "items": [
                                    {
                                        "type": "song",
                                        "title": "Airbag",
                                        "album": "OK Computer",
                                        "uri": "music-library/FLAC/Radiohead/OK Computer/01 - Airbag.flac",
                                        "service": "mpd"
                                    },
                                    {
                                        "type": "song",
                                        "title": "Paranoid Android",
                                        "album": "OK Computer",
                                        "uri": "music-library/FLAC/Radiohead/OK Computer/02 - Paranoid Android.flac",
                                        "service": "mpd"
                                    }
                                ]
                            }
                        ]
                    }
                }
                """;

        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/api/v1/search")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(searchResponseJson, MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/api/v1/replaceAndPlay")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        volumioClient.playAlbum("OK Computer", "Radiohead");

        mockServer.verify();
    }

    @Test
    void shouldHandleAlbumNotFound() {
        var searchResponseJson = """
                {
                    "navigation": {
                        "lists": [
                            {
                                "items": [
                                    {
                                        "type": "folder",
                                        "title": "Different Album",
                                        "uri": "music-library/FLAC/SomeArtist/Different Album"
                                    }
                                ]
                            }
                        ]
                    }
                }
                """;

        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/api/v1/search")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(searchResponseJson, MediaType.APPLICATION_JSON));

        volumioClient.playAlbum("OK Computer", "Radiohead");

        mockServer.verify();
    }

    @Test
    void shouldHandleSearchError() {
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/api/v1/search")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        volumioClient.playAlbum("OK Computer", "Radiohead");

        mockServer.verify();
    }
}
