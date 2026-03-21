package app.stolat.collection;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import app.stolat.collection.internal.AlbumRepository;
import app.stolat.collection.internal.ArtistRepository;
import app.stolat.collection.internal.AudioFileMetadata;
import app.stolat.collection.internal.DiscogsClient;
import app.stolat.collection.internal.DiscogsRelease;
import app.stolat.collection.internal.FileScanner;
import app.stolat.collection.internal.MusicBrainzSearchClient;
import app.stolat.collection.internal.TagReader;
import app.stolat.collection.internal.TrackRepository;
import app.stolat.collection.internal.VolumioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@Transactional
@Validated
public class CollectionService {

    private final FileScanner fileScanner;
    private final TagReader tagReader;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final TrackRepository trackRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final DiscogsClient discogsClient;
    private final MusicBrainzSearchClient musicBrainzSearchClient;
    private final VolumioClient volumioClient;
    private final TransactionTemplate transactionTemplate;

    public CollectionService(FileScanner fileScanner,
                             TagReader tagReader,
                             ArtistRepository artistRepository,
                             AlbumRepository albumRepository,
                             TrackRepository trackRepository,
                             ApplicationEventPublisher eventPublisher,
                             @Nullable DiscogsClient discogsClient,
                             MusicBrainzSearchClient musicBrainzSearchClient,
                             @Nullable VolumioClient volumioClient,
                             TransactionTemplate transactionTemplate) {
        this.fileScanner = fileScanner;
        this.tagReader = tagReader;
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
        this.trackRepository = trackRepository;
        this.eventPublisher = eventPublisher;
        this.discogsClient = discogsClient;
        this.musicBrainzSearchClient = musicBrainzSearchClient;
        this.volumioClient = volumioClient;
        this.transactionTemplate = transactionTemplate;
    }

    public List<Album> findAllAlbums() {
        return albumRepository.findAll();
    }

    public List<Album> findAlbumsByFormat(AlbumFormat format) {
        return albumRepository.findByFormat(format);
    }

    public List<Album> findAllActiveAlbums() {
        return albumRepository.findAllActive();
    }

    public void playAlbumOnVolumio(String albumTitle, String artistName) {
        if (volumioClient == null) {
            throw new IllegalStateException("Volumio is not configured");
        }
        volumioClient.playAlbum(albumTitle, artistName);
    }

    public void updateAlbumReleaseDate(UUID albumMusicBrainzId, LocalDate releaseDate) {
        albumRepository.findByMusicBrainzId(albumMusicBrainzId)
                .ifPresent(album -> {
                    album.updateReleaseDate(releaseDate);
                    albumRepository.save(album);
                });
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NEVER)
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

        var scannedMusicBrainzIds = new HashSet<>(albumGroups.keySet());

        var albums = albumGroups.values().stream()
                .map(metadata -> transactionTemplate.execute(status -> importFromMetadata(metadata)))
                .toList();

        transactionTemplate.executeWithoutResult(status -> reconcileDigitalFormats(scannedMusicBrainzIds));

        log.info("Scan complete: {} albums processed", albums.size());
        return albums;
    }

    public Album importAlbum(String artistName, UUID artistMusicBrainzId,
                             String albumTitle, UUID albumMusicBrainzId) {
        return importAlbum(artistName, artistMusicBrainzId, albumTitle, albumMusicBrainzId,
                AlbumFormat.DIGITAL, List.of());
    }

    public Album importAlbum(String artistName, UUID artistMusicBrainzId,
                             String albumTitle, UUID albumMusicBrainzId,
                             AlbumFormat format, List<TrackData> tracks) {
        var existing = albumRepository.findByMusicBrainzId(albumMusicBrainzId);
        if (existing.isPresent()) {
            var album = existing.get();
            if (!album.hasFormat(format)) {
                album.addFormat(format);
                albumRepository.save(album);
            }
            return album;
        }

        var artist = artistRepository.findByMusicBrainzId(artistMusicBrainzId)
                .orElseGet(() -> artistRepository.save(new Artist(artistName, artistMusicBrainzId)));

        var newAlbum = new Album(albumTitle, albumMusicBrainzId, artist);
        newAlbum.addFormat(format);
        var savedAlbum = albumRepository.save(newAlbum);

        if (!tracks.isEmpty()) {
            var trackEntities = tracks.stream()
                    .map(td -> new Track(td.title(), td.trackNumber(), td.discNumber(), td.musicBrainzId(), savedAlbum))
                    .toList();
            trackRepository.saveAll(trackEntities);
        }

        eventPublisher.publishEvent(new AlbumDiscoveredEvent(
                savedAlbum.getId(), savedAlbum.getTitle(), artist.getName(), savedAlbum.getMusicBrainzId()));

        return savedAlbum;
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NEVER)
    public List<Album> scanDiscogs(String username) {
        if (discogsClient == null) {
            throw new IllegalStateException("Discogs is not configured");
        }

        log.info("Scanning Discogs collection for user: {}", username);

        var importedAlbums = new ArrayList<Album>();
        var scannedDiscogsIds = new HashSet<Long>();
        boolean fullScanCompleted = true;

        try {
            var releases = discogsClient.fetchCollection(username);
            log.info("Fetched {} releases from Discogs", releases.size());

            for (var release : releases) {
                scannedDiscogsIds.add(release.discogsId());
                var album = transactionTemplate.execute(status -> importDiscogsRelease(release));
                if (album != null) {
                    importedAlbums.add(album);
                    log.info("Imported vinyl: '{}' by '{}'", release.albumTitle(), release.artistName());
                }
            }
        } catch (Exception e) {
            log.error("Discogs scan failed: {}", e.getMessage());
            fullScanCompleted = false;
        }

        if (fullScanCompleted) {
            transactionTemplate.executeWithoutResult(status -> reconcileVinylFormats(scannedDiscogsIds));
        }

        log.info("Discogs scan complete: {} albums processed", importedAlbums.size());
        return importedAlbums;
    }

    private Album importDiscogsRelease(DiscogsRelease release) {
        // 1. Check if album exists by discogsId
        var byDiscogsId = albumRepository.findByDiscogsId(release.discogsId());
        if (byDiscogsId.isPresent()) {
            var album = byDiscogsId.get();
            if (!album.hasFormat(AlbumFormat.VINYL)) {
                album.addFormat(AlbumFormat.VINYL);
                albumRepository.save(album);
            }
            return album;
        }

        // 2. Check if album exists by artist+title (case-insensitive)
        var byArtistTitle = albumRepository.findByTitleAndArtistNameIgnoreCase(
                release.albumTitle(), release.artistName());
        if (byArtistTitle.isPresent()) {
            var album = byArtistTitle.get();
            album.addFormat(AlbumFormat.VINYL);
            album.assignDiscogsId(release.discogsId());
            albumRepository.save(album);
            return album;
        }

        // 3. Search MusicBrainz for MBID by artist+title
        var mbid = musicBrainzSearchClient.searchReleaseGroup(release.artistName(), release.albumTitle());

        // 4. Import as new album with VINYL format
        var artist = artistRepository.findByNameIgnoreCase(release.artistName())
                .orElseGet(() -> artistRepository.save(new Artist(release.artistName())));

        Album album;
        if (mbid.isPresent()) {
            // Check if we already have this MBID
            var byMbid = albumRepository.findByMusicBrainzId(mbid.get());
            if (byMbid.isPresent()) {
                album = byMbid.get();
                album.addFormat(AlbumFormat.VINYL);
                album.assignDiscogsId(release.discogsId());
                albumRepository.save(album);
                return album;
            }
            album = new Album(release.albumTitle(), mbid.get(), artist);
            album.addFormat(AlbumFormat.VINYL);
            album.assignDiscogsId(release.discogsId());
            album = albumRepository.save(album);

            eventPublisher.publishEvent(new AlbumDiscoveredEvent(
                    album.getId(), album.getTitle(), artist.getName(), album.getMusicBrainzId()));
        } else {
            album = new Album(release.albumTitle(), artist, release.discogsId());
            album.addFormat(AlbumFormat.VINYL);
            album = albumRepository.save(album);
        }

        return album;
    }

    private void reconcileDigitalFormats(java.util.Set<UUID> scannedMusicBrainzIds) {
        albumRepository.findByFormat(AlbumFormat.DIGITAL).stream()
                .filter(a -> a.getMusicBrainzId() != null && !scannedMusicBrainzIds.contains(a.getMusicBrainzId()))
                .forEach(a -> {
                    a.removeFormat(AlbumFormat.DIGITAL);
                    albumRepository.save(a);
                    log.info("Removed DIGITAL format from '{}' (no longer in filesystem)", a.getTitle());
                });
    }

    private void reconcileVinylFormats(java.util.Set<Long> scannedDiscogsIds) {
        albumRepository.findByFormat(AlbumFormat.VINYL).stream()
                .filter(a -> a.getDiscogsId() != null && !scannedDiscogsIds.contains(a.getDiscogsId()))
                .forEach(a -> {
                    a.removeFormat(AlbumFormat.VINYL);
                    albumRepository.save(a);
                    log.info("Removed VINYL format from '{}' (no longer in Discogs collection)", a.getTitle());
                });
    }

    private Album importFromMetadata(List<AudioFileMetadata> trackMetadataList) {
        var first = trackMetadataList.getFirst();
        var tracks = trackMetadataList.stream()
                .map(m -> new TrackData(m.trackTitle(), m.trackNumber(), m.discNumber(), m.trackMusicBrainzId()))
                .toList();
        return importAlbum(first.artistName(), first.artistMusicBrainzId(),
                first.albumTitle(), first.albumMusicBrainzId(), AlbumFormat.DIGITAL, tracks);
    }
}
