package app.stolat.collection;

import java.util.UUID;

public record TrackData(String title, int trackNumber, int discNumber, UUID musicBrainzId) {
}
