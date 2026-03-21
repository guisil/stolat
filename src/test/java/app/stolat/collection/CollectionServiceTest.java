package app.stolat.collection;

import app.stolat.collection.internal.AlbumRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CollectionServiceTest {

    @Mock
    private AlbumRepository albumRepository;

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
}
