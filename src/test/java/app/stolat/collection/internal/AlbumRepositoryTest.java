package app.stolat.collection.internal;

import app.stolat.TestcontainersConfiguration;
import app.stolat.collection.Album;
import app.stolat.collection.AlbumFormat;
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

    @Test
    void shouldPersistAndRetrieveAlbumWithFormats() {
        var artist = artistRepository.save(new Artist("Radiohead", UUID.randomUUID()));
        var album = new Album("OK Computer", UUID.randomUUID(), artist);
        album.addFormat(AlbumFormat.DIGITAL);
        albumRepository.save(album);

        albumRepository.flush();
        var found = albumRepository.findById(album.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getFormats()).containsExactly(AlbumFormat.DIGITAL);
    }

    @Test
    void shouldFindByDiscogsId() {
        var artist = artistRepository.save(new Artist("Radiohead", UUID.randomUUID()));
        var album = new Album("OK Computer", artist, 12345L);
        albumRepository.save(album);

        var found = albumRepository.findByDiscogsId(12345L);

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("OK Computer");
        assertThat(found.get().getDiscogsId()).isEqualTo(12345L);
    }

    @Test
    void shouldFindAllActiveAlbums() {
        var artist = artistRepository.save(new Artist("Radiohead", UUID.randomUUID()));

        var activeAlbum = new Album("OK Computer", UUID.randomUUID(), artist);
        activeAlbum.addFormat(AlbumFormat.DIGITAL);
        albumRepository.save(activeAlbum);

        var inactiveAlbum = new Album("Kid A", UUID.randomUUID(), artist);
        albumRepository.save(inactiveAlbum);

        albumRepository.flush();
        var active = albumRepository.findAllActive();

        assertThat(active).hasSize(1);
        assertThat(active.getFirst().getTitle()).isEqualTo("OK Computer");
    }

    @Test
    void shouldFindByFormat() {
        var artist = artistRepository.save(new Artist("Radiohead", UUID.randomUUID()));

        var digitalAlbum = new Album("OK Computer", UUID.randomUUID(), artist);
        digitalAlbum.addFormat(AlbumFormat.DIGITAL);
        albumRepository.save(digitalAlbum);

        var vinylAlbum = new Album("Kid A", UUID.randomUUID(), artist);
        vinylAlbum.addFormat(AlbumFormat.VINYL);
        albumRepository.save(vinylAlbum);

        albumRepository.flush();
        var digitalAlbums = albumRepository.findByFormat(AlbumFormat.DIGITAL);
        var vinylAlbums = albumRepository.findByFormat(AlbumFormat.VINYL);

        assertThat(digitalAlbums).hasSize(1);
        assertThat(digitalAlbums.getFirst().getTitle()).isEqualTo("OK Computer");
        assertThat(vinylAlbums).hasSize(1);
        assertThat(vinylAlbums.getFirst().getTitle()).isEqualTo("Kid A");
    }

    @Test
    void shouldFindByTitleAndArtistNameIgnoreCase() {
        var artist = artistRepository.save(new Artist("Radiohead", UUID.randomUUID()));
        albumRepository.save(new Album("OK Computer", UUID.randomUUID(), artist));

        var found = albumRepository.findByTitleAndArtistNameIgnoreCase("ok computer", "radiohead");

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("OK Computer");
    }
}
