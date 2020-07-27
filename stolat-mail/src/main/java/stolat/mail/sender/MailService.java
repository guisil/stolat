package stolat.mail.sender;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class MailService {

    private MailProperties mailProperties;
    private JavaMailSender mailSender;

    public void sendMail(String content) {
        throw new UnsupportedOperationException("not implemented yet");
    }
}
