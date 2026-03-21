package app.stolat.birthday.internal;

import app.stolat.birthday.BirthdayService;
import app.stolat.collection.AlbumDiscoveredEvent;
import app.stolat.collection.CollectionService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class AlbumDiscoveredListener {

    private final BirthdayService birthdayService;
    private final CollectionService collectionService;

    AlbumDiscoveredListener(BirthdayService birthdayService, CollectionService collectionService) {
        this.birthdayService = birthdayService;
        this.collectionService = collectionService;
    }

    @EventListener
    void onAlbumDiscovered(AlbumDiscoveredEvent event) {
        birthdayService.resolveReleaseDate(event.albumTitle(), event.artistName(), event.musicBrainzId())
                .ifPresent(birthday -> collectionService.updateAlbumReleaseDate(
                        event.musicBrainzId(), birthday.getReleaseDate()));
    }
}
