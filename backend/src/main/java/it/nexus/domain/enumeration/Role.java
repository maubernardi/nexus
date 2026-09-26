package it.nexus.domain.enumeration;

import java.util.Optional;

/**
 * Ruoli applicativi NEXUS. Corrispondono ai realm role Keycloak omonimi.
 */
public enum Role {
    TUTOR,
    CALL_CENTER,
    ADMIN;

    public static final String AUTHORITY_PREFIX = "ROLE_";

    public String authority() {
        return AUTHORITY_PREFIX + name();
    }

    /** Converte un nome ruolo (con o senza prefisso {@code ROLE_}) ignorando i ruoli sconosciuti. */
    public static Optional<Role> fromName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        String normalized = name.startsWith(AUTHORITY_PREFIX) ? name.substring(AUTHORITY_PREFIX.length()) : name;
        for (Role role : values()) {
            if (role.name().equals(normalized)) {
                return Optional.of(role);
            }
        }
        return Optional.empty();
    }
}
