package stolat.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ArtistTest {

    @Test
    void shouldCreateArtistWhenTagsAreValid() {
        new Artist(
                UUID.randomUUID().toString(), "Some Artist");
    }

    @Test
    void shouldNotCreateArtistWhenArtistMbidIsInvalid() {
        assertThrows(IllegalArgumentException.class, () ->
                new Artist(
                        "invalid UUID", "Some Artist"));
    }

    @Test
    void shouldNotCreateArtistWhenArtistMbidIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                new Artist(
                        "", "Some Artist"));
    }

    @Test
    void shouldNotCreateArtistWhenArtistMbidIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Artist(
                        (String) null, "Some Artist"));
    }

    @Test
    void shouldNotCreateArtisthenArtistNameIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                new Artist(
                        UUID.randomUUID().toString(), ""));
    }

    @Test
    void shouldNotCreateArtistWhenArtistNameIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Artist(
                        UUID.randomUUID().toString(), null));
    }
}