package it.nexus.domain.dto;

import java.time.Instant;

/** Azienda esclusa per la segnalazione, con il motivo, chi l'ha esclusa e quando (US-603). */
public record CompanyExclusionDTO(String companyId, String companyName, String reason, Instant createdAt,
        String authorName) {
}
