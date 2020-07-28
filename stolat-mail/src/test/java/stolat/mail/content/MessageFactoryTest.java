package stolat.mail.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MessageFactoryTest {

    private static final String FROM = "stolat@something.com";
    private static final String FIRST_TO = "someone@something.com";
    private static final String SECOND_TO = "someone.else@something.com";
    private static final String SUBJECT = "Subject of the message";
    private static final String CONTENT = "This is the actual message.\nEnjoy!";

    private MessageFactory messageFactory;

    @BeforeEach
    void setUp() {
        messageFactory = new MessageFactory();
    }

    @Test
    void shouldGetSingleMessage() {
        final SimpleMailMessage first =
                new SimpleMailMessage();
        first.setFrom(FROM);
        first.setTo(FIRST_TO);
        first.setSubject(SUBJECT);
        first.setText(CONTENT);
        final List<SimpleMailMessage> expected = List.of(first);
        final List<SimpleMailMessage> actual =
                messageFactory.getMessages(FROM, List.of(FIRST_TO), SUBJECT, CONTENT);
        assertEquals(expected, actual);
    }

    @Test
    void shouldGetMultipleMessages() {
        final SimpleMailMessage first = new SimpleMailMessage();
        first.setFrom(FROM);
        first.setTo(FIRST_TO);
        first.setSubject(SUBJECT);
        first.setText(CONTENT);
        final SimpleMailMessage second = new SimpleMailMessage();
        second.setFrom(FROM);
        second.setTo(SECOND_TO);
        second.setSubject(SUBJECT);
        second.setText(CONTENT);
        final List<SimpleMailMessage> expected = List.of(first, second);
        final List<SimpleMailMessage> actual =
                messageFactory.getMessages(FROM, List.of(FIRST_TO, SECOND_TO), SUBJECT, CONTENT);
        assertEquals(expected, actual);
    }
}