package it.nexus.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Orologio dell'applicazione: le date di calendario (giorni lavorativi, scadenze) sono quelle di Roma. */
@Configuration
public class TimeConfig {

    public static final ZoneId ZONE = ZoneId.of("Europe/Rome");

    @Bean
    Clock clock() {
        return Clock.system(ZONE);
    }
}
