package app.stolat.collection.internal;

import app.stolat.TestcontainersConfiguration;
import app.stolat.collection.Album;
import app.stolat.collection.Artist;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class AlbumRepositoryTest {

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Test
    void shouldPersistAndRetrieveAlbum() {
        var artist = artistRepository.save(new Artist("Radiohead", UUID.randomUUID()));

        var album = new Album("OK Computer", UUID.randomUUID(), artist);
        var saved = albumRepository.save(album);
        var found = albumRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("OK Computer");
        assertThat(found.get().getMusicBrainzId()).isNotNull();
        assertThat(found.get().getArtist()).isEqualTo(artist);
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getUpdatedAt()).isNotNull();
    }
}
