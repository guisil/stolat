package app.stolat.collection.internal;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
class FileScanner {

    private static final Set<String> AUDIO_EXTENSIONS = Set.of(
            ".flac", ".mp3", ".ogg", ".m4a", ".wma", ".wav", ".aiff", ".ape"
    );

    List<Path> scan(Path rootDirectory) {
        if (!Files.exists(rootDirectory)) {
            return List.of();
        }
        try (var stream = Files.walk(rootDirectory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(this::isAudioFile)
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to scan directory: " + rootDirectory, e);
        }
    }

    private boolean isAudioFile(Path path) {
        var fileName = path.getFileName().toString().toLowerCase();
        return AUDIO_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }
}
