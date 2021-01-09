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
public class Artist {

    private final UUID artistMusicBrainzId;
    private final String artistName;

    @JsonCreator
    public Artist(
            @JsonProperty("artistMusicBrainzId") @NonNull UUID artistMusicBrainzId,
            @JsonProperty("artistName") @NonNull String artistName) {
        this.artistMusicBrainzId = artistMusicBrainzId;
        this.artistName = artistName;
    }

    public Artist(
            String artistMbidTag, String artistNameTag) {

        this.artistMusicBrainzId = TagValidator.getUUID(FieldKey.MUSICBRAINZ_ARTISTID.name(), artistMbidTag);
        this.artistName = TagValidator.getString(FieldKey.ARTIST.name(), artistNameTag);
    }
}
