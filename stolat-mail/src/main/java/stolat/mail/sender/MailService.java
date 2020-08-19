package stolat.mail.sender;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    public void prepareAndSendMails(List<MimeMessagePreparator> messagePreparators) {
        log.info("Sending {} mail messages", messagePreparators.size());
        mailSender.send(messagePreparators.toArray(MimeMessagePreparator[]::new));
    }
}
