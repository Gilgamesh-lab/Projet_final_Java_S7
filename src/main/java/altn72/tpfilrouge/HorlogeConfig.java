package altn72.tpfilrouge;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class HorlogeConfig {

    // Injectée dans les services pour pouvoir fixer l'heure dans les tests
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
