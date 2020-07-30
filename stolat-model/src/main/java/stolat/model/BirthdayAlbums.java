package stolat.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.time.MonthDay;
import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
public class BirthdayAlbums {

    private final MonthDay from;
    private final MonthDay to;
    private final List<AlbumBirthday> albumBirthdays;

    @JsonCreator
    public BirthdayAlbums(
            @JsonProperty("from") @NonNull MonthDay from,
            @JsonProperty("to") @NonNull MonthDay to,
            @JsonProperty("albumBirthdays") @NonNull List<AlbumBirthday> albumBirthdays) {
        this.from = from;
        this.to = to;
        this.albumBirthdays = albumBirthdays;
    }
}
