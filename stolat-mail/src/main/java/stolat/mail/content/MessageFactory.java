package stolat.mail.content;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MessageFactory {

    public List<SimpleMailMessage> getMessages(String from, List<String> to, String subject, String content) {
        return to.stream().map(currentTo -> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(currentTo);
            message.setSubject(subject);
            message.setText(content);
            return message;
        }).collect(Collectors.toList());
    }
}
