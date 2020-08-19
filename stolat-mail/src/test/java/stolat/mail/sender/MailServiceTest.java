package stolat.mail.sender;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mockMailSender;

    @Mock
    private MimeMessagePreparator mockFirstMessagePreparator;
    @Mock
    private MimeMessagePreparator mockSecondMessagePreparator;

    private MailService mailService;

    @BeforeEach
    void setUp() {
        mailService = new MailService(mockMailSender);
    }

    @Test
    void shouldSendMail() {
        mailService.prepareAndSendMails(List.of(mockFirstMessagePreparator, mockSecondMessagePreparator));
        verify(mockMailSender).send(mockFirstMessagePreparator, mockSecondMessagePreparator);
    }

    @Test
    void shouldThrowMailParseException() {
        doThrow(new MailParseException("something happened"))
                .when(mockMailSender).send(mockFirstMessagePreparator, mockSecondMessagePreparator);
        assertThrows(MailParseException.class, () -> {
            mailService.prepareAndSendMails(List.of(mockFirstMessagePreparator, mockSecondMessagePreparator));
        });
    }

    @Test
    void shouldThrowMailAuthenticationException() {
        doThrow(new MailAuthenticationException("something happened"))
                .when(mockMailSender).send(mockFirstMessagePreparator, mockSecondMessagePreparator);
        assertThrows(MailAuthenticationException.class, () -> {
            mailService.prepareAndSendMails(List.of(mockFirstMessagePreparator, mockSecondMessagePreparator));
        });
    }

    @Test
    void shouldThrowMailSendException() {
        doThrow(new MailSendException("something happened"))
                .when(mockMailSender).send(mockFirstMessagePreparator, mockSecondMessagePreparator);
        assertThrows(MailSendException.class, () -> {
            mailService.prepareAndSendMails(List.of(mockFirstMessagePreparator, mockSecondMessagePreparator));
        });
    }
}