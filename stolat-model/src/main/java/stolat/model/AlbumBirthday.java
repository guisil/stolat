package stolat.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.time.DateTimeException;
import java.time.Month;
import java.time.MonthDay;

@Getter
@EqualsAndHashCode
@ToString
public class AlbumBirthday {

    private Album album;
    private int albumYear;
    private int albumMonth;
    private int albumDay;

    @JsonCreator
    public AlbumBirthday(
            @JsonProperty("album") @NonNull Album album,
            @JsonProperty("albumYear") @NonNull Integer albumYear,
            @JsonProperty("albumMonth") Integer albumMonth,
            @JsonProperty("albumDay") Integer albumDay) {
        this.album = album;
        this.albumYear = albumYear;
        if (albumMonth != null) {
            this.albumMonth = albumMonth;
        }
        if (albumDay != null) {
            this.albumDay = albumDay;
        }
        validateAlbumMonthAndDay();
    }

    private void validateAlbumMonthAndDay() {
        if (albumMonth > 0) {
            try {
                Month.of(albumMonth);
            } catch (DateTimeException ex) {
                final var message = "Invalid month (" + albumMonth + ")";
                throw new IllegalArgumentException(message, ex);
            }
        } else if (albumDay > 0) {
            final var message = "Invalid month (" + albumMonth + ") and day (" + albumDay + ") combination";
            throw new IllegalArgumentException(message);
        }
        if (albumDay > 0 && albumMonth > 0) {
            try {
                MonthDay.of(albumMonth, albumDay);
            } catch(DateTimeException ex) {
                final var message = "Invalid month (" + albumMonth + ") and day (" + albumDay + ") combination";
                throw new IllegalArgumentException(message, ex);
            }
        }
    }
}
