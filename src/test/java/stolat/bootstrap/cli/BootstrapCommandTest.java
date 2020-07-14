package stolat.bootstrap.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BootstrapCommandTest {

    @Mock
    private AlbumBirthdayCommand mockAlbumBirthdayCommand;

    @Mock
    private AlbumCollectionCommand mockAlbumCollectionCommand;

    @InjectMocks
    private BootstrapCommand command;

    @Test
    void shouldUpdateAlbumBirthdayDatabaseWhenBirthdayOptionSelected() {
        command.albumBirthday = true;
        command.call();
        verify(mockAlbumBirthdayCommand).updateAlbumBirthdayDatabase();
        verifyNoMoreInteractions(mockAlbumBirthdayCommand);
        verifyNoInteractions(mockAlbumCollectionCommand);
    }

    @Test
    void shouldIgnorePathOptionWhenBirthdayOptionSelected() {
        command.albumBirthday = true;
        command.path = Path.of(File.separator, "some", "path");
        command.call();
        verify(mockAlbumBirthdayCommand).updateAlbumBirthdayDatabase();
        verifyNoMoreInteractions(mockAlbumBirthdayCommand);
        verifyNoInteractions(mockAlbumCollectionCommand);
    }

    @Test
    void shouldIgnoreTruncateOptionWhenBirthdayOptionSelected() {
        command.albumBirthday = true;
        command.truncate = true;
        command.call();
        verify(mockAlbumBirthdayCommand).updateAlbumBirthdayDatabase();
        verifyNoMoreInteractions(mockAlbumBirthdayCommand);
        verifyNoInteractions(mockAlbumCollectionCommand);
    }

    @Test
    void shouldIgnoreForceOptionWhenBirthdayOptionSelected() {
        command.albumBirthday = true;
        command.force = true;
        command.call();
        verify(mockAlbumBirthdayCommand).updateAlbumBirthdayDatabase();
        verifyNoMoreInteractions(mockAlbumBirthdayCommand);
        verifyNoInteractions(mockAlbumCollectionCommand);
    }

    @Test
    void shouldUpdateAlbumCollectionDatabaseWhenCollectionOptionSelected() {
        command.albumCollection = true;
        command.call();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(false, false);
        verifyNoMoreInteractions(mockAlbumCollectionCommand);
        verifyNoInteractions(mockAlbumBirthdayCommand);
    }

    @Test
    void shouldUpdateAlbumCollectionDatabaseFromFolderWhenCollectionAndPathOptionsSelected() {
        final Path path = Path.of(File.separator, "some", "other", "path");
        command.albumCollection = true;
        command.path = path;
        command.call();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(false, path, false);
        verifyNoMoreInteractions(mockAlbumCollectionCommand);
        verifyNoInteractions(mockAlbumBirthdayCommand);
    }

    @Test
    void shouldTruncateAndUpdateAlbumCollectionDatabaseWhenCollectionAndTruncateOptionsSelected() {
        command.albumCollection = true;
        command.truncate = true;
        command.call();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(true, false);
        verifyNoMoreInteractions(mockAlbumCollectionCommand);
        verifyNoInteractions(mockAlbumBirthdayCommand);
    }

    @Test
    void shouldForceUpdateAlbumCollectionDatabaseWhenCollectionAndForceOptionsSelected() {
        command.albumCollection = true;
        command.force = true;
        command.call();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(false, true);
        verifyNoMoreInteractions(mockAlbumCollectionCommand);
        verifyNoInteractions(mockAlbumBirthdayCommand);
    }

    @Test
    void shouldTruncateAndUpdateAlbumCollectionDatabaseFromFolderWhenCollectionAndTruncateAndPathOptionsSelected() {
        final Path path = Path.of(File.separator, "some", "other", "path");
        command.albumCollection = true;
        command.truncate = true;
        command.path = path;
        command.call();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(true, path, false);
        verifyNoMoreInteractions(mockAlbumCollectionCommand);
        verifyNoInteractions(mockAlbumBirthdayCommand);
    }

    @Test
    void shouldForceUpdateAlbumCollectionDatabaseFromFolderWhenCollectionAndForceAndPathOptionsSelected() {
        final Path path = Path.of(File.separator, "some", "other", "path");
        command.albumCollection = true;
        command.force = true;
        command.path = path;
        command.call();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(false, path, true);
        verifyNoMoreInteractions(mockAlbumCollectionCommand);
        verifyNoInteractions(mockAlbumBirthdayCommand);
    }

    @Test
    void shouldUpdateAlbumBirthdayAndAlbumCollectionWhenBothOptionsSelected() {
        command.albumBirthday = true;
        command.albumCollection = true;
        command.call();
        verify(mockAlbumBirthdayCommand).updateAlbumBirthdayDatabase();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(false, false);
        verifyNoMoreInteractions(mockAlbumBirthdayCommand, mockAlbumCollectionCommand);
    }

    @Test
    void shouldUpdateAlbumBirthdayAndTruncateAndUpdateAlbumCollectionWhenBothOptionsAndTruncateOptionSelected() {
        command.albumBirthday = true;
        command.albumCollection = true;
        command.truncate = true;
        command.call();
        verify(mockAlbumBirthdayCommand).updateAlbumBirthdayDatabase();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(true, false);
        verifyNoMoreInteractions(mockAlbumBirthdayCommand, mockAlbumCollectionCommand);
    }

    @Test
    void shouldUpdateAlbumBirthdayAndForceUpdateAlbumCollectionWhenBothOptionsAndForceOptionSelected() {
        command.albumBirthday = true;
        command.albumCollection = true;
        command.force = true;
        command.call();
        verify(mockAlbumBirthdayCommand).updateAlbumBirthdayDatabase();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(false, true);
        verifyNoMoreInteractions(mockAlbumBirthdayCommand, mockAlbumCollectionCommand);
    }

    @Test
    void shouldUpdateAlbumBirthdayAndTruncateAndForceUpdateAlbumCollectionWhenBothOptionsAndTruncateAndForceOptionSelected() {
        command.albumBirthday = true;
        command.albumCollection = true;
        command.truncate = true;
        command.force = true;
        command.call();
        verify(mockAlbumBirthdayCommand).updateAlbumBirthdayDatabase();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(true, true);
        verifyNoMoreInteractions(mockAlbumBirthdayCommand, mockAlbumCollectionCommand);
    }

    @Test
    void shouldUpdateAlbumBirthdayAndAlbumCollectionFromFolderWhenBothOptionsAndPathOptionSelected() {
        final Path path = Path.of(File.separator, "some", "other", "path");
        command.albumBirthday = true;
        command.albumCollection = true;
        command.path = path;
        command.call();
        verify(mockAlbumBirthdayCommand).updateAlbumBirthdayDatabase();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(false, path, false);
        verifyNoMoreInteractions(mockAlbumBirthdayCommand, mockAlbumCollectionCommand);
    }

    @Test
    void shouldUpdateAlbumBirthdayAndTruncateAndUpdateAlbumCollectionFromFolderWhenBothOptionsAndPathAndTruncateOptionSelected() {
        final Path path = Path.of(File.separator, "some", "other", "path");
        command.albumBirthday = true;
        command.albumCollection = true;
        command.truncate = true;
        command.path = path;
        command.call();
        verify(mockAlbumBirthdayCommand).updateAlbumBirthdayDatabase();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(true, path, false);
        verifyNoMoreInteractions(mockAlbumBirthdayCommand, mockAlbumCollectionCommand);
    }

    @Test
    void shouldUpdateAlbumBirthdayAndTruncateAndForceUpdateAlbumCollectionFromFolderWhenBothOptionsAndPathAndTruncateAndForceOptionSelected() {
        final Path path = Path.of(File.separator, "some", "other", "path");
        command.albumBirthday = true;
        command.albumCollection = true;
        command.truncate = true;
        command.force = true;
        command.path = path;
        command.call();
        verify(mockAlbumBirthdayCommand).updateAlbumBirthdayDatabase();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(true, path, true);
        verifyNoMoreInteractions(mockAlbumBirthdayCommand, mockAlbumCollectionCommand);
    }

    @Test
    void shouldUpdateAlbumBirthdayAndAlbumCollectionWhenNoOptionsSelected() {
        command.call();
        verify(mockAlbumBirthdayCommand).updateAlbumBirthdayDatabase();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(false, false);
        verifyNoMoreInteractions(mockAlbumBirthdayCommand, mockAlbumCollectionCommand);
    }

    @Test
    void shouldUpdateAlbumBirthdayAndTruncateAndUpdateAlbumCollectionWhenOnlyTruncateOptionSelected() {
        command.truncate = true;
        command.call();
        verify(mockAlbumBirthdayCommand).updateAlbumBirthdayDatabase();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(true, false);
        verifyNoMoreInteractions(mockAlbumBirthdayCommand, mockAlbumCollectionCommand);
    }

    @Test
    void shouldUpdateAlbumBirthdayAndForceUpdateAlbumCollectionWhenOnlyForceOptionSelected() {
        command.force = true;
        command.call();
        verify(mockAlbumBirthdayCommand).updateAlbumBirthdayDatabase();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(false, true);
        verifyNoMoreInteractions(mockAlbumBirthdayCommand, mockAlbumCollectionCommand);
    }

    @Test
    void shouldUpdateAlbumBirthdayAndTruncateAndForceUpdateAlbumCollectionWhenOnlyTruncateAndForceOptionsSelected() {
        command.truncate = true;
        command.force = true;
        command.call();
        verify(mockAlbumBirthdayCommand).updateAlbumBirthdayDatabase();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(true, true);
        verifyNoMoreInteractions(mockAlbumBirthdayCommand, mockAlbumCollectionCommand);
    }

    @Test
    void shouldUpdateAlbumBirthdayAndAlbumCollectionFromFolderWhenOnlyPathOptionSelected() {
        final Path path = Path.of(File.separator, "some", "other", "path");
        command.path = path;
        command.call();
        verify(mockAlbumBirthdayCommand).updateAlbumBirthdayDatabase();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(false, path, false);
        verifyNoMoreInteractions(mockAlbumBirthdayCommand, mockAlbumCollectionCommand);
    }

    @Test
    void shouldUpdateAlbumBirthdayAndTruncateAndUpdateAlbumCollectionFromFolderWhenOnlyPathAndTruncateOptionSelected() {
        final Path path = Path.of(File.separator, "some", "other", "path");
        command.path = path;
        command.truncate = true;
        command.call();
        verify(mockAlbumBirthdayCommand).updateAlbumBirthdayDatabase();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(true, path, false);
        verifyNoMoreInteractions(mockAlbumBirthdayCommand, mockAlbumCollectionCommand);
    }

    @Test
    void shouldUpdateAlbumBirthdayAndForceUpdateAlbumCollectionFromFolderWhenOnlyPathAndForceOptionSelected() {
        final Path path = Path.of(File.separator, "some", "other", "path");
        command.path = path;
        command.force = true;
        command.call();
        verify(mockAlbumBirthdayCommand).updateAlbumBirthdayDatabase();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(false, path, true);
        verifyNoMoreInteractions(mockAlbumBirthdayCommand, mockAlbumCollectionCommand);
    }

    @Test
    void shouldUpdateAlbumBirthdayAndTruncateAndForceUpdateAlbumCollectionFromFolderWhenOnlyPathAndTruncateAndForceOptionSelected() {
        final Path path = Path.of(File.separator, "some", "other", "path");
        command.path = path;
        command.truncate = true;
        command.force = true;
        command.call();
        verify(mockAlbumBirthdayCommand).updateAlbumBirthdayDatabase();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(true, path, true);
        verifyNoMoreInteractions(mockAlbumBirthdayCommand, mockAlbumCollectionCommand);
    }
}