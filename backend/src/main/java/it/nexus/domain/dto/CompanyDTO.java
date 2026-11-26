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
        /** Zona del catalogo (US-511); assente se non indicata. */
        ReferenceItemDTO zone,
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
        /** Affidabilità, da 1 a 4 stelle (US-514); assente se non valutata. */
        Integer reliability,
        /** Interesse al progetto, 1 o 2 pollici (US-514); assente se non valutato. */
        Integer projectInterest,
        /** Nave scuola (US-515). */
        boolean trainingShip,
        boolean active,
        long version,
        Instant updatedAt,
        /** Dati arrivati vuoti dal database attuale, con il segnaposto ancora da sostituire (US-508). */
        List<CompanyMissingData> missingData) {
}
