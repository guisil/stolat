package stolat.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AlbumBirthdayTest {

    @Test
    void shouldCreateAlbumBirthdayWhenAllValuesArePresent() {
        new AlbumBirthday(
                UUID.randomUUID(), UUID.randomUUID(), 2000, 12, 22);
    }

    @Test
    void shouldCreateAlbumBirthdayWhenDayIsNotPresent() {
        new AlbumBirthday(
                UUID.randomUUID(), UUID.randomUUID(), 2000, 12);
    }

    @Test
    void shouldCreateAlbumBirthdayWhenDayAndMonthAreNotPresent() {
        new AlbumBirthday(
                UUID.randomUUID(), UUID.randomUUID(), 2000);
    }

    @Test
    void shouldNotCreateAlbumBirthdayWhenAlbumMbidIsNotPresent() {
        assertThrows(IllegalArgumentException.class, () -> new AlbumBirthday(
                null, UUID.randomUUID(), 2000));
    }

    @Test
    void shouldNotCreateAlbumBirthdayWhenArtistMbidIsNotPresent() {
        assertThrows(IllegalArgumentException.class, () -> new AlbumBirthday(
                UUID.randomUUID(), null, 2000));
    }
}