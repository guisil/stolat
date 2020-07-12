package stolat.bootstrap.sql;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("sql")
@Getter
@Setter
public class SqlProperties {

    private String populateAlbumBirthdayScript;
}
