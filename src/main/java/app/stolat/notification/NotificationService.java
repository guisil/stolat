package app.stolat.notification;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

import app.stolat.birthday.AlbumBirthday;
import app.stolat.birthday.BirthdayService;
import app.stolat.notification.internal.EmailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final BirthdayService birthdayService;
    private final EmailSender emailSender;

    public NotificationService(BirthdayService birthdayService, EmailSender emailSender) {
        this.birthdayService = birthdayService;
        this.emailSender = emailSender;
    }

    public void sendDailyDigest(LocalDate date) {
        var birthdays = birthdayService.findBirthdaysOn(date);
        if (birthdays.isEmpty()) {
            return;
        }

        var subject = "Album Birthdays — " + date;
        var body = birthdays.stream()
                .map(b -> formatBirthday(b, date))
                .collect(Collectors.joining("\n"));

        emailSender.send(subject, body);
    }

    private String formatBirthday(AlbumBirthday birthday, LocalDate today) {
        var years = ChronoUnit.YEARS.between(birthday.getReleaseDate(), today);
        return "%s — %s (%d years)".formatted(
                birthday.getArtistName(), birthday.getAlbumTitle(), years);
    }
}
