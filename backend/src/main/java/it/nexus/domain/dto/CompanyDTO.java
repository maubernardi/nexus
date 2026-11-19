package it.nexus.domain.dto;

import java.time.Instant;
import java.util.List;

import it.nexus.domain.enumeration.CompanyMissingData;

public record CompanyDTO(
        String id,
        String name,
        String vatCode,
        String legalAddress,
        /** Comune della sede legale (US-1007), con la provincia della versione scelta. */
        MunicipalityDTO legalMunicipality,
        String contactPerson,
        String phone,
        String email,
        boolean active,
        long version,
        Instant updatedAt,
        /** Dati arrivati vuoti dal database attuale, con il segnaposto ancora da sostituire (US-508). */
        List<CompanyMissingData> missingData) {
}
