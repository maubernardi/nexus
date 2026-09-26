package it.nexus.config.security;

import java.util.Set;

import it.nexus.domain.enumeration.Role;

/**
 * Principal applicativo, indipendente dalla modalità di autenticazione (JWT Keycloak o mock).
 */
public record AuthenticatedUser(
        String id,
        String username,
        String firstName,
        String lastName,
        String email,
        Set<Role> roles) {

    public AuthenticatedUser {
        roles = Set.copyOf(roles);
    }
}
