package stolat.mail.content;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;
import stolat.mail.sender.MailProperties;
import stolat.model.BirthdayAlbums;

import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@Slf4j
public class MessagePreparatorProvider {

    private final MailProperties mailProperties;
    private final MailSubjectBuilder mailSubjectBuilder;
    private final MailContentBuilder mailContentBuilder;

    public List<MimeMessagePreparator> getPreparators(BirthdayAlbums birthdayAlbums) {
        log.info("Converting birthday albums to message(s)");

        var subject = mailSubjectBuilder.build(birthdayAlbums);
        var content = mailContentBuilder.build(birthdayAlbums);

        return mailProperties.getRecipients().stream().map(recipient -> {

            MimeMessagePreparator messagePreparator = mimeMessage -> {
                MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
                messageHelper.setFrom(mailProperties.getSender());
                messageHelper.setTo(recipient);
                messageHelper.setSubject(subject);
                messageHelper.setText(content);
            };

            return messagePreparator;

        }).collect(Collectors.toList());
    }
}
