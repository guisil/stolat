package app.stolat.collection;

import java.util.UUID;

public record AlbumDiscoveredEvent(UUID albumId, String albumTitle, String artistName, UUID musicBrainzId) {
}
