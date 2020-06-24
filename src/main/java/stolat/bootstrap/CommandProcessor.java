package stolat.bootstrap;

import org.springframework.stereotype.Component;
import stolat.bootstrap.filesystem.FileSystemProperties;
import stolat.bootstrap.sql.SqlProperties;

@Component
public class CommandProcessor {

    private SqlProperties sqlProperties;

    private FileSystemProperties fileSystemProperties;

    public CommandProcessor(SqlProperties sqlProperties, FileSystemProperties fileSystemProperties) {
        this.sqlProperties = sqlProperties;
        this.fileSystemProperties = fileSystemProperties;
    }

    public void bootstrapAlbumBirthday() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public void updateAlbumBirthday() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public void bootstrapAlbumCollection() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public void updateAlbumCollection() {
        throw new UnsupportedOperationException("not implemented yet");
    }
}
