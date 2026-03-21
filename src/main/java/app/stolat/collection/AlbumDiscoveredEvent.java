package app.stolat.collection;

import java.util.UUID;

public record AlbumDiscoveredEvent(UUID albumId, UUID musicBrainzId) {
}
