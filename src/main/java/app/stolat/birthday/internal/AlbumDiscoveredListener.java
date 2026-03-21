package app.stolat.birthday.internal;

import app.stolat.birthday.BirthdayService;
import app.stolat.collection.AlbumDiscoveredEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class AlbumDiscoveredListener {

    private final BirthdayService birthdayService;

    AlbumDiscoveredListener(BirthdayService birthdayService) {
        this.birthdayService = birthdayService;
    }

    @EventListener
    void onAlbumDiscovered(AlbumDiscoveredEvent event) {
        birthdayService.resolveReleaseDate(event.albumTitle(), event.artistName(), event.musicBrainzId());
    }
}
