package stolat.mail.sender;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties("mail")
@Getter
@Setter
public class MailProperties {

    private String sender;
    private List<String> recipients;
}
