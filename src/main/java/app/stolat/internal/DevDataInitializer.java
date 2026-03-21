package app.stolat.internal;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import app.stolat.collection.AlbumFormat;
import app.stolat.collection.CollectionService;
import app.stolat.birthday.BirthdayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev")
class DevDataInitializer {

    private final CollectionService collectionService;
    private final BirthdayService birthdayService;

    DevDataInitializer(CollectionService collectionService, BirthdayService birthdayService) {
        this.collectionService = collectionService;
        this.birthdayService = birthdayService;
    }

    @Order(1)
    @EventListener(ApplicationReadyEvent.class)
    void initializeDevData() {
        if (!collectionService.findAllAlbums().isEmpty()) {
            log.info("Dev data already exists, skipping initialization");
            return;
        }

        log.info("Seeding dev data...");
        // Digital albums
        seedAlbumWithBirthday("Radiohead", "OK Computer", LocalDate.of(1997, 6, 16), AlbumFormat.DIGITAL);
        seedAlbumWithBirthday("Radiohead", "Kid A", LocalDate.of(2000, 10, 2), AlbumFormat.DIGITAL);
        seedAlbumWithBirthday("Portishead", "Dummy", LocalDate.of(1994, 8, 22), AlbumFormat.DIGITAL);
        seedAlbumWithBirthday("Portishead", "Third", LocalDate.of(2008, 4, 28), AlbumFormat.DIGITAL);
        seedAlbumWithBirthday("Massive Attack", "Mezzanine", LocalDate.of(1998, 4, 20), AlbumFormat.DIGITAL);
        seedAlbumWithBirthday("Boards of Canada", "Music Has the Right to Children",
                LocalDate.of(1998, 4, 20), AlbumFormat.DIGITAL);
        seedAlbumWithBirthday("Aphex Twin", "Selected Ambient Works 85-92",
                LocalDate.of(1992, 11, 9), AlbumFormat.DIGITAL);

        // Vinyl-only album
        seedAlbumWithBirthday("Pink Floyd", "The Dark Side of the Moon",
                LocalDate.of(1973, 3, 1), AlbumFormat.VINYL);

        // Album with both formats
        seedAlbumWithBirthday("Bjork", "Homogenic", LocalDate.of(1997, 9, 22), AlbumFormat.DIGITAL);
        // Add VINYL format to Homogenic (already imported as DIGITAL above)
        var bjorkAlbums = collectionService.findAllAlbums().stream()
                .filter(a -> a.getTitle().equals("Homogenic"))
                .findFirst();
        bjorkAlbums.ifPresent(album -> {
            collectionService.importAlbum("Bjork", album.getArtist().getMusicBrainzId(),
                    "Homogenic", album.getMusicBrainzId(), AlbumFormat.VINYL, List.of());
        });

        // Seed a birthday for today so the daily digest has something to show
        seedAlbumWithBirthday("Test Artist", "Today's Birthday Album",
                LocalDate.now().withYear(2000), AlbumFormat.DIGITAL);

        log.info("Dev data seeded: {} albums", collectionService.findAllAlbums().size());
    }

    private void seedAlbumWithBirthday(String artistName, String albumTitle, LocalDate releaseDate,
                                       AlbumFormat format) {
        var artistMbid = UUID.randomUUID();
        var albumMbid = UUID.randomUUID();
        birthdayService.resolveReleaseDateDirect(albumTitle, artistName, albumMbid, releaseDate);
        collectionService.importAlbum(artistName, artistMbid, albumTitle, albumMbid, format, List.of());
    }
}
