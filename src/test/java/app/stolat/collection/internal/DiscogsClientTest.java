package app.stolat.collection.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class DiscogsClientTest {

    private MockRestServiceServer mockServer;
    private DiscogsClient client;

    @BeforeEach
    void setUp() {
        var builder = RestClient.builder().baseUrl("https://api.discogs.com");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new DiscogsClient(builder.build());
    }

    @Test
    void shouldFetchCollectionFromDiscogs() {
        var responseJson = """
                {
                    "pagination": { "pages": 1, "page": 1 },
                    "releases": [
                        {
                            "basic_information": {
                                "id": 12345,
                                "title": "OK Computer",
                                "artists": [{ "name": "Radiohead" }]
                            }
                        },
                        {
                            "basic_information": {
                                "id": 67890,
                                "title": "Kid A",
                                "artists": [{ "name": "Radiohead" }]
                            }
                        }
                    ]
                }
                """;
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/users/testuser/collection/")))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        var releases = client.fetchCollection("testuser");

        assertThat(releases).hasSize(2);
        assertThat(releases.get(0).discogsId()).isEqualTo(12345L);
        assertThat(releases.get(0).albumTitle()).isEqualTo("OK Computer");
        assertThat(releases.get(0).artistName()).isEqualTo("Radiohead");
        assertThat(releases.get(1).discogsId()).isEqualTo(67890L);
        assertThat(releases.get(1).albumTitle()).isEqualTo("Kid A");
        mockServer.verify();
    }

    @Test
    void shouldHandlePagination() {
        var page1Json = """
                {
                    "pagination": { "pages": 2, "page": 1 },
                    "releases": [
                        {
                            "basic_information": {
                                "id": 12345,
                                "title": "OK Computer",
                                "artists": [{ "name": "Radiohead" }]
                            }
                        }
                    ]
                }
                """;
        var page2Json = """
                {
                    "pagination": { "pages": 2, "page": 2 },
                    "releases": [
                        {
                            "basic_information": {
                                "id": 67890,
                                "title": "Kid A",
                                "artists": [{ "name": "Radiohead" }]
                            }
                        }
                    ]
                }
                """;
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("page=1")))
                .andRespond(withSuccess(page1Json, MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("page=2")))
                .andRespond(withSuccess(page2Json, MediaType.APPLICATION_JSON));

        var releases = client.fetchCollection("testuser");

        assertThat(releases).hasSize(2);
        assertThat(releases.get(0).albumTitle()).isEqualTo("OK Computer");
        assertThat(releases.get(1).albumTitle()).isEqualTo("Kid A");
        mockServer.verify();
    }

    @Test
    void shouldThrowOnFetchError() {
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/users/testuser/collection/")))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.fetchCollection("testuser"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to fetch Discogs collection page 1");
    }

    @Test
    void shouldExtractReleaseYearFromDiscogsResponse() {
        var responseJson = """
                {
                    "pagination": { "pages": 1, "page": 1 },
                    "releases": [
                        {
                            "basic_information": {
                                "id": 12345,
                                "title": "OK Computer",
                                "artists": [{ "name": "Radiohead" }],
                                "year": 1997
                            }
                        }
                    ]
                }
                """;
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/users/testuser/collection/")))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        var releases = client.fetchCollection("testuser");

        assertThat(releases).hasSize(1);
        assertThat(releases.getFirst().year()).isEqualTo(1997);
        mockServer.verify();
    }

    @Test
    void shouldTreatZeroYearAsNull() {
        var responseJson = """
                {
                    "pagination": { "pages": 1, "page": 1 },
                    "releases": [
                        {
                            "basic_information": {
                                "id": 12345,
                                "title": "Unknown Release",
                                "artists": [{ "name": "Some Artist" }],
                                "year": 0
                            }
                        }
                    ]
                }
                """;
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/users/testuser/collection/")))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        var releases = client.fetchCollection("testuser");

        assertThat(releases).hasSize(1);
        assertThat(releases.getFirst().year()).isNull();
        mockServer.verify();
    }

    @Test
    void shouldStripNonNumericDiscogsArtistDisambiguation() {
        var responseJson = """
                {
                    "pagination": { "pages": 1, "page": 1 },
                    "releases": [
                        {
                            "basic_information": {
                                "id": 11111,
                                "title": "Some Album",
                                "artists": [{ "name": "The Band (UK)" }]
                            }
                        }
                    ]
                }
                """;
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/users/testuser/collection/")))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        var releases = client.fetchCollection("testuser");

        assertThat(releases).hasSize(1);
        assertThat(releases.getFirst().artistName()).isEqualTo("The Band");
        mockServer.verify();
    }

    @Test
    void shouldStripDiscogsArtistDisambiguation() {
        var responseJson = """
                {
                    "pagination": { "pages": 1, "page": 1 },
                    "releases": [
                        {
                            "basic_information": {
                                "id": 11111,
                                "title": "OK Computer",
                                "artists": [{ "name": "Radiohead (2)" }]
                            }
                        }
                    ]
                }
                """;
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/users/testuser/collection/")))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        var releases = client.fetchCollection("testuser");

        assertThat(releases).hasSize(1);
        assertThat(releases.getFirst().artistName()).isEqualTo("Radiohead");
        mockServer.verify();
    }
}
