package it.nexus.config.security;

import java.util.List;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

import it.nexus.domain.enumeration.Role;

/**
 * Utenti fittizi per il profilo {@code security-mock}.
 */
@ConfigurationProperties(prefix = "nexus.security.mock")
public record MockSecurityProperties(List<MockUser> users) {

    public MockSecurityProperties {
        users = users == null ? List.of() : List.copyOf(users);
    }

    /** {@code id} è il soggetto dell'utente (come il {@code sub} Keycloak); l'header {@code X-USER-ID} usa lo username. */
    public record MockUser(String id, String username, String firstName, String lastName, String email, Set<Role> roles) {

        public AuthenticatedUser toAuthenticatedUser() {
            return new AuthenticatedUser(id, username, firstName, lastName, email, roles == null ? Set.of() : roles);
        }
    }
}
