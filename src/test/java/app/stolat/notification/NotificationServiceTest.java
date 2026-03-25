package app.stolat.notification;

import app.stolat.birthday.AlbumBirthday;
import app.stolat.birthday.BirthdayService;
import app.stolat.notification.internal.EmailSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private BirthdayService birthdayService;

    @Mock
    private EmailSender emailSender;

    private NotificationService notificationService() {
        var resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        var engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return new NotificationService(birthdayService, emailSender, engine);
    }

    @Test
    void shouldSendHtmlEmailWhenBirthdaysExist() {
        var today = LocalDate.of(2026, 6, 16);
        var birthday = new AlbumBirthday("OK Computer", "Radiohead",
                UUID.randomUUID(), LocalDate.of(1997, 6, 16));
        given(birthdayService.findBirthdaysOn(today)).willReturn(List.of(birthday));

        notificationService().sendDailyDigest(today);

        then(emailSender).should().send(contains("Album Birthdays"), argThat(body ->
                body.contains("OK Computer") && body.contains("Radiohead")));
    }

    @Test
    void shouldSortBirthdaysByYearOldestFirst() {
        var today = LocalDate.of(2026, 6, 16);
        var newer = new AlbumBirthday("In Rainbows", "Radiohead",
                UUID.randomUUID(), LocalDate.of(2007, 6, 16));
        var older = new AlbumBirthday("OK Computer", "Radiohead",
                UUID.randomUUID(), LocalDate.of(1997, 6, 16));
        given(birthdayService.findBirthdaysOn(today)).willReturn(List.of(newer, older));

        notificationService().sendDailyDigest(today);

        then(emailSender).should().send(contains("Album Birthdays"), argThat(body -> {
            int okComputerPos = body.indexOf("OK Computer");
            int inRainbowsPos = body.indexOf("In Rainbows");
            return okComputerPos >= 0 && inRainbowsPos >= 0 && okComputerPos < inRainbowsPos;
        }));
    }

    @Test
    void shouldIncludePlayCountWhenAvailable() {
        var today = LocalDate.of(2026, 6, 16);
        var birthday = new AlbumBirthday("OK Computer", "Radiohead",
                UUID.randomUUID(), LocalDate.of(1997, 6, 16));
        birthday.updatePlayCount(142);
        given(birthdayService.findBirthdaysOn(today)).willReturn(List.of(birthday));

        notificationService().sendDailyDigest(today);

        then(emailSender).should().send(contains("Album Birthdays"), argThat(body ->
                body.contains("142 plays")));
    }

    @Test
    void shouldNotSendEmailWhenNoBirthdays() {
        var today = LocalDate.of(2026, 1, 1);
        given(birthdayService.findBirthdaysOn(today)).willReturn(List.of());

        notificationService().sendDailyDigest(today);

        then(emailSender).shouldHaveNoInteractions();
    }
}
