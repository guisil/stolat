package stolat.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AlbumBirthdayTest {

    @Test
    void shouldCreateAlbumBirthdayWhenAllValuesArePresent() {
        new AlbumBirthday(
                new Album(
                        UUID.randomUUID(), "Some Album",
                        UUID.randomUUID(), "Some Artist"),
                2000, 4, 30);
    }

    @Test
    void shouldCreateAlbumBirthdayWhenDayIsNotPresent() {
        new AlbumBirthday(
                new Album(
                        UUID.randomUUID(), "Some Album",
                        UUID.randomUUID(), "Some Artist"),
                2000, 12, null);
    }

    @Test
    void shouldCreateAlbumBirthdayWhenDayAndMonthAreNotPresent() {
        new AlbumBirthday(
                new Album(
                        UUID.randomUUID(), "Some Album",
                        UUID.randomUUID(), "Some Artist"),
                2000, null, null);
    }

    @Test
    void shouldNotCreateAlbumBirthdayWhenAlbumIsNotPresent() {
        assertThrows(IllegalArgumentException.class, () -> new AlbumBirthday(
                null, 2000, null, null));
    }

    @Test
    void shouldNotCreateAlbumBirthdayWhenYearIsNotPresent() {
        assertThrows(IllegalArgumentException.class, () -> new AlbumBirthday(
                new Album(
                        UUID.randomUUID(), "Some Album",
                        UUID.randomUUID(), "Some Artist"),
                null, null, null));
    }

    @Test
    void shouldNotCreateAlbumWithInvalidMonth() {
        assertThrows(IllegalArgumentException.class, () -> {
            final var albumBirthday = new AlbumBirthday(
                    new Album(
                            UUID.randomUUID(), "Some Album",
                            UUID.randomUUID(), "Some Artist"),
                    2000, 13, null);
        });
    }

    @Test
    void shouldNotCreateAlbumWithMonthInvalidForDay() {
        assertThrows(IllegalArgumentException.class, () -> {
            final var albumBirthday = new AlbumBirthday(
                    new Album(
                            UUID.randomUUID(), "Some Album",
                            UUID.randomUUID(), "Some Artist"),
                    2000, null, 31);
        });
    }

    @Test
    void shouldNotCreateAlbumWithInvalidDay() {
        assertThrows(IllegalArgumentException.class, () -> {
            final var albumBirthday = new AlbumBirthday(
                    new Album(
                            UUID.randomUUID(), "Some Album",
                            UUID.randomUUID(), "Some Artist"),
                    2000, 4, 55);
        });
    }

    @Test
    void shouldNotCreateAlbumWithDayInvalidForMonth() {
        assertThrows(IllegalArgumentException.class, () -> {
            final var albumBirthday = new AlbumBirthday(
                    new Album(
                            UUID.randomUUID(), "Some Album",
                            UUID.randomUUID(), "Some Artist"),
                    2000, 4, 31);
        });
    }
}