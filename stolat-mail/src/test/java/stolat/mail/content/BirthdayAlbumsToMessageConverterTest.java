package stolat.mail.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import stolat.mail.sender.MailProperties;
import stolat.model.Album;
import stolat.model.AlbumBirthday;
import stolat.model.BirthdayAlbums;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BirthdayAlbumsToMessageConverterTest {

    private static final String FROM = "stolat@something.com";
    private static final String FIRST_TO = "someone@something.com";
    private static final String SECOND_TO = "someone.else@something.com";

    private static final AlbumBirthday FIRST_ALBUM_BIRTHDAY =
            new AlbumBirthday(
                    new Album(
                            UUID.randomUUID(), "Some Album",
                            UUID.randomUUID(), "Some Artist"),
                    2000, 12, 22);
    private static final AlbumBirthday SECOND_ALBUM_BIRTHDAY =
            new AlbumBirthday(
                    new Album(
                            UUID.randomUUID(), "Some Other Album",
                            UUID.randomUUID(), "Another Artist"),
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
            SECOND_ALBUM_BIRTHDAY.getAlbum().getArtistName() +
                    " - " + SECOND_ALBUM_BIRTHDAY.getAlbum().getAlbumName() +
                    " (" +
                    LocalDate.of(
                            SECOND_ALBUM_BIRTHDAY.getAlbumYear(),
                            SECOND_ALBUM_BIRTHDAY.getAlbumMonth(),
                            SECOND_ALBUM_BIRTHDAY.getAlbumDay())
                            .format(DATE_FORMATTER) +
                    ")" +
                    "\n" + FIRST_ALBUM_BIRTHDAY.getAlbum().getArtistName() +
                    " - " + FIRST_ALBUM_BIRTHDAY.getAlbum().getAlbumName() +
                    " (" +
                    LocalDate.of(
                            FIRST_ALBUM_BIRTHDAY.getAlbumYear(),
                            FIRST_ALBUM_BIRTHDAY.getAlbumMonth(),
                            FIRST_ALBUM_BIRTHDAY.getAlbumDay())
                            .format(DATE_FORMATTER) +
                    ")";

    @Mock
    private MailProperties mockMailProperties;

    private BirthdayAlbumsToMessageConverter converter;

    @BeforeEach
    void setUp() {
        when(mockMailProperties.getFrom()).thenReturn(FROM);
        when(mockMailProperties.getTo()).thenReturn(List.of(FIRST_TO, SECOND_TO));
        converter = new BirthdayAlbumsToMessageConverter(mockMailProperties);
    }

    @Test
    void shouldConvertBirthdayAlbumsToMailContent() {
        final SimpleMailMessage firstMessage = new SimpleMailMessage();
        firstMessage.setFrom(FROM);
        firstMessage.setTo(FIRST_TO);
        firstMessage.setSubject(
                String.format(
                        MessageConstants.SUBJECT,
                        MonthDay.of(12, 22)
                                .format(MONTH_DAY_FORMATTER)));
        firstMessage.setText(
                String.format(
                        MessageConstants.CONTENT,
                        MonthDay.of(12, 22)
                                .format(MONTH_DAY_FORMATTER),
                        BIRTHDAY_ALBUMS_STRING));
        final SimpleMailMessage secondMessage = new SimpleMailMessage();
        secondMessage.setFrom(FROM);
        secondMessage.setTo(SECOND_TO);
        secondMessage.setSubject(
                String.format(
                        MessageConstants.SUBJECT,
                        MonthDay.of(12, 22)
                                .format(MONTH_DAY_FORMATTER)));
        secondMessage.setText(
                String.format(
                        MessageConstants.CONTENT,
                        MonthDay.of(12, 22)
                                .format(MONTH_DAY_FORMATTER),
                        BIRTHDAY_ALBUMS_STRING));
        final var expected = List.of(firstMessage, secondMessage);
        final var actual = converter.convert(BIRTHDAY_ALBUMS);
        assertEquals(expected, actual);
    }

    @Test
    void shouldConvertEmptyBirthdayAlbumsToMailContent() {
        final SimpleMailMessage firstMessage = new SimpleMailMessage();
        firstMessage.setFrom(FROM);
        firstMessage.setTo(FIRST_TO);
        firstMessage.setSubject(
                String.format(
                        MessageConstants.SUBJECT,
                        MonthDay.of(1, 22)
                                .format(MONTH_DAY_FORMATTER)));
        firstMessage.setText(
                String.format(
                        MessageConstants.NO_RESULTS_CONTENT,
                        MonthDay.of(1, 22).
                                format(MONTH_DAY_FORMATTER)));
        final SimpleMailMessage secondMessage = new SimpleMailMessage();
        secondMessage.setFrom(FROM);
        secondMessage.setTo(SECOND_TO);
        secondMessage.setSubject(
                String.format(
                        MessageConstants.SUBJECT,
                        MonthDay.of(1, 22)
                                .format(MONTH_DAY_FORMATTER)));
        secondMessage.setText(
                String.format(
                        MessageConstants.NO_RESULTS_CONTENT,
                        MonthDay.of(1, 22).
                                format(MONTH_DAY_FORMATTER)));
        final var expected = List.of(firstMessage, secondMessage);
        final var actual = converter.convert(NO_RESULTS_BIRTHDAY_ALBUMS);
        assertEquals(expected, actual);
    }
}