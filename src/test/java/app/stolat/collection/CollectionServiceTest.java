package app.stolat.collection;

import app.stolat.collection.internal.AlbumRepository;
import app.stolat.collection.internal.ArtistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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
    private ArtistRepository artistRepository;

    @Mock
    private AlbumRepository albumRepository;

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
}
