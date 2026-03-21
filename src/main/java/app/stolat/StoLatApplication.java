package app.stolat;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@Push
@Theme("stolat")
public class StoLatApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(StoLatApplication.class, args);
    }
}
