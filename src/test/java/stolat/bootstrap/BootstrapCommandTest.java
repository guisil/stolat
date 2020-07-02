package stolat.bootstrap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BootstrapCommandTest {

    private BootstrapCommand command;

    @BeforeEach
    void setUp() {
        command = new BootstrapCommand();
    }

    @Test
    void shouldPopulateAlbumBirthdayDataWhenBirthdayOptionSelected() {
        fail("not tested yet");
    }

    @Test
    void shouldIgnorePathOptionWhenBirthdayOptionSelected() {
        fail("not tested yet");
    }

    @Test
    void shouldIgnoreForceOptionWhenBirthdayOptionSelected() {
        fail("not tested yet");
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
}