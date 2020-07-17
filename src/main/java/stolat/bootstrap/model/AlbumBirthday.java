package stolat.bootstrap.model;

import lombok.*;

import java.util.UUID;

@Data
public class AlbumBirthday {

    private final UUID albumMusicBrainzId;
    private final UUID artistMusicBrainzId;

    //TODO validate date fields
    private final Integer albumYear;
    private final Integer albumMonth;
    private final Integer albumDay;
}
