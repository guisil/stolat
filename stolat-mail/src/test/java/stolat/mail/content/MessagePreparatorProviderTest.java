package stolat.mail.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.MimeMessagePreparator;
import stolat.mail.sender.MailProperties;
import stolat.model.Album;
import stolat.model.AlbumBirthday;
import stolat.model.Artist;
import stolat.model.BirthdayAlbums;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessagePreparatorProviderTest {

    private static final String SENDER = "stolat@something.com";
    private static final String FIRST_RECIPIENT = "someone@something.com";
    private static final String SECOND_RECIPIENT = "someone.else@something.com";

    private static final Artist SOME_ARTIST = new Artist(UUID.randomUUID(), "Some Artist");
	private static final Artist ANOTHER_ARTIST = new Artist(UUID.randomUUID(), "Another Artist");
	
    private static final AlbumBirthday FIRST_ALBUM_BIRTHDAY =
            new AlbumBirthday(
                    new Album(
                            UUID.randomUUID(), "Some Album",
                            List.of(SOME_ARTIST)),
                    2000, 12, 22);
    private static final AlbumBirthday SECOND_ALBUM_BIRTHDAY =
            new AlbumBirthday(
                    new Album(
                            UUID.randomUUID(), "Some Other Album",
                            List.of(ANOTHER_ARTIST, SOME_ARTIST)),
                    2000, 12, 22);
    private static final BirthdayAlbums BIRTHDAY_ALBUMS =
            new BirthdayAlbums(
                    MonthDay.of(12, 22),
                    MonthDay.of(12, 22),
                    List.of(FIRST_ALBUM_BIRTHDAY, SECOND_ALBUM_BIRTHDAY));

    private static final DateTimeFormatter MONTH_DAY_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String BIRTHDAY_ALBUMS_STRING =
            SECOND_ALBUM_BIRTHDAY.getAlbum().getArtists().stream().map(Artist::getArtistName).collect(Collectors.joining(",")) +
                    " - " + SECOND_ALBUM_BIRTHDAY.getAlbum().getAlbumName() +
                    " (" +
                    LocalDate.of(
                            SECOND_ALBUM_BIRTHDAY.getAlbumYear(),
                            SECOND_ALBUM_BIRTHDAY.getAlbumMonth(),
                            SECOND_ALBUM_BIRTHDAY.getAlbumDay())
                            .format(DATE_FORMATTER) +
                    ")" +
                    "\n" + FIRST_ALBUM_BIRTHDAY.getAlbum().getArtists().stream().map(Artist::getArtistName).collect(Collectors.joining(",")) +
                    " - " + FIRST_ALBUM_BIRTHDAY.getAlbum().getAlbumName() +
                    " (" +
                    LocalDate.of(
                            FIRST_ALBUM_BIRTHDAY.getAlbumYear(),
                            FIRST_ALBUM_BIRTHDAY.getAlbumMonth(),
                            FIRST_ALBUM_BIRTHDAY.getAlbumDay())
                            .format(DATE_FORMATTER) +
                    ")";

    @Mock
    private MimeMessage mockMessage;

    @Mock
    private MailProperties mockMailProperties;
    @Mock
    private MailSubjectBuilder mockMailSubjectBuilder;
    @Mock
    private MailContentBuilder mockMailContentBuilder;

    private MessagePreparatorProvider provider;

    @BeforeEach
    void setUp() {
        when(mockMailProperties.getSender()).thenReturn(SENDER);
        when(mockMailProperties.getRecipients()).thenReturn(List.of(FIRST_RECIPIENT, SECOND_RECIPIENT));
        provider = new MessagePreparatorProvider(
                mockMailProperties, mockMailSubjectBuilder, mockMailContentBuilder);
    }

    @Test
    void shouldConvertBirthdayAlbumsToMailContent() throws Exception {
        final var expectedSubject =
                String.format(
                        MessageConstants.SUBJECT,
                        MonthDay.of(12, 22)
                                .format(MONTH_DAY_FORMATTER));
        final var expectedText =
                String.format(
                        MessageConstants.CONTENT,
                        MonthDay.of(12, 22)
                                .format(MONTH_DAY_FORMATTER),
                        BIRTHDAY_ALBUMS_STRING);

        when(mockMailSubjectBuilder.build(BIRTHDAY_ALBUMS)).thenReturn(expectedSubject);
        when(mockMailContentBuilder.build(BIRTHDAY_ALBUMS)).thenReturn(expectedText);

        final List<MimeMessagePreparator> preparators =
                provider.getPreparators(BIRTHDAY_ALBUMS);
        assertEquals(2, preparators.size());

        for (MimeMessagePreparator preparator : preparators) {
            preparator.prepare(mockMessage);
        }

        verify(mockMessage, times(2)).setFrom(InternetAddress.parse(SENDER)[0]);
        verify(mockMessage).setRecipient(Message.RecipientType.TO, InternetAddress.parse(FIRST_RECIPIENT)[0]);
        verify(mockMessage).setRecipient(Message.RecipientType.TO, InternetAddress.parse(SECOND_RECIPIENT)[0]);
        verify(mockMessage, times(2)).setSubject(expectedSubject);
        verify(mockMessage, times(2)).setText(expectedText);
    }
}