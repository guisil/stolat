package app.stolat.collection.internal;

import java.util.Optional;
import java.util.UUID;

import app.stolat.collection.Album;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album, UUID> {

    Optional<Album> findByMusicBrainzId(UUID musicBrainzId);
}
