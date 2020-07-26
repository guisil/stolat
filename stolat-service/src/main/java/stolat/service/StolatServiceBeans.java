package stolat.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneOffset;

@Configuration
@ComponentScan(basePackages = "stolat")
public class StolatServiceBeans {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneOffset.UTC);
    }
}
