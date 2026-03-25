package app.stolat.birthday.internal;

import app.stolat.birthday.BirthdayService;
import app.stolat.birthday.ReleaseDateSource;
import app.stolat.collection.AlbumReleaseDateResolvedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class AlbumReleaseDateResolvedListener {

    private final BirthdayService birthdayService;

    AlbumReleaseDateResolvedListener(BirthdayService birthdayService) {
        this.birthdayService = birthdayService;
    }

    @Async
    @EventListener
    void onAlbumReleaseDateResolved(AlbumReleaseDateResolvedEvent event) {
        log.info("Storing release date {} for '{}' by '{}' (from external source, discogsId={})",
                event.releaseDate(), event.albumTitle(), event.artistName(), event.discogsId());
        birthdayService.resolveReleaseDateForAlbum(event.albumId(), event.albumTitle(),
                event.artistName(), event.releaseDate(), ReleaseDateSource.DISCOGS, event.discogsId());
    }
}
