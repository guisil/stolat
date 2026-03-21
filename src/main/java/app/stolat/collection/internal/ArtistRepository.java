package app.stolat.collection.internal;

import java.util.UUID;

import java.util.Optional;

import app.stolat.collection.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistRepository extends JpaRepository<Artist, UUID> {

    Optional<Artist> findByMusicBrainzId(UUID musicBrainzId);

    Optional<Artist> findByNameIgnoreCase(String name);
}
