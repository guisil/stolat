package app.stolat.notification;

import java.util.Comparator;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import app.stolat.birthday.BirthdayService;
import app.stolat.notification.internal.EmailSender;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Validated
public class NotificationService {

    private final BirthdayService birthdayService;
    private final EmailSender emailSender;
    private final TemplateEngine templateEngine;

    public NotificationService(BirthdayService birthdayService,
                               EmailSender emailSender,
                               TemplateEngine templateEngine) {
        this.birthdayService = birthdayService;
        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
    }

    public void sendDailyDigest(LocalDate date) {
        var birthdays = birthdayService.findBirthdaysOn(date);
        if (birthdays.isEmpty()) {
            return;
        }

        var items = birthdays.stream()
                .map(b -> new BirthdayItem(
                        b.getArtistName(),
                        b.getAlbumTitle(),
                        b.getReleaseDate(),
                        ChronoUnit.YEARS.between(b.getReleaseDate(), date),
                        b.getPlayCount()))
                .sorted(Comparator.comparing(BirthdayItem::releaseDate))
                .toList();

        var context = new Context();
        context.setVariable("date", date);
        context.setVariable("birthdays", items);

        var subject = "Album Birthdays — " + date;
        var body = templateEngine.process("birthday-digest", context);

        emailSender.send(subject, body);
    }

    public record BirthdayItem(String artistName, String albumTitle, LocalDate releaseDate,
                               long years, Integer playCount) {
    }
}
