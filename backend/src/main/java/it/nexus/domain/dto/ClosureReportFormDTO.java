package it.nexus.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Report di restituzione dal modulo (US-1101). Assunto: con {@code hiringFormulaId}; non assunto: con
 * {@code noHireReasonId} (controllo nel servizio, con le sole voci attive). Obiettivi e difficoltà sono facoltativi.
 */
public record ClosureReportFormDTO(
        @NotNull(message = "Versione mancante") @PositiveOrZero Long version,
        @NotNull(message = "Indica la soddisfazione dell'azienda")
        @Min(value = 1, message = "Da 1 a 5") @Max(value = 5, message = "Da 1 a 5") Integer companySatisfaction,
        @NotNull(message = "Indica la soddisfazione del beneficiario")
        @Min(value = 1, message = "Da 1 a 5") @Max(value = 5, message = "Da 1 a 5") Integer beneficiarySatisfaction,
        @Size(max = 2000, message = "Massimo 2000 caratteri") String objectives,
        @Size(max = 2000, message = "Massimo 2000 caratteri") String difficulties,
        @NotNull(message = "Indica se il beneficiario è stato assunto") Boolean hired,
        String hiringFormulaId,
        String noHireReasonId,
        @NotNull(message = "Indica se la mansione è ancora disponibile") Boolean jobSlotStillAvailable) {
}
