package app.stolat.notification;

import app.stolat.birthday.AlbumBirthday;
import app.stolat.birthday.BirthdayService;
import app.stolat.notification.internal.EmailSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private BirthdayService birthdayService;

    @Mock
    private EmailSender emailSender;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void shouldSendEmailWhenBirthdaysExist() {
        var today = LocalDate.of(2026, 6, 16);
        var birthday = new AlbumBirthday("OK Computer", "Radiohead",
                UUID.randomUUID(), LocalDate.of(1997, 6, 16));
        given(birthdayService.findBirthdaysOn(today)).willReturn(List.of(birthday));

        notificationService.sendDailyDigest(today);

        then(emailSender).should().send(
                contains("Album Birthdays"),
                contains("OK Computer"));
    }

    @Test
    void shouldNotSendEmailWhenNoBirthdays() {
        var today = LocalDate.of(2026, 1, 1);
        given(birthdayService.findBirthdaysOn(today)).willReturn(List.of());

        notificationService.sendDailyDigest(today);

        then(emailSender).shouldHaveNoInteractions();
    }
}
