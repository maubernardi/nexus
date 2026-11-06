package it.nexus.config.security;

import java.time.Instant;
import java.util.Set;

import it.nexus.domain.enumeration.Role;

/**
 * Principal applicativo, indipendente dalla modalità di autenticazione (JWT Keycloak o mock). {@code issuedAt} è
 * l'emissione del token ({@code null} per gli utenti mock): un token più vecchio dell'ultima modifica dell'utente in
 * NEXUS non ne riallinea il ruolo (US-105).
 */
public record AuthenticatedUser(
        String id,
        String username,
        String firstName,
        String lastName,
        String email,
        Set<Role> roles,
        Instant issuedAt) {

    public AuthenticatedUser {
        roles = Set.copyOf(roles);
    }

    public AuthenticatedUser(String id, String username, String firstName, String lastName, String email,
            Set<Role> roles) {
        this(id, username, firstName, lastName, email, roles, null);
    }
}
