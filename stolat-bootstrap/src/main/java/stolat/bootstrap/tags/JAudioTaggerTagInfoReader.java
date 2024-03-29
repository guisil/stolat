package stolat.bootstrap.tags;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import stolat.bootstrap.filesystem.FileSystemProperties;
import stolat.model.Album;
import stolat.model.Track;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Profile("jaudiotagger")
@Component
@AllArgsConstructor
@Slf4j
public class JAudioTaggerTagInfoReader implements TagInfoReader {

    private final FileSystemProperties fileSystemProperties;
    private final JAudioTaggerAudioFileProvider audioFileProvider;

    @Override
    public Optional<Track> getTrackInfo(File file) {

        try {
            final AudioFile audioFile = audioFileProvider.getAudioFile(file);
            final Tag tag = audioFile.getTag();
            final AudioHeader audioHeader = audioFile.getAudioHeader();

            String artistDisplayName = tag.getFirst(FieldKey.ALBUM_ARTIST);
            if (artistDisplayName == null || artistDisplayName.isBlank()) {
                artistDisplayName = tag.getFirst(FieldKey.ARTIST);
            }

            final Album album = new Album(
                    tag.getFirst(FieldKey.MUSICBRAINZ_RELEASE_GROUP_ID),
                    tag.getFirst(FieldKey.ALBUM),
                    tag.getAll(FieldKey.MUSICBRAINZ_ARTISTID),
                    tag.getAll(FieldKey.ARTISTS),
                    artistDisplayName);

            Path rootCollectionPath = Paths.get(fileSystemProperties.getAlbumCollectionPath());
            Path other = file.toPath();
            final String relativePath = rootCollectionPath.relativize(other).toString();

            final Track track = new Track(
                    tag.getFirst(FieldKey.MUSICBRAINZ_TRACK_ID),
                    tag.getFirst(FieldKey.DISC_NO),
                    tag.getFirst(FieldKey.TRACK),
                    tag.getFirst(FieldKey.TITLE),
                    audioHeader.getTrackLength(), relativePath, album);

            log.info("Retrieved track info for file {}", file);

            return Optional.of(track);

        } catch (TagException | ReadOnlyFileException | CannotReadException |
                InvalidAudioFrameException | IOException | IllegalArgumentException ex) {
            log.warn("Could not get track info for file {} - {}", file, ex.getMessage());
            log.trace("", ex);
        }

        return Optional.empty();
    }
}
