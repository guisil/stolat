package app.stolat.collection;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CollectionServiceTest {

    @Mock
    private FileScanner fileScanner;

    @Mock
    private TagReader tagReader;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private TrackRepository trackRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private DiscogsClient discogsClient;

    @Mock
    private MusicBrainzSearchClient musicBrainzSearchClient;

    @Mock
    private VolumioClient volumioClient;

    private CollectionService collectionService;

    @BeforeEach
    void setUp() {
        var transactionTemplate = new TransactionTemplate();
        transactionTemplate.setTransactionManager(new org.springframework.transaction.support.AbstractPlatformTransactionManager() {
            @Override
            protected Object doGetTransaction() { return new Object(); }
            @Override
            protected void doBegin(Object transaction, org.springframework.transaction.TransactionDefinition definition) {}
            @Override
            protected void doCommit(org.springframework.transaction.support.DefaultTransactionStatus status) {}
            @Override
            protected void doRollback(org.springframework.transaction.support.DefaultTransactionStatus status) {}
        });
        collectionService = new CollectionService(fileScanner, tagReader, artistRepository,
                albumRepository, trackRepository, eventPublisher, discogsClient,
                musicBrainzSearchClient, volumioClient, transactionTemplate);
    }

    @Test
    void shouldReturnAllAlbums() {
        var artist = new Artist("Radiohead", UUID.randomUUID());
        var album1 = new Album("OK Computer", UUID.randomUUID(), artist);
        var album2 = new Album("Kid A", UUID.randomUUID(), artist);
        given(albumRepository.findAll()).willReturn(List.of(album1, album2));

        var albums = collectionService.findAllAlbums();

        assertThat(albums).containsExactly(album1, album2);
    }

    @Test
    void shouldImportAlbumWithNewArtistAndPublishEvent() {
        var artistMbid = UUID.randomUUID();
        var albumMbid = UUID.randomUUID();
        given(artistRepository.findByMusicBrainzId(artistMbid)).willReturn(Optional.empty());
        given(artistRepository.save(any(Artist.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(albumRepository.save(any(Album.class))).willAnswer(invocation -> invocation.getArgument(0));

        var album = collectionService.importAlbum("Radiohead", artistMbid, "OK Computer", albumMbid);

        assertThat(album.getTitle()).isEqualTo("OK Computer");
        assertThat(album.getMusicBrainzId()).isEqualTo(albumMbid);
        assertThat(album.getArtist().getName()).isEqualTo("Radiohead");
        assertThat(album.getArtist().getMusicBrainzId()).isEqualTo(artistMbid);
        assertThat(album.getFormats()).containsExactly(AlbumFormat.DIGITAL);
        then(artistRepository).should().save(any(Artist.class));
        then(albumRepository).should().save(any(Album.class));
        then(eventPublisher).should().publishEvent(any(AlbumDiscoveredEvent.class));
    }

    @Test
    void shouldImportAlbumWithExistingArtist() {
        var artistMbid = UUID.randomUUID();
        var albumMbid = UUID.randomUUID();
        var existingArtist = new Artist("Radiohead", artistMbid);
        given(artistRepository.findByMusicBrainzId(artistMbid)).willReturn(Optional.of(existingArtist));
        given(albumRepository.save(any(Album.class))).willAnswer(invocation -> invocation.getArgument(0));

        var album = collectionService.importAlbum("Radiohead", artistMbid, "OK Computer", albumMbid);

        assertThat(album.getArtist()).isEqualTo(existingArtist);
        assertThat(album.getFormats()).containsExactly(AlbumFormat.DIGITAL);
        then(artistRepository).should().findByMusicBrainzId(artistMbid);
        then(artistRepository).shouldHaveNoMoreInteractions();
        then(eventPublisher).should().publishEvent(any(AlbumDiscoveredEvent.class));
    }

    @Test
    void shouldImportAlbumWithTracks() {
        var artistMbid = UUID.randomUUID();
        var albumMbid = UUID.randomUUID();
        var tracks = List.of(
                new TrackData("Airbag", 1, 1, UUID.randomUUID()),
                new TrackData("Paranoid Android", 2, 1, UUID.randomUUID()),
                new TrackData("Subterranean Homesick Alien", 3, 1, UUID.randomUUID())
        );
        given(artistRepository.findByMusicBrainzId(artistMbid)).willReturn(Optional.empty());
        given(artistRepository.save(any(Artist.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(albumRepository.save(any(Album.class))).willAnswer(invocation -> invocation.getArgument(0));
        var album = collectionService.importAlbum("Radiohead", artistMbid, "OK Computer", albumMbid,
                AlbumFormat.DIGITAL, tracks);

        assertThat(album.getTitle()).isEqualTo("OK Computer");
        assertThat(album.getFormats()).containsExactly(AlbumFormat.DIGITAL);
        then(trackRepository).should().saveAll(any());
        then(eventPublisher).should().publishEvent(any(AlbumDiscoveredEvent.class));
    }

    @Test
    void shouldScanDirectoryAndImportAlbums() {
        var rootDir = Path.of("/music");
        var artistMbid = UUID.randomUUID();
        var albumMbid = UUID.randomUUID();
        var track1Path = Path.of("/music/Radiohead/OK Computer/01 - Airbag.flac");
        var track2Path = Path.of("/music/Radiohead/OK Computer/02 - Paranoid Android.flac");

        given(fileScanner.scan(rootDir)).willReturn(List.of(track1Path, track2Path));
        given(tagReader.read(track1Path)).willReturn(Optional.of(
                new AudioFileMetadata("Radiohead", artistMbid, "OK Computer", albumMbid,
                        "Airbag", 1, 1, UUID.randomUUID(), null)));
        given(tagReader.read(track2Path)).willReturn(Optional.of(
                new AudioFileMetadata("Radiohead", artistMbid, "OK Computer", albumMbid,
                        "Paranoid Android", 2, 1, UUID.randomUUID(), null)));
        given(artistRepository.findByMusicBrainzId(artistMbid)).willReturn(Optional.empty());
        given(artistRepository.save(any(Artist.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(albumRepository.save(any(Album.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(albumRepository.findByFormat(AlbumFormat.DIGITAL)).willReturn(List.of());

        var albums = collectionService.scanDirectory(rootDir);

        assertThat(albums).hasSize(1);
        assertThat(albums.getFirst().getTitle()).isEqualTo("OK Computer");
        assertThat(albums.getFirst().getFormats()).containsExactly(AlbumFormat.DIGITAL);
        then(trackRepository).should().saveAll(any());
        then(eventPublisher).should().publishEvent(any(AlbumDiscoveredEvent.class));
    }

    @Test
    void shouldStoreFolderPathWhenScanningDirectory() {
        var rootDir = Path.of("/music");
        var artistMbid = UUID.randomUUID();
        var albumMbid = UUID.randomUUID();
        var trackPath = Path.of("/music/Radiohead/OK Computer/01 - Airbag.flac");

        given(fileScanner.scan(rootDir)).willReturn(List.of(trackPath));
        given(tagReader.read(trackPath)).willReturn(Optional.of(
                new AudioFileMetadata("Radiohead", artistMbid, "OK Computer", albumMbid,
                        "Airbag", 1, 1, UUID.randomUUID(), null)));
        given(artistRepository.findByMusicBrainzId(artistMbid)).willReturn(Optional.empty());
        given(artistRepository.save(any(Artist.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(albumRepository.save(any(Album.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(albumRepository.findByFormat(AlbumFormat.DIGITAL)).willReturn(List.of());

        var albums = collectionService.scanDirectory(rootDir);

        assertThat(albums.getFirst().getFolderPath()).isEqualTo("Radiohead/OK Computer");
    }

    @Test
    void shouldScanAndImportAlbumsWithoutMusicBrainzId() {
        var rootDir = Path.of("/music");
        var trackPath = Path.of("/music/SomeArtist/SomeAlbum/01 - Track.flac");

        given(fileScanner.scan(rootDir)).willReturn(List.of(trackPath));
        given(tagReader.read(trackPath)).willReturn(Optional.of(
                new AudioFileMetadata("Some Artist", null, "Some Album", null,
                        "Track", 1, 1, null, 2020)));
        given(artistRepository.findByNameIgnoreCase("Some Artist")).willReturn(List.of());
        given(artistRepository.save(any(Artist.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(albumRepository.findByTitleAndArtistNameIgnoreCase("Some Album", "Some Artist"))
                .willReturn(Optional.empty());
        given(albumRepository.save(any(Album.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(albumRepository.findByFormat(AlbumFormat.DIGITAL)).willReturn(List.of());

        var albums = collectionService.scanDirectory(rootDir);

        assertThat(albums).hasSize(1);
        assertThat(albums.getFirst().getTitle()).isEqualTo("Some Album");
        assertThat(albums.getFirst().getMusicBrainzId()).isNull();
        assertThat(albums.getFirst().getReleaseDate()).isEqualTo(LocalDate.of(2020, 1, 1));
        then(eventPublisher).should(never()).publishEvent(any(AlbumDiscoveredEvent.class));
        then(eventPublisher).should(never()).publishEvent(any(AlbumReleaseDateResolvedEvent.class));
    }

    @Test
    void shouldSkipFilesWithUnreadableTags() {
        var rootDir = Path.of("/music");
        var artistMbid = UUID.randomUUID();
        var albumMbid = UUID.randomUUID();
        var goodFile = Path.of("/music/Radiohead/OK Computer/01 - Airbag.flac");
        var badFile = Path.of("/music/corrupt.flac");

        given(fileScanner.scan(rootDir)).willReturn(List.of(goodFile, badFile));
        given(tagReader.read(goodFile)).willReturn(Optional.of(
                new AudioFileMetadata("Radiohead", artistMbid, "OK Computer", albumMbid,
                        "Airbag", 1, 1, UUID.randomUUID(), null)));
        given(tagReader.read(badFile)).willReturn(Optional.empty());
        given(artistRepository.findByMusicBrainzId(artistMbid)).willReturn(Optional.empty());
        given(artistRepository.save(any(Artist.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(albumRepository.save(any(Album.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(albumRepository.findByFormat(AlbumFormat.DIGITAL)).willReturn(List.of());

        var albums = collectionService.scanDirectory(rootDir);

        assertThat(albums).hasSize(1);
    }

    @Test
    void shouldUpdateAlbumReleaseDateWhenAlbumExists() {
        var albumMbid = UUID.randomUUID();
        var artist = new Artist("Radiohead", UUID.randomUUID());
        var album = new Album("OK Computer", albumMbid, artist);
        var releaseDate = LocalDate.of(1997, 6, 16);
        given(albumRepository.findByMusicBrainzId(albumMbid)).willReturn(Optional.of(album));
        given(albumRepository.save(album)).willReturn(album);

        collectionService.updateAlbumReleaseDate(albumMbid, releaseDate);

        assertThat(album.getReleaseDate()).isEqualTo(releaseDate);
        then(albumRepository).should().save(album);
    }

    @Test
    void shouldUpdateAlbumReleaseDateById() {
        var albumId = UUID.randomUUID();
        var releaseDate = LocalDate.of(2015, 1, 19);
        var artist = new Artist("Anushka", UUID.randomUUID());
        var album = new Album("Kisses", artist, 12345L);
        given(albumRepository.findById(albumId)).willReturn(Optional.of(album));
        given(albumRepository.save(album)).willReturn(album);

        collectionService.updateAlbumReleaseDateById(albumId, releaseDate);

        assertThat(album.getReleaseDate()).isEqualTo(releaseDate);
        then(albumRepository).should().save(album);
    }

    @Test
    void shouldDoNothingWhenUpdatingReleaseDateForNonExistentAlbum() {
        var albumMbid = UUID.randomUUID();
        var releaseDate = LocalDate.of(1997, 6, 16);
        given(albumRepository.findByMusicBrainzId(albumMbid)).willReturn(Optional.empty());

        collectionService.updateAlbumReleaseDate(albumMbid, releaseDate);

        then(albumRepository).should(never()).save(any());
    }

    @Test
    void shouldSkipAlreadyImportedAlbumAndAddFormat() {
        var artistMbid = UUID.randomUUID();
        var albumMbid = UUID.randomUUID();
        var existingArtist = new Artist("Radiohead", artistMbid);
        var existingAlbum = new Album("OK Computer", albumMbid, existingArtist);
        given(albumRepository.findByMusicBrainzId(albumMbid)).willReturn(Optional.of(existingAlbum));
        given(albumRepository.save(existingAlbum)).willReturn(existingAlbum);

        var album = collectionService.importAlbum("Radiohead", artistMbid, "OK Computer", albumMbid);

        assertThat(album).isEqualTo(existingAlbum);
        assertThat(album.getFormats()).containsExactly(AlbumFormat.DIGITAL);
        then(eventPublisher).shouldHaveNoInteractions();
    }

    @Test
    void shouldAddDigitalFormatWhenImportingAlbum() {
        var artistMbid = UUID.randomUUID();
        var albumMbid = UUID.randomUUID();
        given(artistRepository.findByMusicBrainzId(artistMbid)).willReturn(Optional.empty());
        given(artistRepository.save(any(Artist.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(albumRepository.save(any(Album.class))).willAnswer(invocation -> invocation.getArgument(0));

        var album = collectionService.importAlbum("Radiohead", artistMbid, "OK Computer", albumMbid);

        assertThat(album.getFormats()).containsExactly(AlbumFormat.DIGITAL);
    }

    @Test
    void shouldAddFormatToExistingAlbumWhenReimporting() {
        var artistMbid = UUID.randomUUID();
        var albumMbid = UUID.randomUUID();
        var existingArtist = new Artist("Radiohead", artistMbid);
        var existingAlbum = new Album("OK Computer", albumMbid, existingArtist);
        existingAlbum.addFormat(AlbumFormat.DIGITAL);

        given(albumRepository.findByMusicBrainzId(albumMbid)).willReturn(Optional.of(existingAlbum));
        given(albumRepository.save(existingAlbum)).willReturn(existingAlbum);

        var album = collectionService.importAlbum("Radiohead", artistMbid, "OK Computer", albumMbid,
                AlbumFormat.VINYL, List.of());

        assertThat(album.getFormats()).containsExactlyInAnyOrder(AlbumFormat.DIGITAL, AlbumFormat.VINYL);
        then(albumRepository).should().save(existingAlbum);
    }

    @Test
    void shouldReconcileDigitalFormatsOnScan() {
        var rootDir = Path.of("/music");
        var artistMbid = UUID.randomUUID();
        var albumMbid = UUID.randomUUID();
        var track1Path = Path.of("/music/Radiohead/OK Computer/01 - Airbag.flac");

        // Album that IS in the scan
        given(fileScanner.scan(rootDir)).willReturn(List.of(track1Path));
        given(tagReader.read(track1Path)).willReturn(Optional.of(
                new AudioFileMetadata("Radiohead", artistMbid, "OK Computer", albumMbid,
                        "Airbag", 1, 1, UUID.randomUUID(), null)));
        given(artistRepository.findByMusicBrainzId(artistMbid)).willReturn(Optional.empty());
        given(artistRepository.save(any(Artist.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(albumRepository.save(any(Album.class))).willAnswer(invocation -> invocation.getArgument(0));

        // Album that is NOT in the scan but has DIGITAL format
        var removedArtist = new Artist("Portishead", UUID.randomUUID());
        var removedAlbum = new Album("Dummy", UUID.randomUUID(), removedArtist);
        removedAlbum.addFormat(AlbumFormat.DIGITAL);
        given(albumRepository.findByFormat(AlbumFormat.DIGITAL)).willReturn(List.of(removedAlbum));

        collectionService.scanDirectory(rootDir);

        assertThat(removedAlbum.hasFormat(AlbumFormat.DIGITAL)).isFalse();
    }

    @Test
    void shouldReconcileNonMbidDigitalFormatsOnScan() {
        var rootDir = Path.of("/music");
        var track1Path = Path.of("/music/Various/Compilation/01 - Track.flac");

        // Album that IS in the scan (no MBID)
        var scannedArtist = new Artist("Various Artists");
        var scannedAlbum = new Album("Compilation", scannedArtist, null);
        scannedAlbum.addFormat(AlbumFormat.DIGITAL);
        given(fileScanner.scan(rootDir)).willReturn(List.of(track1Path));
        given(tagReader.read(track1Path)).willReturn(Optional.of(
                new AudioFileMetadata("Various Artists", null, "Compilation", null,
                        "Track", 1, 1, null, 2014)));
        given(albumRepository.findByTitleAndArtistNameIgnoreCase("Compilation", "Various Artists"))
                .willReturn(Optional.of(scannedAlbum));

        // Non-MBID album that is NOT in the scan but has DIGITAL format
        var removedArtist = new Artist("Old Artist");
        var removedAlbum = new Album("Old Album", removedArtist, null);
        removedAlbum.addFormat(AlbumFormat.DIGITAL);
        given(albumRepository.findByFormat(AlbumFormat.DIGITAL))
                .willReturn(List.of(scannedAlbum, removedAlbum));

        collectionService.scanDirectory(rootDir);

        assertThat(scannedAlbum.hasFormat(AlbumFormat.DIGITAL)).isTrue();
        assertThat(removedAlbum.hasFormat(AlbumFormat.DIGITAL)).isFalse();
    }

    @Test
    void shouldReturnAlbumsByFormat() {
        var artist = new Artist("Radiohead", UUID.randomUUID());
        var album = new Album("OK Computer", UUID.randomUUID(), artist);
        album.addFormat(AlbumFormat.DIGITAL);
        given(albumRepository.findByFormat(AlbumFormat.DIGITAL)).willReturn(List.of(album));

        var albums = collectionService.findAlbumsByFormat(AlbumFormat.DIGITAL);

        assertThat(albums).hasSize(1);
        assertThat(albums.getFirst().getTitle()).isEqualTo("OK Computer");
    }

    @Test
    void shouldReturnAllActiveAlbums() {
        var artist = new Artist("Radiohead", UUID.randomUUID());
        var album = new Album("OK Computer", UUID.randomUUID(), artist);
        album.addFormat(AlbumFormat.DIGITAL);
        given(albumRepository.findAllActive()).willReturn(List.of(album));

        var albums = collectionService.findAllActiveAlbums();

        assertThat(albums).hasSize(1);
        assertThat(albums.getFirst().getTitle()).isEqualTo("OK Computer");
    }

    @Test
    void shouldMergeNewMusicBrainzIdIntoExistingAlbumByArtistAndTitle() {
        var artistMbid = UUID.randomUUID();
        var albumMbid = UUID.randomUUID();
        var existingArtist = new Artist("Some Artist");
        var existingAlbum = new Album("Some Album", existingArtist, null);
        existingAlbum.addFormat(AlbumFormat.DIGITAL);

        given(albumRepository.findByMusicBrainzId(albumMbid)).willReturn(Optional.empty());
        given(artistRepository.findByMusicBrainzId(artistMbid)).willReturn(Optional.of(existingArtist));
        given(albumRepository.findByTitleAndArtistNameIgnoreCase("Some Album", "Some Artist"))
                .willReturn(Optional.of(existingAlbum));
        given(albumRepository.save(existingAlbum)).willReturn(existingAlbum);

        var album = collectionService.importAlbum("Some Artist", artistMbid, "Some Album", albumMbid);

        assertThat(album).isEqualTo(existingAlbum);
        assertThat(album.getMusicBrainzId()).isEqualTo(albumMbid);
        then(eventPublisher).should().publishEvent(any(AlbumDiscoveredEvent.class));
    }

    @Test
    void shouldImportAlbumWithoutMbidWhenDuplicateArtistsExist() {
        var artistWithoutMbid = new Artist("Some Artist");
        var artistWithMbid = new Artist("Some Artist", UUID.randomUUID());
        given(albumRepository.findByTitleAndArtistNameIgnoreCase("Some Album", "Some Artist"))
                .willReturn(Optional.empty());
        given(artistRepository.findByNameIgnoreCase("Some Artist"))
                .willReturn(List.of(artistWithoutMbid, artistWithMbid));
        given(albumRepository.save(any(Album.class))).willAnswer(invocation -> invocation.getArgument(0));

        var album = collectionService.importAlbum("Some Artist", null, "Some Album", null);

        assertThat(album.getTitle()).isEqualTo("Some Album");
        assertThat(album.getArtist().getName()).isEqualTo("Some Artist");
        assertThat(album.getArtist()).isEqualTo(artistWithMbid);
    }

    // --- Discogs scan tests ---

    @Test
    void shouldImportVinylAlbumsFromDiscogs() {
        var releases = List.of(new DiscogsRelease(12345L, "Radiohead", "OK Computer", null));
        given(discogsClient.fetchCollection("testuser")).willReturn(releases);
        given(albumRepository.findByDiscogsId(12345L)).willReturn(Optional.empty());
        given(albumRepository.findByTitleAndArtistNameIgnoreCase("OK Computer", "Radiohead"))
                .willReturn(Optional.empty());
        given(musicBrainzSearchClient.searchReleaseGroup("Radiohead", "OK Computer"))
                .willReturn(Optional.empty());
        given(artistRepository.findByNameIgnoreCase("Radiohead")).willReturn(List.of());
        given(artistRepository.save(any(Artist.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(albumRepository.save(any(Album.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(albumRepository.findByFormat(AlbumFormat.VINYL)).willReturn(List.of());

        var albums = collectionService.scanDiscogs("testuser");

        assertThat(albums).hasSize(1);
        assertThat(albums.getFirst().getTitle()).isEqualTo("OK Computer");
        assertThat(albums.getFirst().getFormats()).containsExactly(AlbumFormat.VINYL);
    }

    @Test
    void shouldMatchExistingDigitalAlbumAndAddVinylFormat() {
        var artist = new Artist("Radiohead", UUID.randomUUID());
        var existingAlbum = new Album("OK Computer", UUID.randomUUID(), artist);
        existingAlbum.addFormat(AlbumFormat.DIGITAL);

        var releases = List.of(new DiscogsRelease(12345L, "Radiohead", "OK Computer", null));
        given(discogsClient.fetchCollection("testuser")).willReturn(releases);
        given(albumRepository.findByDiscogsId(12345L)).willReturn(Optional.empty());
        given(albumRepository.findByTitleAndArtistNameIgnoreCase("OK Computer", "Radiohead"))
                .willReturn(Optional.of(existingAlbum));
        given(albumRepository.save(existingAlbum)).willReturn(existingAlbum);
        given(albumRepository.findByFormat(AlbumFormat.VINYL)).willReturn(List.of());

        var albums = collectionService.scanDiscogs("testuser");

        assertThat(albums).hasSize(1);
        assertThat(albums.getFirst().getFormats()).containsExactlyInAnyOrder(AlbumFormat.DIGITAL, AlbumFormat.VINYL);
        assertThat(albums.getFirst().getDiscogsId()).isEqualTo(12345L);
    }

    @Test
    void shouldSearchMusicBrainzForUnmatchedDiscogsRelease() {
        var mbid = UUID.randomUUID();
        var releases = List.of(new DiscogsRelease(12345L, "Radiohead", "OK Computer", null));
        given(discogsClient.fetchCollection("testuser")).willReturn(releases);
        given(albumRepository.findByDiscogsId(12345L)).willReturn(Optional.empty());
        given(albumRepository.findByTitleAndArtistNameIgnoreCase("OK Computer", "Radiohead"))
                .willReturn(Optional.empty());
        given(musicBrainzSearchClient.searchReleaseGroup("Radiohead", "OK Computer"))
                .willReturn(Optional.of(mbid));
        given(albumRepository.findByMusicBrainzId(mbid)).willReturn(Optional.empty());
        given(artistRepository.findByNameIgnoreCase("Radiohead")).willReturn(List.of());
        given(artistRepository.save(any(Artist.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(albumRepository.save(any(Album.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(albumRepository.findByFormat(AlbumFormat.VINYL)).willReturn(List.of());

        var albums = collectionService.scanDiscogs("testuser");

        assertThat(albums).hasSize(1);
        assertThat(albums.getFirst().getMusicBrainzId()).isEqualTo(mbid);
        assertThat(albums.getFirst().getFormats()).containsExactly(AlbumFormat.VINYL);
        then(eventPublisher).should().publishEvent(any(AlbumDiscoveredEvent.class));
    }

    @Test
    void shouldPublishReleaseDateEventWhenDiscogsYearAvailableAndNoMbid() {
        var releases = List.of(new DiscogsRelease(12345L, "Radiohead", "OK Computer", 1997));
        given(discogsClient.fetchCollection("testuser")).willReturn(releases);
        given(albumRepository.findByDiscogsId(12345L)).willReturn(Optional.empty());
        given(albumRepository.findByTitleAndArtistNameIgnoreCase("OK Computer", "Radiohead"))
                .willReturn(Optional.empty());
        given(musicBrainzSearchClient.searchReleaseGroup("Radiohead", "OK Computer"))
                .willReturn(Optional.empty());
        given(artistRepository.findByNameIgnoreCase("Radiohead")).willReturn(List.of());
        given(artistRepository.save(any(Artist.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(albumRepository.save(any(Album.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(albumRepository.findByFormat(AlbumFormat.VINYL)).willReturn(List.of());

        var albums = collectionService.scanDiscogs("testuser");

        assertThat(albums).hasSize(1);
        assertThat(albums.getFirst().getReleaseDate()).isEqualTo(LocalDate.of(1997, 1, 1));
        then(eventPublisher).should().publishEvent(any(AlbumReleaseDateResolvedEvent.class));
    }

    @Test
    void shouldNotPublishEventWhenNoYearAndNoMbid() {
        var releases = List.of(new DiscogsRelease(12345L, "Radiohead", "OK Computer", null));
        given(discogsClient.fetchCollection("testuser")).willReturn(releases);
        given(albumRepository.findByDiscogsId(12345L)).willReturn(Optional.empty());
        given(albumRepository.findByTitleAndArtistNameIgnoreCase("OK Computer", "Radiohead"))
                .willReturn(Optional.empty());
        given(musicBrainzSearchClient.searchReleaseGroup("Radiohead", "OK Computer"))
                .willReturn(Optional.empty());
        given(artistRepository.findByNameIgnoreCase("Radiohead")).willReturn(List.of());
        given(artistRepository.save(any(Artist.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(albumRepository.save(any(Album.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(albumRepository.findByFormat(AlbumFormat.VINYL)).willReturn(List.of());

        var albums = collectionService.scanDiscogs("testuser");

        assertThat(albums).hasSize(1);
        assertThat(albums.getFirst().getReleaseDate()).isNull();
        then(eventPublisher).should(never()).publishEvent(any(AlbumReleaseDateResolvedEvent.class));
    }

    @Test
    void shouldReconcileVinylFormatsOnDiscogsScan() {
        given(discogsClient.fetchCollection("testuser")).willReturn(List.of());

        // Album with VINYL format that is NOT in the Discogs scan
        var artist = new Artist("Portishead", UUID.randomUUID());
        var removedAlbum = new Album("Dummy", artist, 99999L);
        removedAlbum.addFormat(AlbumFormat.VINYL);
        given(albumRepository.findByFormat(AlbumFormat.VINYL)).willReturn(List.of(removedAlbum));
        given(albumRepository.save(removedAlbum)).willReturn(removedAlbum);

        collectionService.scanDiscogs("testuser");

        assertThat(removedAlbum.hasFormat(AlbumFormat.VINYL)).isFalse();
    }

    @Test
    void shouldThrowWhenDiscogsNotConfigured() {
        var noDiscogsService = new CollectionService(fileScanner, tagReader, artistRepository,
                albumRepository, trackRepository, eventPublisher, null, musicBrainzSearchClient,
                null, new TransactionTemplate());

        assertThatThrownBy(() -> noDiscogsService.scanDiscogs("testuser"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Discogs is not configured");
    }

    // --- Volumio tests ---

    @Test
    void shouldPassFolderPathToVolumioWhenAlbumFound() {
        var artist = new Artist("Radiohead", UUID.randomUUID());
        var album = new Album("OK Computer", UUID.randomUUID(), artist);
        album.setFolderPath("Radiohead/OK Computer");
        given(albumRepository.findByTitleAndArtistNameIgnoreCase("OK Computer", "Radiohead"))
                .willReturn(Optional.of(album));

        collectionService.playAlbumOnVolumio("OK Computer", "Radiohead");

        then(volumioClient).should().playAlbum("OK Computer", "Radiohead", "Radiohead/OK Computer");
    }

    @Test
    void shouldPassNullFolderPathWhenAlbumNotFound() {
        given(albumRepository.findByTitleAndArtistNameIgnoreCase("OK Computer", "Radiohead"))
                .willReturn(Optional.empty());

        collectionService.playAlbumOnVolumio("OK Computer", "Radiohead");

        then(volumioClient).should().playAlbum("OK Computer", "Radiohead", null);
    }

    @Test
    void shouldThrowWhenVolumioNotConfigured() {
        var noVolumioService = new CollectionService(fileScanner, tagReader, artistRepository,
                albumRepository, trackRepository, eventPublisher, null, musicBrainzSearchClient,
                null, new TransactionTemplate());

        assertThatThrownBy(() -> noVolumioService.playAlbumOnVolumio("OK Computer", "Radiohead"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Volumio is not configured");
    }
}
