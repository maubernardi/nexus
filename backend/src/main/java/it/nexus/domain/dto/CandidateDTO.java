package it.nexus.domain.dto;

import java.time.Instant;
import java.util.List;

import it.nexus.domain.enumeration.EducationLevel;
import it.nexus.domain.enumeration.Gender;
import it.nexus.domain.enumeration.LicenseType;
import it.nexus.domain.enumeration.TransportMode;

public record CandidateDTO(
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
        List<CandidateLanguageDTO> languages,
        Instant createdAt) {
}
