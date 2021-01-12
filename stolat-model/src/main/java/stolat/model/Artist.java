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

    private final UUID artistMbId;
    private final String artistName;

    @JsonCreator
    public Artist(
            @JsonProperty("artistMbId") @NonNull UUID artistMbId,
            @JsonProperty("artistName") @NonNull String artistName) {
        this.artistMbId = artistMbId;
        this.artistName = artistName;
    }

    public Artist(
            String artistMbidTag, String artistNameTag) {

        this.artistMbId = TagValidator.getUUID(FieldKey.MUSICBRAINZ_ARTISTID.name(), artistMbidTag);
        this.artistName = TagValidator.getString(FieldKey.ARTIST.name(), artistNameTag);
    }
}
