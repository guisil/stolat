package app.stolat.notification.internal;

import java.time.LocalDate;

import app.stolat.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class NotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);

    private final NotificationService notificationService;
    private final boolean sendOnStartup;

    NotificationScheduler(NotificationService notificationService,
                          @Value("${stolat.notification.send-on-startup:false}") boolean sendOnStartup) {
        this.notificationService = notificationService;
        this.sendOnStartup = sendOnStartup;
    }

    @Scheduled(cron = "${stolat.notification.cron:0 0 8 * * *}")
    void sendDailyNotification() {
        log.info("Running daily birthday notification");
        notificationService.sendDailyDigest(LocalDate.now());
    }

    @Order(2)
    @EventListener(ApplicationReadyEvent.class)
    void sendOnStartup() {
        if (sendOnStartup) {
            log.info("Sending birthday notification on startup");
            notificationService.sendDailyDigest(LocalDate.now());
        }
    }
}
