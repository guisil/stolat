package stolat.mail.sender;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    private static final String FROM = "stolat@something.com";
    private static final String FIRST_TO = "someone@something.com";
    private static final String SECOND_TO = "someone.else@something.com";
    private static final String SUBJECT = "Subject of the message";
    private static final String CONTENT = "This is the actual message.\nEnjoy!";

    private SimpleMailMessage firstMessage;
    private SimpleMailMessage secondMessage;

    @Mock
    private JavaMailSender mockMailSender;

    private MailService mailService;

    @BeforeEach
    void setUp() {
        firstMessage = new SimpleMailMessage();
        firstMessage.setFrom(FROM);
        firstMessage.setTo(FIRST_TO);
        firstMessage.setSubject(SUBJECT);
        firstMessage.setText(CONTENT);
        secondMessage = new SimpleMailMessage();
        secondMessage.setFrom(FROM);
        secondMessage.setTo(SECOND_TO);
        secondMessage.setSubject(SUBJECT);
        secondMessage.setText(CONTENT);
        mailService = new MailService(mockMailSender);
    }

    @Test
    void shouldSendMail() {
        mailService.sendMail(List.of(firstMessage, secondMessage));
        verify(mockMailSender).send(firstMessage, secondMessage);
    }

    @Test
    void shouldThrowMailParseException() {
        doThrow(new MailParseException("something happened"))
                .when(mockMailSender).send(firstMessage, secondMessage);
        assertThrows(MailParseException.class, () -> {
            mailService.sendMail(List.of(firstMessage, secondMessage));
        });
    }

    @Test
    void shouldThrowMailAuthenticationException() {
        doThrow(new MailAuthenticationException("something happened"))
                .when(mockMailSender).send(firstMessage, secondMessage);
        assertThrows(MailAuthenticationException.class, () -> {
            mailService.sendMail(List.of(firstMessage, secondMessage));
        });
    }

    @Test
    void shouldThrowMailSendException() {
        doThrow(new MailSendException("something happened"))
                .when(mockMailSender).send(firstMessage, secondMessage);
        assertThrows(MailSendException.class, () -> {
            mailService.sendMail(List.of(firstMessage, secondMessage));
        });
    }
}