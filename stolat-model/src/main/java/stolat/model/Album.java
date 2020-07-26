package stolat.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.jaudiotagger.tag.FieldKey;

import java.util.UUID;

@Getter
@EqualsAndHashCode
@ToString
public class Album {

    private final UUID albumMusicBrainzId;
    private final String albumName;
    private final UUID artistMusicBrainzId;
    private final String artistName;

    public Album(
            @NonNull UUID albumMusicBrainzId,
            @NonNull String albumName,
            @NonNull UUID artistMusicBrainzId,
            @NonNull String artistName) {
        this.albumMusicBrainzId = albumMusicBrainzId;
        this.albumName = albumName;
        this.artistMusicBrainzId = artistMusicBrainzId;
        this.artistName = artistName;
    }

    public Album(
            String albumMbidTag, String albumNameTag,
            String artistMbidTag, String artistNameTag) {

        this.albumMusicBrainzId = TagValidator.getUUID(FieldKey.MUSICBRAINZ_RELEASE_GROUP_ID.name(), albumMbidTag);
        this.albumName = TagValidator.getString(FieldKey.ALBUM.name(), albumNameTag);
        this.artistMusicBrainzId = TagValidator.getUUID(FieldKey.MUSICBRAINZ_ARTISTID.name(), artistMbidTag);
        this.artistName = TagValidator.getString(FieldKey.ARTIST.name(), artistNameTag);
    }
}
