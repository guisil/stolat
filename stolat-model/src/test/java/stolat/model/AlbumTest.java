package stolat.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AlbumTest {

    @Test
    void shouldCreateAlbumWhenTagsAreValid() {
        new Album(
                UUID.randomUUID().toString(), "Some Album",
                List.of(UUID.randomUUID().toString()), List.of("Some Artist"));
    }

    @Test
    void shouldCreateAlbumWithMultipleArtistsWhenTagsAreValid() {
        new Album(
                UUID.randomUUID().toString(), "Some Album",
                List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString()), List.of("Some Artist", "Some Other Artist"));
    }

    @Test
    void shouldNotCreateAlbumWhenAlbumMbidIsInvalid() {
        assertThrows(IllegalArgumentException.class, () ->
                new Album(
                        "invalid UUID", "Some Album",
                        List.of(UUID.randomUUID().toString()), List.of("Some Artist")));
    }

    @Test
    void shouldNotCreateAlbumWhenAlbumMbidIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                new Album(
                        "", "Some Album",
                        List.of(UUID.randomUUID().toString()), List.of("Some Artist")));
    }

    @Test
    void shouldNotCreateAlbumWhenAlbumMbidIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Album(
                        null, "Some Album",
                        List.of(UUID.randomUUID().toString()), List.of("Some Artist")));
    }

    @Test
    void shouldNotCreateAlbumWhenAlbumNameIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                new Album(
                        UUID.randomUUID().toString(), "",
                        List.of(UUID.randomUUID().toString()), List.of("Some Artist")));
    }

    @Test
    void shouldNotCreateAlbumWhenAlbumNameIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Album(
                        UUID.randomUUID().toString(), null,
                        List.of(UUID.randomUUID().toString()), List.of("Some Artist")));
    }

    @Test
    void shouldNotCreateAlbumWhenArtistMbidIsInvalid() {
        assertThrows(IllegalArgumentException.class, () ->
                new Album(
                        UUID.randomUUID().toString(), "Some Album",
                        List.of("invalid UUID"), List.of("Some Artist")));
    }

    @Test
    void shouldNotCreateAlbumWhenOneOfArtistMbidIsInvalid() {
        assertThrows(IllegalArgumentException.class, () ->
                new Album(
                        UUID.randomUUID().toString(), "Some Album",
                        List.of(UUID.randomUUID().toString(), "invalid UUID"), List.of("Some Artist", "Some Other Artist")));
    }

    @Test
    void shouldNotCreateAlbumWhenArtistMbidIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                new Album(
                        UUID.randomUUID().toString(), "Some Album",
                        List.of(""), List.of("Some Artist")));
    }

    @Test
    void shouldNotCreateAlbumWhenOneOfArtistMbidIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                new Album(
                        UUID.randomUUID().toString(), "Some Album",
                        List.of("", UUID.randomUUID().toString()), List.of("Some Artist", "Some Other Artist")));
    }

    @Test
    void shouldNotCreateAlbumWhenArtistMbidIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Album(
                        UUID.randomUUID().toString(), "Some Album",
                        Collections.singletonList(null), List.of("Some Artist")));
    }

    @Test
    void shouldNotCreateAlbumWhenOneOfArtistMbidIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Album(
                        UUID.randomUUID().toString(), "Some Album",
                        Arrays.asList(UUID.randomUUID().toString(), null), List.of("Some Artist", "Some Other Artist")));
    }

    @Test
    void shouldNotCreateAlbumWhenArtistNameIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                new Album(
                        UUID.randomUUID().toString(), "Some Album",
                        List.of(UUID.randomUUID().toString()), List.of("")));
    }

    @Test
    void shouldNotCreateAlbumWhenOneOfArtistNameIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                new Album(
                        UUID.randomUUID().toString(), "Some Album",
                        List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString()), List.of("", "Some Other Artist")));
    }

    @Test
    void shouldNotCreateAlbumWhenArtistNameIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Album(
                        UUID.randomUUID().toString(), "Some Album",
                        List.of(UUID.randomUUID().toString()), Collections.singletonList(null)));
    }

    @Test
    void shouldNotCreateAlbumWhenOneOfArtistNameIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Album(
                        UUID.randomUUID().toString(), "Some Album",
                        List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString()), Arrays.asList("Some Artist", null)));
    }

    @Test
    void shouldNotCreateAlbumWhenArtistIDsAndNamesHaveDifferentSize() {
        assertThrows(IllegalArgumentException.class, () ->
                new Album(
                        UUID.randomUUID().toString(), "Some Album",
                        List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString()), List.of("Some Artist")
                ));
    }
}