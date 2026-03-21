package app.stolat.collection.internal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import app.stolat.collection.Album;
import app.stolat.collection.AlbumFormat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlbumRepository extends JpaRepository<Album, UUID> {

    Optional<Album> findByMusicBrainzId(UUID musicBrainzId);

    Optional<Album> findByDiscogsId(Long discogsId);

    @Query("SELECT a FROM Album a WHERE a.formats IS NOT EMPTY")
    List<Album> findAllActive();

    @Query("SELECT a FROM Album a JOIN a.formats f WHERE f = :format")
    List<Album> findByFormat(@Param("format") AlbumFormat format);

    @Query("SELECT a FROM Album a WHERE LOWER(a.title) = LOWER(:title) AND LOWER(a.artist.name) = LOWER(:artistName)")
    Optional<Album> findByTitleAndArtistNameIgnoreCase(@Param("title") String title, @Param("artistName") String artistName);
}
