package stolat.model;

import org.junit.jupiter.api.Test;

import java.time.MonthDay;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class BirthdayAlbumsTest {

    @Test
    void shouldCreateBirthdayAlbumsWhenAllValuesArePresent() {
        new BirthdayAlbums(MonthDay.of(7, 25),
                MonthDay.of(7, 27),
                List.of(new AlbumBirthday(
                        new Album(
                                UUID.randomUUID(), "Some Album",
                                UUID.randomUUID(), "Some Artist"),
                        2000, 12, 22)));
    }

    @Test
    void shouldNotCreateBirthdayAlbumsWhenFromDateIsNotPresent() {
        assertThrows(IllegalArgumentException.class, () -> new BirthdayAlbums(
                null,
                MonthDay.of(7, 27),
                List.of(new AlbumBirthday(
                        null, 2000))));
    }

    @Test
    void shouldNotCreateBirthdayAlbumsWhenToDateIsNotPresent() {
        assertThrows(IllegalArgumentException.class, () -> new BirthdayAlbums(
                MonthDay.of(7, 25),
                null,
                List.of(new AlbumBirthday(
                        null, 2000))));
    }

    @Test
    void shouldNotCreateBirthdayAlbumsWhenAlbumBirthdayListIsNotPresent() {
        assertThrows(IllegalArgumentException.class, () -> new BirthdayAlbums(
                MonthDay.of(7, 25),
                MonthDay.of(7, 27),
                null));
    }

    @Test
    void shouldNotAllowSettingNullFromDate() {
        assertThrows(IllegalArgumentException.class, () -> {
            final var birthdayAlbums = new BirthdayAlbums();
            birthdayAlbums.setFrom(null);
        });
    }

    @Test
    void shouldNotAllowSettingNullToDate() {
        assertThrows(IllegalArgumentException.class, () -> {
            final var birthdayAlbums = new BirthdayAlbums();
            birthdayAlbums.setTo(null);
        });
    }

    @Test
    void shouldNotAllowSettingNullAlbumBirthdays() {
        assertThrows(IllegalArgumentException.class, () -> {
            final var birthdayAlbums = new BirthdayAlbums();
            birthdayAlbums.setAlbumBirthdays(null);
        });
    }
}