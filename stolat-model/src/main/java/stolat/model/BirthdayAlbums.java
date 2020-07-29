package stolat.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.MonthDay;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class BirthdayAlbums {
    @NonNull
    private MonthDay from;
    @NonNull
    private MonthDay to;
    @NonNull
    private List<AlbumBirthday> albumBirthdays;
}
