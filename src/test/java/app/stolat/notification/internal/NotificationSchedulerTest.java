package app.stolat.notification.internal;

import app.stolat.notification.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class NotificationSchedulerTest {

    @Mock
    private NotificationService notificationService;

    @Test
    void shouldSendDailyDigestForToday() {
        var scheduler = new NotificationScheduler(notificationService, false);
        scheduler.sendDailyNotification();

        then(notificationService).should().sendDailyDigest(LocalDate.now());
    }

    @Test
    void shouldSendOnStartupWhenEnabled() {
        var scheduler = new NotificationScheduler(notificationService, true);
        scheduler.sendOnStartup();

        then(notificationService).should().sendDailyDigest(LocalDate.now());
    }

    @Test
    void shouldNotSendOnStartupWhenDisabled() {
        var scheduler = new NotificationScheduler(notificationService, false);
        scheduler.sendOnStartup();

        then(notificationService).shouldHaveNoInteractions();
    }
}
