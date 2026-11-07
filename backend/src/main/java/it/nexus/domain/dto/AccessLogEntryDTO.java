package it.nexus.domain.dto;

import java.time.Instant;

/**
 * Un accesso a NEXUS (US-113): quando, da quale indirizzo IP e con quale esito.
 *
 * @param username username o email scritti nel modulo di accesso
 * @param userName nome e cognome dell'utente NEXUS, se lo username esiste
 * @param error    motivo del tentativo fallito, codice di Keycloak (es. {@code invalid_user_credentials})
 */
public record AccessLogEntryDTO(Instant at, boolean success, String ipAddress, String username, String userName,
        String error) {
}
