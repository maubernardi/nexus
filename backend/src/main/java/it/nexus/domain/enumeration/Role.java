package it.nexus.domain.enumeration;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

/**
 * Ruoli applicativi NEXUS. Corrispondono ai realm role Keycloak omonimi. L'ordine di dichiarazione è anche la
 * priorità crescente: se il token ne riporta più d'uno, prevale il più alto ({@link #highest}).
 */
public enum Role {
    TUTOR,
    CALL_CENTER,
    ADMIN;

    public static final String AUTHORITY_PREFIX = "ROLE_";

    public String authority() {
        return AUTHORITY_PREFIX + name();
    }

    /** Ruolo di priorità più alta tra quelli indicati, vuoto se non ce ne sono. */
    public static Optional<Role> highest(Collection<Role> roles) {
        return roles.stream().max(Comparator.naturalOrder());
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
