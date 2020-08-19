package stolat.mail;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.test.context.TestPropertySource;
import stolat.mail.content.MessagePreparatorProvider;
import stolat.mail.sender.MailService;
import stolat.model.BirthdayAlbums;
import stolat.service.client.ServiceClient;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource("classpath:test-application.properties")
class StolatMailApplicationTest {

    @Mock
    private BirthdayAlbums mockBirthdayAlbums;
    @Mock
    private MimeMessagePreparator mockMessagePreparator;

    @MockBean
    private ServiceClient mockServiceClient;
    @MockBean
    private MessagePreparatorProvider mockMessagePreparatorProvider;
    @MockBean
    private MailService mockMailService;

    @Autowired
    private StolatMailApplication application;

    @Test
    void shouldLoadApplicationContext() {

    }

    @Test
    void shouldRunApplication() throws Exception {
        when(mockServiceClient.getBirthdayAlbums()).thenReturn(mockBirthdayAlbums);
        when(mockMessagePreparatorProvider.getPreparators(mockBirthdayAlbums)).thenReturn(List.of(mockMessagePreparator));

        application.run(mockServiceClient, mockMessagePreparatorProvider, mockMailService)
                .run();

        verify(mockServiceClient).getBirthdayAlbums();
        verify(mockMessagePreparatorProvider).getPreparators(mockBirthdayAlbums);
        verify(mockMailService).prepareAndSendMails(List.of(mockMessagePreparator));
    }
}