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
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Dati del beneficiario inviati dal modulo, in registrazione e in modifica. Il possesso della patente è dedotto da
 * {@code licenseTypes}.
 */
public record BeneficiaryFormDTO(
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
        @Valid @Size(max = 12) List<BeneficiaryLanguageDTO> languages,
        /* contatti e presa in carico (US-206), tutti facoltativi */
        @Size(max = 254, message = "Massimo 254 caratteri")
        @Email(message = "Inserisci un indirizzo email valido") String email,
        @Size(max = 30, message = "Massimo 30 caratteri")
        @Pattern(regexp = PHONE, message = "Inserisci solo cifre, spazi e + . / ( ) -") String phone,
        String careAgencyId,
        @Size(max = 200, message = "Massimo 200 caratteri") String caseManagerName,
        @Size(max = 300, message = "Massimo 300 caratteri") String caseManagerOffice,
        @Size(max = 30, message = "Massimo 30 caratteri")
        @Pattern(regexp = PHONE, message = "Inserisci solo cifre, spazi e + . / ( ) -") String caseManagerPhone,
        @Size(max = 254, message = "Massimo 254 caratteri")
        @Email(message = "Inserisci un indirizzo email valido") String caseManagerEmail) {

    /** Telefono: cifre e i segni usati per scriverlo; vuoto ammesso (campo facoltativo). */
    public static final String PHONE = "^[0-9 +./()-]*$";

    public BeneficiaryFormDTO {
        licenseTypes = licenseTypes == null ? Set.of() : Set.copyOf(licenseTypes);
        languages = languages == null ? List.of() : List.copyOf(languages);
    }
}
