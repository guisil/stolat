package app.stolat.collection;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import app.stolat.collection.internal.AlbumRepository;
import app.stolat.collection.internal.ArtistRepository;
import app.stolat.collection.internal.AudioFileMetadata;
import app.stolat.collection.internal.FileScanner;
import app.stolat.collection.internal.TagReader;
import app.stolat.collection.internal.TrackRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class CollectionService {

    private final FileScanner fileScanner;
    private final TagReader tagReader;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final TrackRepository trackRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CollectionService(FileScanner fileScanner,
                             TagReader tagReader,
                             ArtistRepository artistRepository,
                             AlbumRepository albumRepository,
                             TrackRepository trackRepository,
                             ApplicationEventPublisher eventPublisher) {
        this.fileScanner = fileScanner;
        this.tagReader = tagReader;
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
        this.trackRepository = trackRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<Album> findAllAlbums() {
        return albumRepository.findAll();
    }

    public void updateAlbumReleaseDate(UUID albumMusicBrainzId, LocalDate releaseDate) {
        albumRepository.findByMusicBrainzId(albumMusicBrainzId)
                .ifPresent(album -> {
                    album.updateReleaseDate(releaseDate);
                    albumRepository.save(album);
                });
    }

    public List<Album> scanDirectory(Path rootDirectory) {
        log.info("Scanning directory: {}", rootDirectory);
        var files = fileScanner.scan(rootDirectory);
        log.info("Found {} audio files", files.size());

        var albumGroups = files.stream()
                .map(tagReader::read)
                .flatMap(java.util.Optional::stream)
                .filter(m -> m.albumMusicBrainzId() != null)
                .collect(Collectors.groupingBy(AudioFileMetadata::albumMusicBrainzId));

        log.info("Grouped into {} albums", albumGroups.size());

        var albums = albumGroups.values().stream()
                .map(this::importFromMetadata)
                .toList();

        log.info("Imported {} new albums", albums.stream().filter(a -> a.getReleaseDate() == null).count());
        return albums;
    }

    public Album importAlbum(String artistName, UUID artistMusicBrainzId,
                             String albumTitle, UUID albumMusicBrainzId) {
        return importAlbum(artistName, artistMusicBrainzId, albumTitle, albumMusicBrainzId, List.of());
    }

    public Album importAlbum(String artistName, UUID artistMusicBrainzId,
                             String albumTitle, UUID albumMusicBrainzId,
                             List<TrackData> tracks) {
        var existing = albumRepository.findByMusicBrainzId(albumMusicBrainzId);
        if (existing.isPresent()) {
            return existing.get();
        }

        var artist = artistRepository.findByMusicBrainzId(artistMusicBrainzId)
                .orElseGet(() -> artistRepository.save(new Artist(artistName, artistMusicBrainzId)));

        var album = albumRepository.save(new Album(albumTitle, albumMusicBrainzId, artist));

        if (!tracks.isEmpty()) {
            var trackEntities = tracks.stream()
                    .map(td -> new Track(td.title(), td.trackNumber(), td.discNumber(), td.musicBrainzId(), album))
                    .toList();
            trackRepository.saveAll(trackEntities);
        }

        eventPublisher.publishEvent(new AlbumDiscoveredEvent(
                album.getId(), album.getTitle(), artist.getName(), album.getMusicBrainzId()));

        return album;
    }

    private Album importFromMetadata(List<AudioFileMetadata> trackMetadataList) {
        var first = trackMetadataList.getFirst();
        var tracks = trackMetadataList.stream()
                .map(m -> new TrackData(m.trackTitle(), m.trackNumber(), m.discNumber(), m.trackMusicBrainzId()))
                .toList();
        return importAlbum(first.artistName(), first.artistMusicBrainzId(),
                first.albumTitle(), first.albumMusicBrainzId(), tracks);
    }
}
