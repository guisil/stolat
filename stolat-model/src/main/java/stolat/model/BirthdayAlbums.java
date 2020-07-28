package stolat.model;

import lombok.*;

import java.time.MonthDay;
import java.util.List;

@NoArgsConstructor
@Data
public class BirthdayAlbums {

    private MonthDay from;
    private MonthDay to;
    private List<AlbumBirthday> albumBirthdays;

    public BirthdayAlbums(
            @NonNull MonthDay from,
            @NonNull MonthDay to,
            @NonNull List<AlbumBirthday> albumBirthdays) {
        this.from = from;
        this.to = to;
        this.albumBirthdays = albumBirthdays;
    }
}
