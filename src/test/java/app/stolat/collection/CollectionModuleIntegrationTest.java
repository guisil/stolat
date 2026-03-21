package app.stolat.collection;

import app.stolat.TestcontainersConfiguration;
import app.stolat.collection.internal.AlbumRepository;
import app.stolat.collection.internal.ArtistRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ApplicationModuleTest
@Import(TestcontainersConfiguration.class)
@RecordApplicationEvents
@Transactional
class CollectionModuleIntegrationTest {

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private ApplicationEvents events;

    @Test
    void shouldImportAlbumAndPublishEvent() {
        var artistMbid = UUID.randomUUID();
        var albumMbid = UUID.randomUUID();

        var album = collectionService.importAlbum("Radiohead", artistMbid, "OK Computer", albumMbid);

        assertThat(albumRepository.findById(album.getId())).isPresent();
        assertThat(artistRepository.findByMusicBrainzId(artistMbid)).isPresent();
        assertThat(events.stream(AlbumDiscoveredEvent.class))
                .hasSize(1)
                .first()
                .satisfies(event -> {
                    assertThat(event.albumId()).isEqualTo(album.getId());
                    assertThat(event.musicBrainzId()).isEqualTo(albumMbid);
                });
    }

    @Test
    void shouldReuseExistingArtistOnSecondImport() {
        var artistMbid = UUID.randomUUID();

        collectionService.importAlbum("Radiohead", artistMbid, "OK Computer", UUID.randomUUID());
        collectionService.importAlbum("Radiohead", artistMbid, "Kid A", UUID.randomUUID());

        assertThat(artistRepository.findAll()).hasSize(1);
        assertThat(albumRepository.findAll()).hasSize(2);
    }
}
