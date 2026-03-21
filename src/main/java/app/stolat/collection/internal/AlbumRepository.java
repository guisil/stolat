package app.stolat.collection.internal;

import java.util.UUID;

import app.stolat.collection.Album;
import org.springframework.data.jpa.repository.JpaRepository;

interface AlbumRepository extends JpaRepository<Album, UUID> {
}
