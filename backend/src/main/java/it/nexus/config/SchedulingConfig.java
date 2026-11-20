package it.nexus.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Attività pianificate (controllo orario del timer delle proposte, US-704/705). Spente con
 * {@code nexus.scheduling.enabled=false}, come nell'istanza dell'importazione dal database attuale (US-1309).
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "nexus.scheduling", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SchedulingConfig {
}
