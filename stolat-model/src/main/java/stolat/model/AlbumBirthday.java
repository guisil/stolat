package stolat.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;

@Getter
@EqualsAndHashCode
@ToString
public class AlbumBirthday {

    private final Album album;

    private final Year albumYear;
    private final YearMonth albumYearMonth;
    private final LocalDate albumCompleteDate;

    public AlbumBirthday(
            @NonNull Album album,
            int year) {
        this.album = album;
        this.albumYear = Year.of(year);
        this.albumYearMonth = null;
        this.albumCompleteDate = null;
    }

    public AlbumBirthday(
            @NonNull Album album,
            int year, int month) {
        this.album = album;
        this.albumYear = Year.of(year);
        this.albumYearMonth = YearMonth.of(year, month);
        this.albumCompleteDate = null;
    }

    public AlbumBirthday(
            @NonNull Album album,
            int year, int month, int day) {
        this.album = album;
        this.albumYear = Year.of(year);
        this.albumYearMonth = YearMonth.of(year, month);
        this.albumCompleteDate = LocalDate.of(year, month, day);
    }
}
