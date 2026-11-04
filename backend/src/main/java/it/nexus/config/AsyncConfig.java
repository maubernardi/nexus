package it.nexus.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** Attività in un thread a parte, fuori dalla richiesta: l'invio delle email delle notifiche (US-1202). */
@Configuration
@EnableAsync
public class AsyncConfig {
}
