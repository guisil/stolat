package app.stolat.birthday.internal;

import app.stolat.birthday.BirthdayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty("stolat.lastfm.api-key")
class LastFmSyncScheduler {

    private final BirthdayService birthdayService;

    LastFmSyncScheduler(BirthdayService birthdayService) {
        this.birthdayService = birthdayService;
    }

    @Scheduled(cron = "${stolat.lastfm.sync-cron:0 0 6 * * *}")
    void scheduledSync() {
        log.info("Running scheduled Last.fm play count sync");
        var synced = birthdayService.syncPlayCounts();
        log.info("Scheduled sync complete: {} play counts updated", synced);
    }
}
