package stolat.bootstrap.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stolat.bootstrap.dao.AlbumBirthdayDao;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AlbumBirthdayCommandTest {

    @Mock
    private AlbumBirthdayDao mockAlbumBirthdayDao;

    private AlbumBirthdayCommand albumBirthdayCommand;

    @BeforeEach
    void setUp() {
        albumBirthdayCommand = new AlbumBirthdayCommand(mockAlbumBirthdayDao);
    }

    @Test
    void shouldClearAndPopulateAlbumBirthdays() {
        albumBirthdayCommand.updateAlbumBirthdayDatabase();
        verify(mockAlbumBirthdayDao).clearAlbumBirthdays();
        verify(mockAlbumBirthdayDao).populateAlbumBirthdays();
    }
}