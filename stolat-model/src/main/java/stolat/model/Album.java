package stolat.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.jaudiotagger.tag.FieldKey;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@EqualsAndHashCode
@ToString
public class Album {

    private final UUID albumMbId;
    private final String albumName;
    private final List<Artist> artists;

    @JsonCreator
    public Album(
            @JsonProperty("albumMusicBrainzId") @NonNull UUID albumMbId,
            @JsonProperty("albumName") @NonNull String albumName,
            @JsonProperty("artists") @NonNull List<Artist> artists) {
        this.albumMbId = albumMbId;
        this.albumName = albumName;
        this.artists = artists;
    }

    public Album(
            String albumMbidTag, String albumNameTag,
            List<String> artistMbidTags, List<String> artistNameTags) {

        this.albumMbId = TagValidator.getUUID(FieldKey.MUSICBRAINZ_RELEASE_GROUP_ID.name(), albumMbidTag);
        this.albumName = TagValidator.getString(FieldKey.ALBUM.name(), albumNameTag);
        TagValidator.checkListsHaveSameSize(artistMbidTags, artistNameTags, "artists MBIDs and names");
        artists = new ArrayList<>();
        for (int i = 0; i < artistMbidTags.size(); i++ ) {
            artists.add(new Artist(artistMbidTags.get(i), artistNameTags.get(i)));
        }
    }
}
