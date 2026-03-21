package app.stolat.collection.internal;

import java.util.UUID;

record AudioFileMetadata(
        String artistName,
        UUID artistMusicBrainzId,
        String albumTitle,
        UUID albumMusicBrainzId,
        String trackTitle,
        int trackNumber,
        int discNumber,
        UUID trackMusicBrainzId
) {
}
