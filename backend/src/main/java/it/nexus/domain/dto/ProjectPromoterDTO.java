package it.nexus.domain.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Dati del soggetto promotore di un progetto (US-1013), letti e modificati dall'ADMIN. Tutti facoltativi; i testi vuoti
 * valgono come dati assenti.
 */
public record ProjectPromoterDTO(
        @Size(max = 300, message = "Massimo 300 caratteri") String name,
        @Size(max = 300, message = "Massimo 300 caratteri") String address,
        @Pattern(regexp = "^\\s*$|^\\s*([0-9]{11}|[A-Za-z0-9]{16})\\s*$",
                message = "11 cifre, oppure 16 caratteri per un codice fiscale personale") String fiscalCode,
        @Size(max = 200, message = "Massimo 200 caratteri") String representative,
        @Size(max = 200, message = "Massimo 200 caratteri") String representativeRole,
        @Size(max = 200, message = "Massimo 200 caratteri") String representativeBirthPlace,
        @Past(message = "La data di nascita deve essere passata") LocalDate representativeBirthDate,
        @Size(max = 50, message = "Massimo 50 caratteri") String phone,
        @Email(message = "Indirizzo email non valido") @Size(max = 254, message = "Massimo 254 caratteri") String email,
        @Email(message = "Indirizzo PEC non valido") @Size(max = 254, message = "Massimo 254 caratteri") String pec,
        @Size(max = 100, message = "Massimo 100 caratteri") String inailPosition,
        @Size(max = 200, message = "Massimo 200 caratteri") String rcInsurer,
        @Size(max = 100, message = "Massimo 100 caratteri") String rcPolicy,
        LocalDate rcExpiry) {
}
