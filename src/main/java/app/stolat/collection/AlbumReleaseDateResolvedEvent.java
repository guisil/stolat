package app.stolat.collection;

import java.time.LocalDate;
import java.util.UUID;

public record AlbumReleaseDateResolvedEvent(UUID albumId, String albumTitle, String artistName,
                                             LocalDate releaseDate, Long discogsId) {}
