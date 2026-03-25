package app.stolat.birthday.internal;

import java.time.Duration;
import java.time.Instant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@ConditionalOnProperty("stolat.lastfm.api-key")
class LastFmSyncService {

    private static final Duration RATE_LIMIT_DELAY = Duration.ofMillis(200);
    private static final Duration STALE_THRESHOLD = Duration.ofDays(7);

    private final AlbumBirthdayRepository albumBirthdayRepository;
    private final LastFmClient lastFmClient;

    LastFmSyncService(AlbumBirthdayRepository albumBirthdayRepository,
                      LastFmClient lastFmClient) {
        this.albumBirthdayRepository = albumBirthdayRepository;
        this.lastFmClient = lastFmClient;
    }

    int syncAllPlayCounts() {
        var birthdays = albumBirthdayRepository.findAll();
        var now = Instant.now();
        int updated = 0;

        for (var birthday : birthdays) {
            if (birthday.getPlayCountUpdatedAt() != null
                    && Duration.between(birthday.getPlayCountUpdatedAt(), now).compareTo(STALE_THRESHOLD) < 0) {
                continue;
            }

            var playCount = lastFmClient.fetchAlbumPlayCount(
                    birthday.getArtistName(), birthday.getAlbumTitle());
            if (playCount.isPresent()) {
                birthday.updatePlayCount(playCount.getAsInt());
                albumBirthdayRepository.save(birthday);
                updated++;
            }

            try {
                Thread.sleep(RATE_LIMIT_DELAY.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Play count sync interrupted after updating {} albums", updated);
                break;
            }
        }

        log.info("Synced play counts for {} albums", updated);
        return updated;
    }
}
