package it.nexus.domain.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

/**
 * Dati dei documenti di un progetto dal modulo dell'ADMIN: vuoti = avviso predefinito, nessuna intestazione (US-1011).
 * Promotore e variabili (US-1013) assenti = invariati; le variabili sono l'elenco completo: quelle che mancano si
 * tolgono.
 */
public record ProjectDocumentsFormDTO(
        @Size(max = 1000, message = "Massimo 1000 caratteri") String noticeReference,
        @Size(max = 300, message = "Massimo 300 caratteri") String headerLine,
        @Valid ProjectPromoterDTO promoter,
        @Valid @Size(max = 50, message = "Al massimo 50 variabili") List<ProjectVariableDTO> variables) {
}
