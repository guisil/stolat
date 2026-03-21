package app.stolat.notification.internal;

import java.time.LocalDate;

import app.stolat.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class NotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);

    private final NotificationService notificationService;

    NotificationScheduler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "${stolat.notification.cron:0 0 8 * * *}")
    void sendDailyNotification() {
        log.info("Running daily birthday notification");
        notificationService.sendDailyDigest(LocalDate.now());
    }
}
