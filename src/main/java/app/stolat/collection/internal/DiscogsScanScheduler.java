package app.stolat.collection.internal;

import app.stolat.collection.CollectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty("stolat.discogs.username")
class DiscogsScanScheduler {

    private final CollectionService collectionService;
    private final String username;

    DiscogsScanScheduler(CollectionService collectionService,
                         @Value("${stolat.discogs.username}") String username) {
        this.collectionService = collectionService;
        this.username = username;
    }

    @Scheduled(cron = "${stolat.discogs.scan-cron:0 0 4 * * *}")
    void scheduledDiscogsScan() {
        log.info("Running scheduled Discogs scan");
        var albums = collectionService.scanDiscogs(username);
        log.info("Scheduled Discogs scan complete: {} albums processed", albums.size());
    }
}
