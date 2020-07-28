package stolat.mail.sender;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    public void sendMail(List<SimpleMailMessage> messages) {
        log.info("Sending {} mail messages", messages.size());
        mailSender.send(messages.toArray(SimpleMailMessage[]::new));
    }
}
