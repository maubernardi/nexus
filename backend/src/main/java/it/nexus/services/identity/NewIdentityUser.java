package it.nexus.services.identity;

import it.nexus.domain.enumeration.Role;

/** Dati di un nuovo account nell'identity provider (US-103). {@code toString()} senza dati personali. */
public record NewIdentityUser(String username, String firstName, String lastName, String email, Role role) {

    @Override
    public String toString() {
        return "NewIdentityUser[role=" + role + "]";
    }
}
