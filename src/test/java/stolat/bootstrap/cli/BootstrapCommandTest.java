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
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(false);
        verifyNoMoreInteractions(mockAlbumCollectionCommand);
        verifyNoInteractions(mockAlbumBirthdayCommand);
    }

    @Test
    void shouldUpdateAlbumCollectionDatabaseFromFolderWhenCollectionAndPathOptionsSelected() {
        final Path path = Path.of(File.separator, "some", "other", "path");
        command.albumCollection = true;
        command.path = path;
        command.call();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(false, path);
        verifyNoMoreInteractions(mockAlbumCollectionCommand);
        verifyNoInteractions(mockAlbumBirthdayCommand);
    }

    @Test
    void shouldTruncateAndUpdateAlbumCollectionDatabaseWhenCollectionAndForceOptionsSelected() {
        command.albumCollection = true;
        command.force = true;
        command.call();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(true);
        verifyNoMoreInteractions(mockAlbumCollectionCommand);
        verifyNoInteractions(mockAlbumBirthdayCommand);
    }

    @Test
    void shouldTruncateAndUpdateAlbumCollectionDatabaseFromFolderWhenCollectionAndForceAndPathOptionsSelected() {
        final Path path = Path.of(File.separator, "some", "other", "path");
        command.albumCollection = true;
        command.force = true;
        command.path = path;
        command.call();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(true, path);
        verifyNoMoreInteractions(mockAlbumCollectionCommand);
        verifyNoInteractions(mockAlbumBirthdayCommand);
    }

    @Test
    void shouldUpdateAlbumBirthdayAndAlbumCollectionWhenBothOptionsSelected() {
        command.albumBirthday = true;
        command.albumCollection = true;
        command.call();
        verify(mockAlbumBirthdayCommand).updateAlbumBirthdayDatabase();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(false);
        verifyNoMoreInteractions(mockAlbumBirthdayCommand, mockAlbumCollectionCommand);
    }

    @Test
    void shouldUpdateAlbumBirthdayAndTruncateAndUpdateAlbumCollectionWhenBothOptionsAndForceOptionSelected() {
        command.albumBirthday = true;
        command.albumCollection = true;
        command.force = true;
        command.call();
        verify(mockAlbumBirthdayCommand).updateAlbumBirthdayDatabase();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(true);
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
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(false, path);
        verifyNoMoreInteractions(mockAlbumBirthdayCommand, mockAlbumCollectionCommand);
    }

    @Test
    void shouldUpdateAlbumBirthdayAndTruncateAndUpdateAlbumCollectionFromFolderWhenBothOptionsAndPathAndForceOptionSelected() {
        final Path path = Path.of(File.separator, "some", "other", "path");
        command.albumBirthday = true;
        command.albumCollection = true;
        command.force = true;
        command.path = path;
        command.call();
        verify(mockAlbumBirthdayCommand).updateAlbumBirthdayDatabase();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(true, path);
        verifyNoMoreInteractions(mockAlbumBirthdayCommand, mockAlbumCollectionCommand);
    }

    @Test
    void shouldUpdateAlbumBirthdayAndAlbumCollectionWhenNoOptionsSelected() {
        command.call();
        verify(mockAlbumBirthdayCommand).updateAlbumBirthdayDatabase();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(false);
        verifyNoMoreInteractions(mockAlbumBirthdayCommand, mockAlbumCollectionCommand);
    }

    @Test
    void shouldUpdateAlbumBirthdayAndTruncateAndUpdateAlbumCollectionWhenOnlyForceOptionSelected() {
        command.force = true;
        command.call();
        verify(mockAlbumBirthdayCommand).updateAlbumBirthdayDatabase();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(true);
        verifyNoMoreInteractions(mockAlbumBirthdayCommand, mockAlbumCollectionCommand);
    }

    @Test
    void shouldUpdateAlbumBirthdayAndAlbumCollectionFromFolderWhenOnlyPathOptionSelected() {
        final Path path = Path.of(File.separator, "some", "other", "path");
        command.path = path;
        command.call();
        verify(mockAlbumBirthdayCommand).updateAlbumBirthdayDatabase();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(false, path);
        verifyNoMoreInteractions(mockAlbumBirthdayCommand, mockAlbumCollectionCommand);
    }

    @Test
    void shouldUpdateAlbumBirthdayAndTruncateAndUpdateAlbumCollectionFromFolderWhenOnlyPathAndForceOptionSelected() {
        final Path path = Path.of(File.separator, "some", "other", "path");
        command.path = path;
        command.force = true;
        command.call();
        verify(mockAlbumBirthdayCommand).updateAlbumBirthdayDatabase();
        verify(mockAlbumCollectionCommand).updateAlbumCollectionDatabase(true, path);
        verifyNoMoreInteractions(mockAlbumBirthdayCommand, mockAlbumCollectionCommand);
    }
}