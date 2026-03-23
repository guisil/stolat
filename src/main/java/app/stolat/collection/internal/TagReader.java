package app.stolat.collection.internal;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TagReader {

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
        var year = parseYear(tag.getFirst(FieldKey.YEAR));
        var artistName = firstNonBlank(tag.getFirst(FieldKey.ALBUM_ARTIST), tag.getFirst(FieldKey.ARTIST));
        return new AudioFileMetadata(
                artistName,
                parseUuid(tag.getFirst(FieldKey.MUSICBRAINZ_ARTISTID)),
                tag.getFirst(FieldKey.ALBUM),
                parseUuid(tag.getFirst(FieldKey.MUSICBRAINZ_RELEASE_GROUP_ID)),
                tag.getFirst(FieldKey.TITLE),
                parseIntOrDefault(tag.getFirst(FieldKey.TRACK), 0),
                parseIntOrDefault(tag.getFirst(FieldKey.DISC_NO), 1),
                parseUuid(tag.getFirst(FieldKey.MUSICBRAINZ_TRACK_ID)),
                year != null && year > 0 ? year : null
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

    private Integer parseYear(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            // Handle "2015", "2015-01-19", "2015-01" etc. — extract first 4 digits
            var yearStr = value.length() >= 4 ? value.substring(0, 4) : value;
            var year = Integer.parseInt(yearStr);
            return year > 0 ? year : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String firstNonBlank(String... values) {
        for (var value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
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
