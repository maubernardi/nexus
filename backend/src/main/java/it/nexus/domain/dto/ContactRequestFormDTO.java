package it.nexus.domain.dto;

import jakarta.validation.constraints.Size;

/**
 * Richiesta di contatto dal Tutor (US-507): per un'azienda già presente {@code companyId}; per un'azienda nuova nome e
 * zona, obbligatori, e la categoria merceologica facoltativa (controlli nel servizio).
 */
public record ContactRequestFormDTO(
        String companyId,
        @Size(max = 200, message = "Massimo 200 caratteri") String companyName,
        String zoneId,
        String businessCategoryId) {
}
