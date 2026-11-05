package it.nexus.domain.dto;

import java.time.Instant;

/**
 * Indirizzo email dell'utente corrente in NEXUS e, se c'è, il cambio in attesa del codice (US-110).
 *
 * @param pending assente se non c'è un cambio in corso
 */
public record EmailStatusDTO(String email, Pending pending) {

    /**
     * @param expiresAt          fino a quando vale il codice inviato
     * @param resendAvailableAt  da quando si può chiedere un altro codice
     * @param remainingAttempts  tentativi rimasti per il codice attuale
     */
    public record Pending(String newEmail, Instant expiresAt, Instant resendAvailableAt, int remainingAttempts) {
    }
}
