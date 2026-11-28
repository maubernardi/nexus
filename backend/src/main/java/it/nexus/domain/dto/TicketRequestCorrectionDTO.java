package it.nexus.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Correzione del progetto e della mansione richiesta (US-309), dal Tutor della segnalazione prima della proposta. La
 * mansione è una tipologia del catalogo ({@code jobCategoryId}) oppure, se la segnalazione aspetta l'approvazione della
 * nuova tipologia, il testo libero. Sede di competenza e tipo di tirocinio contano solo se cambia il progetto.
 */
public record TicketRequestCorrectionDTO(
        @NotNull(message = "Versione mancante") @PositiveOrZero Long version,
        @NotBlank(message = "Scegli il progetto") String projectId,
        String jobCategoryId,
        @Size(max = 200, message = "Massimo 200 caratteri") String jobFreeText,
        String competenceSiteId,
        String internshipTypeId) {
}
