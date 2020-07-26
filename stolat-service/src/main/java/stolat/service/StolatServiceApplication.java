package stolat.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"stolat"})
@Slf4j
public class StolatServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StolatServiceApplication.class, args);
    }
}
