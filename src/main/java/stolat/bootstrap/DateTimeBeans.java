package stolat.bootstrap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class DateTimeBeans {

    @Bean
    public Clock getClock() {
        return Clock.systemDefaultZone();
    }
}
