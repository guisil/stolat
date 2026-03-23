package app.stolat.collection.internal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import app.stolat.collection.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistRepository extends JpaRepository<Artist, UUID> {

    Optional<Artist> findByMusicBrainzId(UUID musicBrainzId);

    List<Artist> findByNameIgnoreCase(String name);
}
