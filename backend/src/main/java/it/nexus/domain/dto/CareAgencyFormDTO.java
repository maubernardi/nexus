package it.nexus.domain.dto;

import java.time.LocalDate;

import it.nexus.domain.enumeration.CareAgencyKind;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Dati di un ente dal modulo dell'ADMIN (US-115): tutti facoltativi tranne la tipologia. */
public record CareAgencyFormDTO(
        @NotNull(message = "Scegli la tipologia") CareAgencyKind kind,
        @Pattern(regexp = "^([0-9]{11}|[A-Za-z0-9]{16})$",
                message = "Inserisci una partita IVA di 11 cifre o un codice fiscale di 16 caratteri") String fiscalCode,
        @Size(max = 300, message = "Massimo 300 caratteri") String legalAddress,
        @Size(max = 200, message = "Massimo 200 caratteri") String representativeName,
        @Size(max = 200, message = "Massimo 200 caratteri") String representativeRole,
        @Size(max = 200, message = "Massimo 200 caratteri") String representativeBirthPlace,
        @Past(message = "La data di nascita deve essere passata") LocalDate representativeBirthDate,
        @Size(max = 200, message = "Massimo 200 caratteri") String referentName,
        @Size(max = 30, message = "Massimo 30 caratteri")
        @Pattern(regexp = "^[0-9 +./()-]*$", message = "Inserisci solo cifre, spazi e + . / ( ) -") String referentPhone,
        @Size(max = 254, message = "Massimo 254 caratteri")
        @Email(message = "Inserisci un indirizzo email valido") String referentEmail,
        @NotNull @PositiveOrZero Long version) {
}
