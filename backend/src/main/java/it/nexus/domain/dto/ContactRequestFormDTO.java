package it.nexus.domain.dto;

import jakarta.validation.constraints.Size;

/**
 * Richiesta di contatto dal Tutor (US-507), solo per un'azienda non ancora in anagrafica (PO, 27/11): nome e zona
 * obbligatori, categoria merceologica facoltativa (controlli nel servizio, con le sole voci attive).
 */
public record ContactRequestFormDTO(
        @Size(max = 200, message = "Massimo 200 caratteri") String companyName,
        String zoneId,
        String businessCategoryId) {
}
