package it.nexus.domain.dto;

import java.time.LocalDate;

import it.nexus.domain.enumeration.ContactOutcome;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Tentativo di contatto (US-512): la data non è futura (controllo nel servizio); la nota è facoltativa. */
public record ContactAttemptFormDTO(
        @NotNull(message = "Indica la data del tentativo") LocalDate date,
        @NotNull(message = "Scegli l'esito") ContactOutcome outcome,
        @Size(max = 500, message = "Massimo 500 caratteri") String note) {
}
