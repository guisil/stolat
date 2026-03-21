package app.stolat.collection.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class FileScannerTest {

    @TempDir
    Path musicDir;

    @Test
    void shouldFindAudioFilesRecursively() throws IOException {
        // artist/album directory structure
        var albumDir = Files.createDirectories(musicDir.resolve("Radiohead/OK Computer"));
        Files.createFile(albumDir.resolve("01 - Airbag.flac"));
        Files.createFile(albumDir.resolve("02 - Paranoid Android.flac"));
        Files.createFile(albumDir.resolve("cover.jpg"));

        var albumDir2 = Files.createDirectories(musicDir.resolve("Radiohead/Kid A"));
        Files.createFile(albumDir2.resolve("01 - Everything in Its Right Place.mp3"));

        var scanner = new FileScanner();
        var files = scanner.scan(musicDir);

        assertThat(files).hasSize(3);
        assertThat(files).allMatch(p -> {
            var name = p.getFileName().toString().toLowerCase();
            return name.endsWith(".flac") || name.endsWith(".mp3");
        });
    }

    @Test
    void shouldReturnEmptyListForEmptyDirectory() {
        var scanner = new FileScanner();
        var files = scanner.scan(musicDir);

        assertThat(files).isEmpty();
    }
}
