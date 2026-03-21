package app.stolat.notification.internal;

import jakarta.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailSender {

    private static final Logger log = LoggerFactory.getLogger(EmailSender.class);

    private final JavaMailSender mailSender;
    private final String recipientEmail;
    private final String fromEmail;

    public EmailSender(JavaMailSender mailSender,
                       @Value("${stolat.notification.recipient}") String recipientEmail,
                       @Value("${stolat.notification.from:stolat@noreply.com}") String fromEmail) {
        this.mailSender = mailSender;
        this.recipientEmail = recipientEmail;
        this.fromEmail = fromEmail;
    }

    public void send(String subject, String body) {
        try {
            var mimeMessage = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(mimeMessage);
            log.info("Sent notification email to {}", recipientEmail);
        } catch (MessagingException e) {
            log.error("Failed to send notification email: {}", e.getMessage());
        }
    }
}
