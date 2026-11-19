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
        /** Categoria merceologica (US-509); assente se non indicata. */
        ReferenceItemDTO businessCategory,
        /** Descrizione dell'attività (US-509). */
        String activity,
        /** Sede operativa (US-510). */
        String operatingAddress,
        MunicipalityDTO operatingMunicipality,
        String phone,
        /** Cellulare, fax e seconda email dell'azienda (US-510). */
        String mobile,
        String fax,
        String email,
        String secondaryEmail,
        /** Referenti in ordine (US-510). */
        List<CompanyContactDTO> contacts,
        boolean active,
        long version,
        Instant updatedAt,
        /** Dati arrivati vuoti dal database attuale, con il segnaposto ancora da sostituire (US-508). */
        List<CompanyMissingData> missingData) {
}
