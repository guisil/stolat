package app.stolat.collection.internal;

import java.nio.file.Path;

import app.stolat.collection.CollectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class CollectionScanScheduler {

    private final CollectionService collectionService;
    private final String musicDirectory;

    CollectionScanScheduler(CollectionService collectionService,
                            @Value("${stolat.collection.music-directory}") String musicDirectory) {
        this.collectionService = collectionService;
        this.musicDirectory = musicDirectory;
    }

    @Scheduled(cron = "${stolat.collection.scan-cron:0 0 3 * * *}")
    void scheduledScan() {
        log.info("Running scheduled collection scan");
        var albums = collectionService.scanDirectory(Path.of(musicDirectory));
        log.info("Scheduled scan complete: {} albums processed", albums.size());
    }
}
