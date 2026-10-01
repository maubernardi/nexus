package it.nexus.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Dati dell'azienda inviati dal modulo; {@code version} serve solo in modifica (blocco ottimistico). */
public record CompanyFormDTO(
        @NotBlank(message = "Inserisci la ragione sociale") @Size(max = 200, message = "Massimo 200 caratteri") String name,
        @NotBlank(message = "Inserisci la partita IVA")
        @Pattern(regexp = "\\s*[0-9]{11}\\s*", message = "La partita IVA è composta da 11 cifre") String vatCode,
        @Size(max = 300, message = "Massimo 300 caratteri") String legalAddress,
        @Size(max = 200, message = "Massimo 200 caratteri") String contactPerson,
        @Size(max = 30, message = "Massimo 30 caratteri")
        @Pattern(regexp = "[0-9 +().\\-/]*", message = "Usa solo cifre, spazi e i simboli + ( ) . - /") String phone,
        @Size(max = 254, message = "Massimo 254 caratteri") @Email(message = "Indirizzo email non valido") String email,
        @PositiveOrZero Long version) {
}
