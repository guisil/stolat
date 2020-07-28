package stolat.mail.content;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;
import stolat.mail.sender.MailProperties;
import stolat.model.AlbumBirthday;
import stolat.model.AlbumMonthDayArtistComparator;
import stolat.model.BirthdayAlbums;

import java.time.MonthDay;
import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@Slf4j
public class BirthdayAlbumsToMessageConverter {

    private MailProperties mailProperties;

    public List<SimpleMailMessage> convert(BirthdayAlbums birthdayAlbums) {
        log.info("Converting birthday albums to message(s)");

        return mailProperties.getTo().stream().map(to -> {
            String formattedMonthDay =
                    birthdayAlbums.getFrom().format(MessageConstants.MONTH_DAY_FORMATTER);
            String subject = String.format(MessageConstants.SUBJECT, formattedMonthDay);
            String content;
            if (birthdayAlbums.getAlbumBirthdays().isEmpty()) {
                content = String.format(MessageConstants.NO_RESULTS_CONTENT,
                        formattedMonthDay);
            } else {
                content = String.format(MessageConstants.CONTENT,
                        formattedMonthDay,
                        getFormattedAlbumBirthdays(birthdayAlbums.getAlbumBirthdays()));
            }

            var message = new SimpleMailMessage();
            message.setFrom(mailProperties.getFrom());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            return message;
        }).collect(Collectors.toList());
    }

    private String getFormattedAlbumBirthdays(List<AlbumBirthday> albumBirthdays) {
        return albumBirthdays.stream().sorted(new AlbumMonthDayArtistComparator())
                .map(albumBirthday -> {
            return new StringBuilder()
                    .append(albumBirthday.getAlbum().getArtistName())
                    .append(" - ")
                    .append(albumBirthday.getAlbum().getAlbumName())
                    .append(" (")
                    .append(albumBirthday.getAlbumCompleteDate().format(MessageConstants.DATE_FORMATTER))
                    .append(")")
                    .toString();
        }).collect(Collectors.joining("\n"));
    }
}
