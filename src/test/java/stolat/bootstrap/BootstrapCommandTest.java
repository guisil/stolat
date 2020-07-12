package stolat.bootstrap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stolat.bootstrap.dao.AlbumBirthdayDao;

import java.io.File;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class BootstrapCommandTest {

    @Mock
    private AlbumBirthdayDao mockAlbumBirthdayDao;

    @InjectMocks
    private BootstrapCommand command;

    @Test
    void shouldPopulateAlbumBirthdayDataWhenBirthdayOptionSelected() {
        command.albumBirthday = true;
        command.call();
        verify(mockAlbumBirthdayDao).populateAlbumBirthdays();
        verifyNoMoreInteractions(mockAlbumBirthdayDao);
    }

    @Test
    void shouldIgnorePathOptionWhenBirthdayOptionSelected() {
        command.albumBirthday = true;
        command.path = new File("/some/path").toPath();
        command.call();
        verify(mockAlbumBirthdayDao).populateAlbumBirthdays();
        verifyNoMoreInteractions(mockAlbumBirthdayDao);
    }

    @Test
    void shouldIgnoreForceOptionWhenBirthdayOptionSelected() {
        command.albumBirthday = true;
        command.force = true;
        command.call();
        verify(mockAlbumBirthdayDao).populateAlbumBirthdays();
        verifyNoMoreInteractions(mockAlbumBirthdayDao);
    }

    @Test
    void shouldPopulateAlbumCollectionDataWhenCollectionOptionSelected() {
        fail("not tested yet");
    }

    @Test
    void shouldPopulateAlbumCollectionDataFromFolderWhenCollectionAndPathOptionsSelected() {
        fail("not tested yet");
    }

    @Test
    void shouldTruncateAndPopulateAlbumCollectionDataWhenCollectionAndForceOptionsSelected() {
        fail("not tested yet");
    }

    @Test
    void shouldPopulateAlbumBirthdayAndAlbumCollectionWhenBothOptionsSelected() {
        fail("not tested yet");
    }
}