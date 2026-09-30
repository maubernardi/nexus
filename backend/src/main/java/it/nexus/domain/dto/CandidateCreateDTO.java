package it.nexus.domain.dto;

import java.util.List;
import java.util.Set;

import it.nexus.domain.enumeration.EducationLevel;
import it.nexus.domain.enumeration.Gender;
import it.nexus.domain.enumeration.LicenseType;
import it.nexus.domain.enumeration.TransportMode;
import it.nexus.validation.BirthYear;
import it.nexus.validation.IsoCountry;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Dati per registrare un candidato. Il possesso della patente è dedotto da {@code licenseTypes}. */
public record CandidateCreateDTO(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotNull @BirthYear Integer birthYear,
        @NotNull Gender gender,
        @IsoCountry String nationality,
        @IsoCountry String citizenship,
        @NotBlank String residenceZoneId,
        Set<LicenseType> licenseTypes,
        boolean hasVehicle,
        TransportMode transportMode,
        boolean hasLaw68,
        EducationLevel educationLevel,
        @Size(max = 2000) String constraints,
        @Valid @Size(max = 12) List<CandidateLanguageDTO> languages) {

    public CandidateCreateDTO {
        licenseTypes = licenseTypes == null ? Set.of() : Set.copyOf(licenseTypes);
        languages = languages == null ? List.of() : List.copyOf(languages);
    }
}
