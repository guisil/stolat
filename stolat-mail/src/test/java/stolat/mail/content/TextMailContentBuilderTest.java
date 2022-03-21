package stolat.mail.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import stolat.model.Album;
import stolat.model.AlbumBirthday;
import stolat.model.Artist;
import stolat.model.BirthdayAlbums;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class TextMailContentBuilderTest {

	private static final Artist SOME_ARTIST = new Artist(UUID.randomUUID(), "Some Artist");
    private static final String SOME_ARTIST_DISPLAY_NAME = SOME_ARTIST.getArtistName();
	private static final Artist ANOTHER_ARTIST = new Artist(UUID.randomUUID(), "Another Artist");
    private static final String SOME_AND_ANOTHER_ARTIST_DISPLAY_NAME = "Multiple Artists";
	
    private static final AlbumBirthday FIRST_ALBUM_BIRTHDAY =
            new AlbumBirthday(
                    new Album(
                            UUID.randomUUID(), "Some Album",
                            List.of(SOME_ARTIST), SOME_ARTIST_DISPLAY_NAME),
                    2000, 12, 22);
    private static final AlbumBirthday SECOND_ALBUM_BIRTHDAY =
            new AlbumBirthday(
                    new Album(
                            UUID.randomUUID(), "Some Other Album",
                            List.of(ANOTHER_ARTIST, SOME_ARTIST), SOME_AND_ANOTHER_ARTIST_DISPLAY_NAME),
                    2000, 12, 22);
    private static final BirthdayAlbums BIRTHDAY_ALBUMS =
            new BirthdayAlbums(
                    MonthDay.of(12, 22),
                    MonthDay.of(12, 22),
                    List.of(FIRST_ALBUM_BIRTHDAY, SECOND_ALBUM_BIRTHDAY));
    private static final BirthdayAlbums NO_RESULTS_BIRTHDAY_ALBUMS =
            new BirthdayAlbums(
                    MonthDay.of(1, 22),
                    MonthDay.of(1, 22),
                    Collections.emptyList());

    private static final DateTimeFormatter MONTH_DAY_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String BIRTHDAY_ALBUMS_STRING =
            SECOND_ALBUM_BIRTHDAY.getAlbum().getDisplayArtist() +
                    " - " + SECOND_ALBUM_BIRTHDAY.getAlbum().getAlbumName() +
                    " (" +
                    LocalDate.of(
                            SECOND_ALBUM_BIRTHDAY.getAlbumYear(),
                            SECOND_ALBUM_BIRTHDAY.getAlbumMonth(),
                            SECOND_ALBUM_BIRTHDAY.getAlbumDay())
                            .format(DATE_FORMATTER) +
                    ")" +
                    "\n" + FIRST_ALBUM_BIRTHDAY.getAlbum().getDisplayArtist() +
                    " - " + FIRST_ALBUM_BIRTHDAY.getAlbum().getAlbumName() +
                    " (" +
                    LocalDate.of(
                            FIRST_ALBUM_BIRTHDAY.getAlbumYear(),
                            FIRST_ALBUM_BIRTHDAY.getAlbumMonth(),
                            FIRST_ALBUM_BIRTHDAY.getAlbumDay())
                            .format(DATE_FORMATTER) +
                    ")";

    private TextMailContentBuilder textMailContentBuilder;

    @BeforeEach
    void setUp() {
        textMailContentBuilder = new TextMailContentBuilder();
    }

    @Test
    void shouldBuildContentForBirthdayAlbums() {
        var expected = String.format(
                MessageConstants.CONTENT,
                MonthDay.of(12, 22)
                        .format(MONTH_DAY_FORMATTER),
                BIRTHDAY_ALBUMS_STRING);
        var actual = textMailContentBuilder.build(BIRTHDAY_ALBUMS);
        assertEquals(expected, actual);
    }

    @Test
    void shouldBuildContentForEmptyBirthdayAlbums() {
        var expected = String.format(
                MessageConstants.NO_RESULTS_CONTENT,
                MonthDay.of(1, 22).
                        format(MONTH_DAY_FORMATTER));
        var actual = textMailContentBuilder.build(NO_RESULTS_BIRTHDAY_ALBUMS);
        assertEquals(expected, actual);
    }
}