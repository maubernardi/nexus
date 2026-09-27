package it.nexus.config;

import java.time.Instant;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import it.nexus.config.security.AuthenticatedUser;
import it.nexus.config.security.SecurityUtils;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider", dateTimeProviderRef = "auditingDateTimeProvider")
public class PersistenceConfig {

    /** Autore registrato per le operazioni senza utente autenticato (job schedulati, migrazioni applicative). */
    public static final String SYSTEM_AUDITOR = "system";

    @Bean
    AuditorAware<String> auditorProvider() {
        return () -> Optional.of(SecurityUtils.getCurrentUser().map(AuthenticatedUser::username).orElse(SYSTEM_AUDITOR));
    }

    @Bean
    DateTimeProvider auditingDateTimeProvider() {
        return () -> Optional.of(Instant.now());
    }
}
