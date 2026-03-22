package app.stolat.birthday.internal;

import app.stolat.birthday.BirthdayService;
import app.stolat.collection.AlbumDiscoveredEvent;
import app.stolat.collection.CollectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class AlbumDiscoveredListener {

    private final BirthdayService birthdayService;
    private final CollectionService collectionService;

    AlbumDiscoveredListener(BirthdayService birthdayService, CollectionService collectionService) {
        this.birthdayService = birthdayService;
        this.collectionService = collectionService;
    }

    @Async
    @EventListener
    void onAlbumDiscovered(AlbumDiscoveredEvent event) {
        log.info("Looking up release date for '{}' by '{}'", event.albumTitle(), event.artistName());
        birthdayService.resolveReleaseDate(event.albumId(), event.albumTitle(),
                        event.artistName(), event.musicBrainzId())
                .ifPresent(birthday -> {
                    log.info("Found release date {} for '{}' by '{}'",
                            birthday.getReleaseDate(), event.albumTitle(), event.artistName());
                    collectionService.updateAlbumReleaseDate(
                            event.musicBrainzId(), birthday.getReleaseDate());
                });
    }
}
