package it.nexus.domain.dto;

import java.time.Instant;
import java.time.LocalDate;

import it.nexus.domain.enumeration.ContactOutcome;

/** Tentativo di contatto (US-512), anche per il Tutor: data, esito, nota e chi l'ha segnato. */
public record ContactAttemptDTO(
        String id,
        LocalDate date,
        ContactOutcome outcome,
        String note,
        /** Nome e cognome dell'operatore. */
        String operatorName,
        Instant createdAt) {
}
