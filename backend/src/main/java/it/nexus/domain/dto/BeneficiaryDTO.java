package it.nexus.domain.dto;

import java.time.Instant;
import java.util.List;

import it.nexus.domain.enumeration.EducationLevel;
import it.nexus.domain.enumeration.Gender;
import it.nexus.domain.enumeration.LicenseType;
import it.nexus.domain.enumeration.TransportMode;

/**
 * Scheda del beneficiario. {@code editable}: l'utente corrente può modificarlo (solo il Tutor proprietario, US-203).
 */
public record BeneficiaryDTO(
        String id,
        String firstName,
        String lastName,
        Integer birthYear,
        Gender gender,
        String nationality,
        String citizenship,
        ReferenceItemDTO residenceZone,
        boolean hasDrivingLicense,
        List<LicenseType> licenseTypes,
        boolean hasVehicle,
        TransportMode transportMode,
        boolean hasLaw68,
        EducationLevel educationLevel,
        String constraints,
        List<BeneficiaryLanguageDTO> languages,
        Instant createdAt,
        String ownerTutorName,
        boolean editable,
        /** Nome del CV caricato (US-205), assente se non c'è. */
        String cvFileName,
        Long cvSizeBytes,
        /* contatti e presa in carico (US-206) */
        String email,
        String phone,
        ReferenceItemDTO careAgency,
        String caseManagerName,
        String caseManagerOffice,
        String caseManagerPhone,
        String caseManagerEmail,
        /** Ha un tirocinio aperto: l'ente non si cambia. */
        boolean careAgencyLocked,
        /** Ha un tirocinio di inclusione aperto: nome, mail e telefono del case manager non si svuotano. */
        boolean caseManagerRequired) {
}
