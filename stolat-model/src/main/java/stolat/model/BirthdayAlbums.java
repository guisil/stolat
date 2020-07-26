package stolat.model;

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

    public BirthdayAlbums(
            @NonNull MonthDay from,
            @NonNull MonthDay to,
            @NonNull List<AlbumBirthday> albumBirthdays) {
        this.from = from;
        this.to = to;
        this.albumBirthdays = albumBirthdays;
    }
}
