package stolat.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.jaudiotagger.tag.FieldKey;

import java.util.UUID;

@Getter
@EqualsAndHashCode
@ToString
public class Track {

    private final UUID trackMbId;
    private final int trackNumber;
    private final String trackName;
    private final int trackLength;
    private final String trackRelativePath;
    private final String trackFileType;
    private final Album album;
    private int discNumber = 1;

    @JsonCreator
    public Track(
            @JsonProperty("trackMusicBrainzId") @NonNull UUID trackMbId,
            @JsonProperty("discNumber") int discNumber,
            @JsonProperty("trackNumber") int trackNumber,
            @JsonProperty("trackName") @NonNull String trackName,
            @JsonProperty("trackLength") int trackLength,
            @JsonProperty("trackRelativePath") @NonNull String trackRelativePath,
            @JsonProperty("album") @NonNull Album album) {
        this.trackMbId = trackMbId;
        this.discNumber = discNumber;
        this.trackNumber = trackNumber;
        this.trackName = trackName;
        this.trackLength = trackLength;
        this.trackRelativePath = trackRelativePath;
        this.trackFileType = getFileType(trackRelativePath);
        this.album = album;
    }

    public Track(
            String trackMbidTag, String trackNumberTag,
            String trackNameTag, int trackLength, String trackRelativePath, Album album) {
        this(trackMbidTag, "", trackNumberTag, trackNameTag, trackLength, trackRelativePath, album);
    }

    public Track(
            String trackMbidTag, String discNumberTag, String trackNumberTag,
            String trackNameTag, int trackLength, String trackRelativePath, Album album) {

        this.trackMbId = TagValidator.getUUID(FieldKey.MUSICBRAINZ_TRACK_ID.name(), trackMbidTag);
        if (discNumberTag != null && !discNumberTag.isBlank()) {
            this.discNumber = TagValidator.getPositiveInteger(FieldKey.DISC_NO.name(), discNumberTag);
        }
        this.trackNumber = TagValidator.getPositiveInteger(FieldKey.TRACK.name(), trackNumberTag);
        this.trackName = TagValidator.getString(FieldKey.TITLE.name(), trackNameTag);
        this.trackLength = TagValidator.getPositiveInteger("track length", Integer.toString(trackLength));
        this.trackRelativePath = TagValidator.getString("track relative path", trackRelativePath);
        this.trackFileType = getFileType(trackRelativePath);
        if (album != null) {
            this.album = album;
        } else {
            throw new IllegalArgumentException("Track album is null");
        }
    }

    private String getFileType(String path) {
        String extension = "";
        if (path != null) {
            int index = path.lastIndexOf(".");
            if (index > -1) {
                extension = path.substring(index);
            }
        }
        return extension;
    }
}
