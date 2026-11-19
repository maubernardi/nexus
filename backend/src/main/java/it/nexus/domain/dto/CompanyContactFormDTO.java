package it.nexus.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Referente inviato dal modulo dell'azienda (US-510). Serve almeno il nome o il ruolo: lo controlla il servizio, con
 * l'errore sul nome.
 */
public record CompanyContactFormDTO(
        @Size(max = 200, message = "Massimo 200 caratteri") String name,
        /** Ruolo: voce attiva o un ruolo che un referente dell'azienda ha già; vuoto = nessuno. */
        String roleId,
        @Size(max = 30, message = "Massimo 30 caratteri")
        @Pattern(regexp = CompanyFormDTO.PHONE_REGEX, message = CompanyFormDTO.PHONE_MESSAGE) String phone,
        @Size(max = 30, message = "Massimo 30 caratteri")
        @Pattern(regexp = CompanyFormDTO.PHONE_REGEX, message = CompanyFormDTO.PHONE_MESSAGE) String mobile,
        @Size(max = 254, message = "Massimo 254 caratteri") @Email(message = CompanyFormDTO.EMAIL_MESSAGE)
        String email) {
}
