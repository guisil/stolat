package stolat.mail.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import stolat.model.BirthdayAlbums;

@Component
@Slf4j
public class MailSubjectBuilder {

    public String build(BirthdayAlbums birthdayAlbums) {
        String formattedMonthDay =
                birthdayAlbums.getFrom().format(MessageConstants.MONTH_DAY_FORMATTER);
        return String.format(MessageConstants.SUBJECT, formattedMonthDay);
    }
}
