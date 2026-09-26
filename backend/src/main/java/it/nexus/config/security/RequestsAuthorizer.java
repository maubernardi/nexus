package it.nexus.config.security;

import org.springframework.stereotype.Component;

import it.nexus.domain.enumeration.Role;

/**
 * Costanti di ruolo da referenziare in {@code @PreAuthorize}, es.
 * {@code @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")}. Evita stringhe sparse nel codice.
 */
@Component("requestsAuthorizer")
public class RequestsAuthorizer {

    public final String TUTOR = Role.TUTOR.name();
    public final String CALL_CENTER = Role.CALL_CENTER.name();
    public final String ADMIN = Role.ADMIN.name();

    /** Endpoint accessibili senza autenticazione. Tutto il resto è negato di default. */
    public static final String[] PUBLIC_PATHS = {
        "/actuator/health/**",
        "/actuator/info",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html"
    };
}
