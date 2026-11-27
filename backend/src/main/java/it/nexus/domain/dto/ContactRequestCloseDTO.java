package it.nexus.domain.dto;

import it.nexus.domain.enumeration.ContactOutcome;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Chiusura della richiesta di contatto (US-512): esito finale e nota per il Tutor, obbligatori; l'azienda attiva
 * dell'anagrafica, se c'è.
 */
public record ContactRequestCloseDTO(
        @NotNull(message = "Versione mancante") @PositiveOrZero Long version,
        @NotNull(message = "Scegli l'esito finale") ContactOutcome outcome,
        @NotBlank(message = "Scrivi la nota per il Tutor") @Size(max = 500, message = "Massimo 500 caratteri") String note,
        String companyId) {
}
