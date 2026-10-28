package it.nexus.config;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Orologio dell'applicazione: le date di calendario (giorni lavorativi, scadenze) sono quelle di Roma. */
@Configuration
public class TimeConfig {

    public static final ZoneId ZONE = ZoneId.of("Europe/Rome");

    /** Ultimo giorno utile di una scadenza fissata alla mezzanotte di Roma (es. timer della proposta). */
    public static LocalDate lastDay(Instant deadline) {
        return LocalDate.ofInstant(deadline.minusSeconds(1), ZONE);
    }

    @Bean
    Clock clock() {
        return Clock.system(ZONE);
    }
}
