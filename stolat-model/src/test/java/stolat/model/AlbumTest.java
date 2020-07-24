package stolat.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AlbumTest {

    @Test
    void shouldCreateAlbumWhenTagsAreValid() {
        new Album(
                UUID.randomUUID().toString(), "Some Album",
                UUID.randomUUID().toString(), "Some Artist");
    }

    @Test
    void shouldNotCreateAlbumWhenAlbumMbidIsInvalid() {
        assertThrows(IllegalArgumentException.class, () ->
                new Album(
                        "invalid UUID", "Some Album",
                        UUID.randomUUID().toString(), "Some Artist"));
    }

    @Test
    void shouldNotCreateAlbumWhenAlbumMbidIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                new Album(
                        "", "Some Album",
                        UUID.randomUUID().toString(), "Some Artist"));
    }

    @Test
    void shouldNotCreateAlbumWhenAlbumMbidIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Album(
                        null, "Some Album",
                        UUID.randomUUID().toString(), "Some Artist"));
    }

    @Test
    void shouldNotCreateAlbumWhenAlbumNameIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                new Album(
                        UUID.randomUUID().toString(), "",
                        UUID.randomUUID().toString(), "Some Artist"));
    }

    @Test
    void shouldNotCreateAlbumWhenAlbumNameIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Album(
                        UUID.randomUUID().toString(), null,
                        UUID.randomUUID().toString(), "Some Artist"));
    }

    @Test
    void shouldNotCreateAlbumWhenArtistMbidIsInvalid() {
        assertThrows(IllegalArgumentException.class, () ->
                new Album(
                        UUID.randomUUID().toString(), "Some Album",
                        "invalid UUID", "Some Artist"));
    }

    @Test
    void shouldNotCreateAlbumWhenArtistMbidIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                new Album(
                        UUID.randomUUID().toString(), "Some Album",
                        "", "Some Artist"));
    }

    @Test
    void shouldNotCreateAlbumWhenArtistMbidIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Album(
                        UUID.randomUUID().toString(), "Some Album",
                        null, "Some Artist"));
    }

    @Test
    void shouldNotCreateAlbumWhenArtistNameIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                new Album(
                        UUID.randomUUID().toString(), "Some Album",
                        UUID.randomUUID().toString(), ""));
    }

    @Test
    void shouldNotCreateAlbumWhenArtistNameIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Album(
                        UUID.randomUUID().toString(), "Some Album",
                        UUID.randomUUID().toString(), null));
    }
}