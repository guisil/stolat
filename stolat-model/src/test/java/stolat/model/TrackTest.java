package stolat.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class TrackTest {

    private final Album album =
            new Album(
                    UUID.randomUUID().toString(), "Some Album",
                    List.of(UUID.randomUUID().toString()), List.of("Some Artist"));

    @Test
    void shouldCreateTrackWhenTagsAreValid() {
        new Track(
                UUID.randomUUID().toString(), "1",
                "Some Track", 123,
                "some/path/to/track.flac", album);
    }

    @Test
    void shouldCreateTrackWhenTagsAreValidAndDiscNumberPresent() {
        new Track(
                UUID.randomUUID().toString(), "1", "1",
                "Some Track", 123,
                "some/path/to/track.flac",
                new Album(
                        UUID.randomUUID().toString(), "Some Album",
                        List.of(UUID.randomUUID().toString()), List.of("Some Artist")));
    }

    @Test
    void shouldNotCreateTrackWhenTrackMbidIsInvalid() {
        assertThrows(IllegalArgumentException.class, () ->
                new Track(
                        "invalid UUID", "1",
                        "Some Track", 123,
                        "some/path/to/track.flac", album));
    }

    @Test
    void shouldNotCreateTrackWhenTrackMbidIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                new Track(
                        "", "1",
                        "Some Track", 123,
                        "some/path/to/track.flac", album));
    }

    @Test
    void shouldNotCreateTrackWhenTrackMbidIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Track(
                        null, "1",
                        "Some Track", 123,
                        "some/path/to/track.flac", album));
    }

    @Test
    void shouldNotCreateTrackWhenDiscNumberIsNotANumber() {
        assertThrows(IllegalArgumentException.class, () ->
                new Track(
                        UUID.randomUUID().toString(), "A", "1",
                        "Some Track", 123,
                        "some/path/to/track.flac", album));
    }

    @Test
    void shouldNotCreateTrackWhenDiscNumberIsAnInvalidNumber() {
        assertThrows(IllegalArgumentException.class, () ->
                new Track(
                        UUID.randomUUID().toString(), "-1", "1",
                        "Some Track", 123,
                        "some/path/to/track.flac", album));
    }

    @Test
    void shouldStillCreateTrackWhenDiscNumberIsEmpty() {
        new Track(
                UUID.randomUUID().toString(), "", "1",
                "Some Track", 123,
                "some/path/to/track.flac", album);
    }

    @Test
    void shouldStillCreateTrackWhenDiscNumberIsNull() {
        new Track(
                UUID.randomUUID().toString(), null, "1",
                "Some Track", 123,
                "some/path/to/track.flac", album);
    }

    @Test
    void shouldNotCreateTrackWhenTrackNumberIsNotANumber() {
        assertThrows(IllegalArgumentException.class, () ->
                new Track(
                        UUID.randomUUID().toString(), "A",
                        "Some Track", 123,
                        "some/path/to/track.flac", album));
    }

    @Test
    void shouldNotCreateTrackWhenTrackNumberIsAnInvalidNumber() {
        assertThrows(IllegalArgumentException.class, () ->
                new Track(
                        UUID.randomUUID().toString(), "-1",
                        "Some Track", 123,
                        "some/path/to/track.flac", album));
    }

    @Test
    void shouldNotCreateTrackWhenTrackNumberIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                new Track(
                        UUID.randomUUID().toString(), "",
                        "Some Track", 123,
                        "some/path/to/track.flac", album));
    }

    @Test
    void shouldNotCreateTrackWhenTrackNumberIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Track(
                        UUID.randomUUID().toString(), null,
                        "Some Track", 123,
                        "some/path/to/track.flac", album));
    }

    @Test
    void shouldNotCreateTrackWhenTrackNameIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                new Track(
                        UUID.randomUUID().toString(), "1",
                        "", 123,
                        "some/path/to/track.flac", album));
    }

    @Test
    void shouldNotCreateTrackWhenTrackNameIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Track(
                        UUID.randomUUID().toString(), "1",
                        null, 123,
                        "some/path/to/track.flac", album));
    }

    @Test
    void shouldNotCreateTrackWhenTrackLengthIsAnInvalidNumber() {
        assertThrows(IllegalArgumentException.class, () ->
                new Track(
                        UUID.randomUUID().toString(), "1",
                        "Some Track", -1,
                        "some/path/to/track.flac", album));
    }

    @Test
    void shouldNotCreateTrackWhenTrackRelativePathIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                new Track(
                        UUID.randomUUID().toString(), "1",
                        "Some Track", 123,
                        "", album));
    }

    @Test
    void shouldNotCreateTrackWhenTrackRelativePathIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Track(
                        UUID.randomUUID().toString(), "1",
                        "Some Track", 123,
                        null, album));
    }

    @Test
    void shouldNotCreateTrackWhenAlbumIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Track(
                        UUID.randomUUID().toString(), "1",
                        "Some Track", 123,
                        "some/path/to/track.flac", null));
    }
}