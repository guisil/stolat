package stolat.mail.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import stolat.model.AlbumBirthday;
import stolat.model.AlbumMonthDayArtistComparator;
import stolat.model.Artist;
import stolat.model.BirthdayAlbums;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Profile("text")
@Component
@Slf4j
public class TextMailContentBuilder implements MailContentBuilder {

    @Override
    public String build(BirthdayAlbums birthdayAlbums) {
        String formattedMonthDay =
                birthdayAlbums.getFrom().format(MessageConstants.MONTH_DAY_FORMATTER);
        if (birthdayAlbums.getAlbumBirthdays().isEmpty()) {
            return String.format(MessageConstants.NO_RESULTS_CONTENT,
                    formattedMonthDay);
        } else {
            return String.format(MessageConstants.CONTENT,
                    formattedMonthDay,
                    getFormattedAlbumBirthdays(birthdayAlbums.getAlbumBirthdays()));
        }
    }

    private String getFormattedAlbumBirthdays(List<AlbumBirthday> albumBirthdays) {
        return albumBirthdays.stream().sorted(new AlbumMonthDayArtistComparator())
                .map(albumBirthday -> {
                    return new StringBuilder()
                            .append(albumBirthday.getAlbum().getArtists().stream().map(Artist::getArtistName).collect(Collectors.joining(",")))
                            .append(" - ")
                            .append(albumBirthday.getAlbum().getAlbumName())
                            .append(" (")
                            .append(
                                    LocalDate.of(
                                            albumBirthday.getAlbumYear(),
                                            albumBirthday.getAlbumMonth(),
                                            albumBirthday.getAlbumDay())
                                            .format(MessageConstants.DATE_FORMATTER))
                            .append(")")
                            .toString();
                }).collect(Collectors.joining("\n"));
    }
}
