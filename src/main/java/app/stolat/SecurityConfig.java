package app.stolat;

import static com.vaadin.flow.spring.security.VaadinSecurityConfigurer.vaadin;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.with(vaadin(), vaadin -> {
            vaadin.loginView(LoginView.class);
        });

        return http.build();
    }

    // TODO: Replace with DB-backed authentication
    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        var user = User.withUsername("user")
                .password("{noop}stolat")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}
