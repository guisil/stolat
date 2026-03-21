package app.stolat.internal;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import app.stolat.collection.CollectionService;
import app.stolat.birthday.BirthdayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
class DevDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);

    private final CollectionService collectionService;
    private final BirthdayService birthdayService;

    DevDataInitializer(CollectionService collectionService, BirthdayService birthdayService) {
        this.collectionService = collectionService;
        this.birthdayService = birthdayService;
    }

    @EventListener(ApplicationReadyEvent.class)
    void initializeDevData() {
        if (!collectionService.findAllAlbums().isEmpty()) {
            log.info("Dev data already exists, skipping initialization");
            return;
        }

        log.info("Seeding dev data...");
        seedAlbumWithBirthday("Radiohead", "OK Computer", LocalDate.of(1997, 6, 16));
        seedAlbumWithBirthday("Radiohead", "Kid A", LocalDate.of(2000, 10, 2));
        seedAlbumWithBirthday("Portishead", "Dummy", LocalDate.of(1994, 8, 22));
        seedAlbumWithBirthday("Portishead", "Third", LocalDate.of(2008, 4, 28));
        seedAlbumWithBirthday("Massive Attack", "Mezzanine", LocalDate.of(1998, 4, 20));
        seedAlbumWithBirthday("Björk", "Homogenic", LocalDate.of(1997, 9, 22));
        seedAlbumWithBirthday("Boards of Canada", "Music Has the Right to Children", LocalDate.of(1998, 4, 20));
        seedAlbumWithBirthday("Aphex Twin", "Selected Ambient Works 85-92", LocalDate.of(1992, 11, 9));

        // Seed a birthday for today so the daily digest has something to show
        seedAlbumWithBirthday("Test Artist", "Today's Birthday Album",
                LocalDate.now().withYear(2000));

        log.info("Dev data seeded: {} albums", collectionService.findAllAlbums().size());
    }

    private void seedAlbumWithBirthday(String artistName, String albumTitle, LocalDate releaseDate) {
        var artistMbid = UUID.randomUUID();
        var albumMbid = UUID.randomUUID();
        collectionService.importAlbum(artistName, artistMbid, albumTitle, albumMbid);
        birthdayService.resolveReleaseDateDirect(albumTitle, artistName, albumMbid, releaseDate);
    }
}
