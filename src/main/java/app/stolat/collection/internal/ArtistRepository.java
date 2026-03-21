package app.stolat.collection.internal;

import java.util.UUID;

import app.stolat.collection.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

interface ArtistRepository extends JpaRepository<Artist, UUID> {
}
