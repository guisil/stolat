package stolat.bootstrap.filesystem;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties("filesystem")
@Getter
@Setter
public class FileSystemProperties {

    private String albumCollectionPath;
    private int albumCollectionBatchSize = 100;
    private List<String> musicFileExtensions;
}
