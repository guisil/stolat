package app.stolat.collection.internal;

import app.stolat.TestcontainersConfiguration;
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
class ArtistRepositoryTest {

    @Autowired
    private ArtistRepository artistRepository;

    @Test
    void shouldPersistAndRetrieveArtist() {
        var artist = new Artist("Radiohead");

        var saved = artistRepository.save(artist);
        var found = artistRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Radiohead");
        assertThat(found.get().getId()).isNotNull();
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldPersistAndRetrieveArtistWithMusicBrainzId() {
        var mbid = UUID.randomUUID();
        var artist = new Artist("Radiohead", mbid);

        var saved = artistRepository.save(artist);
        var found = artistRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Radiohead");
        assertThat(found.get().getMusicBrainzId()).isEqualTo(mbid);
    }
}
