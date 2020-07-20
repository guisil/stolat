package stolat.bootstrap.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.UUID;

@Getter
@EqualsAndHashCode
@ToString
public class AlbumBirthday {

    private final UUID albumMusicBrainzId;
    private final UUID artistMusicBrainzId;

    private final Year albumYear;
    private final YearMonth albumYearMonth;
    private final LocalDate albumCompleteDate;

    public AlbumBirthday(
            UUID albumMusicBrainzId, UUID artistMusicBrainzId,
            int year) {
        this.albumMusicBrainzId = albumMusicBrainzId;
        this.artistMusicBrainzId = artistMusicBrainzId;
        this.albumYear = Year.of(year);
        this.albumYearMonth = null;
        this.albumCompleteDate = null;
    }

    public AlbumBirthday(
            UUID albumMusicBrainzId, UUID artistMusicBrainzId,
            int year, int month) {
        this.albumMusicBrainzId = albumMusicBrainzId;
        this.artistMusicBrainzId = artistMusicBrainzId;
        this.albumYear = Year.of(year);
        this.albumYearMonth = YearMonth.of(year, month);
        this.albumCompleteDate = null;
    }

    public AlbumBirthday(
            UUID albumMusicBrainzId, UUID artistMusicBrainzId,
            int year, int month, int day) {
        this.albumMusicBrainzId = albumMusicBrainzId;
        this.artistMusicBrainzId = artistMusicBrainzId;
        this.albumYear = Year.of(year);
        this.albumYearMonth = YearMonth.of(year, month);
        this.albumCompleteDate = LocalDate.of(year, month, day);
    }
}
