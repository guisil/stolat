package stolat.bootstrap.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jaudiotagger.tag.FieldKey;

import java.time.Instant;
import java.util.UUID;

@Getter
@EqualsAndHashCode
@ToString
public class Track {

    private final UUID trackMusicBrainzId;
    private final int trackNumber;
    private final String trackName;
    private final int trackLength;
    private final String trackRelativePath;
    private final Album album;
    private final Instant lastUpdated;
    private int discNumber = 1;

    public Track(
            String trackMbidTag, String trackNumberTag,
            String trackNameTag, int trackLength, String trackRelativePath, Album album, Instant lastUpdated) {
        this(trackMbidTag, "", trackNumberTag, trackNameTag, trackLength, trackRelativePath, album, lastUpdated);
    }

    public Track(
            String trackMbidTag, String discNumberTag, String trackNumberTag,
            String trackNameTag, int trackLength, String trackRelativePath, Album album, Instant lastUpdated) {

        this.trackMusicBrainzId = TagValidator.getUUID(FieldKey.MUSICBRAINZ_TRACK_ID.name(), trackMbidTag);
        if (discNumberTag != null && !discNumberTag.isBlank()) {
            this.discNumber = TagValidator.getPositiveInteger(FieldKey.DISC_NO.name(), discNumberTag);
        }
        this.trackNumber = TagValidator.getPositiveInteger(FieldKey.TRACK.name(), trackNumberTag);
        this.trackName = TagValidator.getString(FieldKey.TITLE.name(), trackNameTag);
        this.trackLength = TagValidator.getPositiveInteger("track length", Integer.toString(trackLength));
        this.trackRelativePath = TagValidator.getString("track relative path", trackRelativePath);
        this.album = album;

        this.lastUpdated = lastUpdated;
    }
}
