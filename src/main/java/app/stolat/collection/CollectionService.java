package app.stolat.collection;

import java.util.List;
import java.util.UUID;

import app.stolat.collection.internal.AlbumRepository;
import app.stolat.collection.internal.ArtistRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CollectionService {

    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CollectionService(ArtistRepository artistRepository,
                             AlbumRepository albumRepository,
                             ApplicationEventPublisher eventPublisher) {
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<Album> findAllAlbums() {
        return albumRepository.findAll();
    }

    public Album importAlbum(String artistName, UUID artistMusicBrainzId,
                             String albumTitle, UUID albumMusicBrainzId) {
        var artist = artistRepository.findByMusicBrainzId(artistMusicBrainzId)
                .orElseGet(() -> artistRepository.save(new Artist(artistName, artistMusicBrainzId)));

        var album = albumRepository.save(new Album(albumTitle, albumMusicBrainzId, artist));

        eventPublisher.publishEvent(new AlbumDiscoveredEvent(album.getId(), album.getMusicBrainzId()));

        return album;
    }
}
