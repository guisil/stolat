package app.stolat.collection.internal;

import app.stolat.TestcontainersConfiguration;
import app.stolat.collection.Album;
import app.stolat.collection.Artist;
import app.stolat.collection.Track;
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
class TrackRepositoryTest {

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Test
    void shouldPersistAndRetrieveTrack() {
        var artist = artistRepository.save(new Artist("Radiohead", UUID.randomUUID()));
        var album = albumRepository.save(new Album("OK Computer", UUID.randomUUID(), artist));

        var track = new Track("Paranoid Android", 2, 1, UUID.randomUUID(), album);
        var saved = trackRepository.save(track);
        var found = trackRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Paranoid Android");
        assertThat(found.get().getTrackNumber()).isEqualTo(2);
        assertThat(found.get().getDiscNumber()).isEqualTo(1);
        assertThat(found.get().getMusicBrainzId()).isNotNull();
        assertThat(found.get().getAlbum()).isEqualTo(album);
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getUpdatedAt()).isNotNull();
    }
}
