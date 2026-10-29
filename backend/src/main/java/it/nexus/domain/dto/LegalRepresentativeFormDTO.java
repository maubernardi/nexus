package it.nexus.domain.dto;

import java.time.LocalDate;

import it.nexus.validation.FiscalCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Legale rappresentante dal modulo (US-506): nome e cognome obbligatori, il resto quando si conosce (i documenti
 * segnaleranno ciò che manca). {@code version} nullo alla prima registrazione.
 */
public record LegalRepresentativeFormDTO(
        @NotBlank(message = "Inserisci il nome") @Size(max = 100, message = "Massimo 100 caratteri") String firstName,
        @NotBlank(message = "Inserisci il cognome") @Size(max = 100, message = "Massimo 100 caratteri") String lastName,
        @FiscalCode String fiscalCode,
        @Past(message = "La data di nascita deve essere passata") LocalDate birthDate,
        @Size(max = 200, message = "Massimo 200 caratteri") String birthPlace,
        @Size(max = 300, message = "Massimo 300 caratteri") String residence,
        @PositiveOrZero Long version) {
}
