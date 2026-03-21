package app.stolat.collection;

import java.util.List;
import java.util.UUID;

import app.stolat.collection.internal.AlbumRepository;
import app.stolat.collection.internal.ArtistRepository;
import app.stolat.collection.internal.TrackRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CollectionService {

    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final TrackRepository trackRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CollectionService(ArtistRepository artistRepository,
                             AlbumRepository albumRepository,
                             TrackRepository trackRepository,
                             ApplicationEventPublisher eventPublisher) {
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
        this.trackRepository = trackRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<Album> findAllAlbums() {
        return albumRepository.findAll();
    }

    public Album importAlbum(String artistName, UUID artistMusicBrainzId,
                             String albumTitle, UUID albumMusicBrainzId) {
        return importAlbum(artistName, artistMusicBrainzId, albumTitle, albumMusicBrainzId, List.of());
    }

    public Album importAlbum(String artistName, UUID artistMusicBrainzId,
                             String albumTitle, UUID albumMusicBrainzId,
                             List<TrackData> tracks) {
        var artist = artistRepository.findByMusicBrainzId(artistMusicBrainzId)
                .orElseGet(() -> artistRepository.save(new Artist(artistName, artistMusicBrainzId)));

        var album = albumRepository.save(new Album(albumTitle, albumMusicBrainzId, artist));

        if (!tracks.isEmpty()) {
            var trackEntities = tracks.stream()
                    .map(td -> new Track(td.title(), td.trackNumber(), td.discNumber(), td.musicBrainzId(), album))
                    .toList();
            trackRepository.saveAll(trackEntities);
        }

        eventPublisher.publishEvent(new AlbumDiscoveredEvent(album.getId(), album.getMusicBrainzId()));

        return album;
    }
}
