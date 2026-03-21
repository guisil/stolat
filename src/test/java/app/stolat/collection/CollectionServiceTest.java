package app.stolat.collection;

import app.stolat.collection.internal.AlbumRepository;
import app.stolat.collection.internal.ArtistRepository;
import app.stolat.collection.internal.AudioFileMetadata;
import app.stolat.collection.internal.FileScanner;
import app.stolat.collection.internal.TagReader;
import app.stolat.collection.internal.TrackRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

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

    @InjectMocks
    private CollectionService collectionService;

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
        var album = collectionService.importAlbum("Radiohead", artistMbid, "OK Computer", albumMbid, tracks);

        assertThat(album.getTitle()).isEqualTo("OK Computer");
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
                        "Airbag", 1, 1, UUID.randomUUID())));
        given(tagReader.read(track2Path)).willReturn(Optional.of(
                new AudioFileMetadata("Radiohead", artistMbid, "OK Computer", albumMbid,
                        "Paranoid Android", 2, 1, UUID.randomUUID())));
        given(artistRepository.findByMusicBrainzId(artistMbid)).willReturn(Optional.empty());
        given(artistRepository.save(any(Artist.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(albumRepository.save(any(Album.class))).willAnswer(invocation -> invocation.getArgument(0));

        var albums = collectionService.scanDirectory(rootDir);

        assertThat(albums).hasSize(1);
        assertThat(albums.getFirst().getTitle()).isEqualTo("OK Computer");
        then(trackRepository).should().saveAll(any());
        then(eventPublisher).should().publishEvent(any(AlbumDiscoveredEvent.class));
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
                        "Airbag", 1, 1, UUID.randomUUID())));
        given(tagReader.read(badFile)).willReturn(Optional.empty());
        given(artistRepository.findByMusicBrainzId(artistMbid)).willReturn(Optional.empty());
        given(artistRepository.save(any(Artist.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(albumRepository.save(any(Album.class))).willAnswer(invocation -> invocation.getArgument(0));

        var albums = collectionService.scanDirectory(rootDir);

        assertThat(albums).hasSize(1);
    }

    @Test
    void shouldSkipAlreadyImportedAlbum() {
        var artistMbid = UUID.randomUUID();
        var albumMbid = UUID.randomUUID();
        var existingArtist = new Artist("Radiohead", artistMbid);
        var existingAlbum = new Album("OK Computer", albumMbid, existingArtist);
        given(albumRepository.findByMusicBrainzId(albumMbid)).willReturn(Optional.of(existingAlbum));

        var album = collectionService.importAlbum("Radiohead", artistMbid, "OK Computer", albumMbid);

        assertThat(album).isEqualTo(existingAlbum);
        then(albumRepository).should().findByMusicBrainzId(albumMbid);
        then(albumRepository).shouldHaveNoMoreInteractions();
        then(eventPublisher).shouldHaveNoInteractions();
    }
}
