package stolat.bootstrap.sql;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties("sql")
@Getter
@Setter
public class SqlProperties {

    private List<String> albumBirthdayScripts;

    private List<String> albumCollectionScripts;
}
