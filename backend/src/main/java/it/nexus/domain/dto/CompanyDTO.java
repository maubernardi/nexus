package it.nexus.domain.dto;

import java.time.Instant;

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
        Instant updatedAt) {
}
