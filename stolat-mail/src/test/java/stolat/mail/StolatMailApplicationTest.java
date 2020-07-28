package stolat.mail;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.TestPropertySource;
import stolat.mail.content.BirthdayAlbumsToMessageConverter;
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
    private SimpleMailMessage mockMessage;

    @MockBean
    private ServiceClient mockServiceClient;
    @MockBean
    private BirthdayAlbumsToMessageConverter mockConverter;
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
        when(mockConverter.convert(mockBirthdayAlbums)).thenReturn(List.of(mockMessage));

        application.run(mockServiceClient, mockConverter, mockMailService)
                .run();

        verify(mockServiceClient).getBirthdayAlbums();
        verify(mockConverter).convert(mockBirthdayAlbums);
        verify(mockMailService).sendMail(List.of(mockMessage));
    }
}