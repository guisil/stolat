package app.stolat.collection.internal;

import java.util.UUID;

import app.stolat.collection.Track;
import org.springframework.data.jpa.repository.JpaRepository;

interface TrackRepository extends JpaRepository<Track, UUID> {
}
