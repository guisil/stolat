package app.stolat.collection.internal;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TagReader {

    private static final Logger log = LoggerFactory.getLogger(TagReader.class);

    public Optional<AudioFileMetadata> read(Path audioFile) {
        try {
            var file = AudioFileIO.read(audioFile.toFile());
            var tag = file.getTag();
            if (tag == null) {
                return Optional.empty();
            }
            return Optional.of(extractMetadata(tag));
        } catch (Exception e) {
            log.warn("Failed to read tags from {}: {}", audioFile, e.getMessage());
            return Optional.empty();
        }
    }

    private AudioFileMetadata extractMetadata(Tag tag) {
        return new AudioFileMetadata(
                tag.getFirst(FieldKey.ARTIST),
                parseUuid(tag.getFirst(FieldKey.MUSICBRAINZ_ARTISTID)),
                tag.getFirst(FieldKey.ALBUM),
                parseUuid(tag.getFirst(FieldKey.MUSICBRAINZ_RELEASE_GROUP_ID)),
                tag.getFirst(FieldKey.TITLE),
                parseIntOrDefault(tag.getFirst(FieldKey.TRACK), 0),
                parseIntOrDefault(tag.getFirst(FieldKey.DISC_NO), 1),
                parseUuid(tag.getFirst(FieldKey.MUSICBRAINZ_TRACK_ID))
        );
    }

    private UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.split("/")[0].trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
