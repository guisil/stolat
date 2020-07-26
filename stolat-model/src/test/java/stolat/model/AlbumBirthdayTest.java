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
                2000, 12, 22);
    }

    @Test
    void shouldCreateAlbumBirthdayWhenDayIsNotPresent() {
        new AlbumBirthday(
                new Album(
                        UUID.randomUUID(), "Some Album",
                        UUID.randomUUID(), "Some Artist"),
                2000, 12);
    }

    @Test
    void shouldCreateAlbumBirthdayWhenDayAndMonthAreNotPresent() {
        new AlbumBirthday(
                new Album(
                        UUID.randomUUID(), "Some Album",
                        UUID.randomUUID(), "Some Artist"),
                2000);
    }

    @Test
    void shouldNotCreateAlbumBirthdayWhenAlbumIsNotPresent() {
        assertThrows(IllegalArgumentException.class, () -> new AlbumBirthday(
                null, 2000));
    }
}