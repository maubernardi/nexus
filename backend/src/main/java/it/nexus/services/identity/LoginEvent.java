package it.nexus.services.identity;

import java.time.Instant;

/**
 * Un accesso dal registro degli eventi di Keycloak (US-113).
 *
 * @param userExternalId soggetto dell'account; {@code null} se lo username scritto non esiste
 * @param username       username o email scritti nel modulo di accesso; può mancare
 * @param error          codice di Keycloak del tentativo fallito (es. {@code invalid_user_credentials}), {@code null} se
 *                       l'accesso è riuscito
 */
public record LoginEvent(Instant at, boolean success, String userExternalId, String username, String ipAddress,
        String error) {

    @Override
    public String toString() {
        // niente username né indirizzo IP nei log
        return "LoginEvent[at=" + at + ", success=" + success + ", error=" + error + "]";
    }
}
