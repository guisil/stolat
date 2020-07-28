package stolat.mail;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import stolat.mail.content.BirthdayAlbumsToMessageConverter;
import stolat.mail.sender.MailService;
import stolat.service.client.ServiceClient;

@SpringBootApplication(scanBasePackages = "stolat")
@AllArgsConstructor
@Slf4j
public class StolatMailApplication {

    public static void main(String[] args) {
        SpringApplication.run(StolatMailApplication.class, args);
    }


    @Bean
    public CommandLineRunner run(
            ServiceClient serviceClient,
            BirthdayAlbumsToMessageConverter converter,
            MailService mailService) {
        return args -> {
            final var birthdayAlbums = serviceClient.getBirthdayAlbums();
            final var messages = converter.convert(birthdayAlbums);
            mailService.sendMail(messages);
        };
    }
}
