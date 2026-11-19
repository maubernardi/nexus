package it.nexus.domain.dto;

import java.util.List;

import it.nexus.domain.CompanyPlaceholders;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Dati dell'azienda inviati dal modulo; {@code version} serve solo in modifica (blocco ottimistico). I segnaposto dei dati
 * mancanti nel database attuale non si salvano: il modulo chiede quelli veri (US-508).
 */
public record CompanyFormDTO(
        @NotBlank(message = "Inserisci la ragione sociale") @Size(max = 200, message = "Massimo 200 caratteri")
        @Pattern(regexp = CompanyPlaceholders.NOT_NAME_REGEX, message = NAME_PLACEHOLDER) String name,
        @NotBlank(message = "Inserisci la partita IVA")
        @Pattern(regexp = "\\s*[0-9]{11}\\s*", message = "La partita IVA è composta da 11 cifre")
        @Pattern(regexp = CompanyPlaceholders.NOT_VAT_CODE_REGEX, message = VAT_CODE_PLACEHOLDER) String vatCode,
        @Size(max = 300, message = "Massimo 300 caratteri") String legalAddress,
        /** Versione del comune della sede legale (US-1007); vuoto = non indicato. */
        String legalMunicipalityId,
        /** Categoria merceologica (US-509): voce attiva o quella che l'azienda ha già; vuoto = nessuna. */
        String businessCategoryId,
        @Size(max = 200, message = "Massimo 200 caratteri") String activity,
        /** Sede operativa (US-510): indirizzo e versione del comune; vuoti = non indicati. */
        @Size(max = 300, message = "Massimo 300 caratteri") String operatingAddress,
        String operatingMunicipalityId,
        @Size(max = 30, message = "Massimo 30 caratteri")
        @Pattern(regexp = PHONE_REGEX, message = PHONE_MESSAGE) String phone,
        @Size(max = 30, message = "Massimo 30 caratteri")
        @Pattern(regexp = PHONE_REGEX, message = PHONE_MESSAGE) String mobile,
        @Size(max = 30, message = "Massimo 30 caratteri")
        @Pattern(regexp = PHONE_REGEX, message = PHONE_MESSAGE) String fax,
        @Size(max = 254, message = "Massimo 254 caratteri") @Email(message = EMAIL_MESSAGE) String email,
        @Size(max = 254, message = "Massimo 254 caratteri") @Email(message = EMAIL_MESSAGE) String secondaryEmail,
        /** Referenti in ordine (US-510); assente = nessuno. */
        @Size(max = MAX_CONTACTS, message = "Al massimo 10 referenti") List<@Valid CompanyContactFormDTO> contacts,
        @PositiveOrZero Long version) {

    /** Massimo dei referenti di un'azienda (US-510). */
    public static final int MAX_CONTACTS = 10;
    static final String PHONE_REGEX = "[0-9 +().\\-/]*";
    static final String PHONE_MESSAGE = "Usa solo cifre, spazi e i simboli + ( ) . - /";
    static final String EMAIL_MESSAGE = "Indirizzo email non valido";

    static final String NAME_PLACEHOLDER =
            "Inserisci la ragione sociale vera: «Azienda senza nome n. …» vuol dire che manca";
    static final String VAT_CODE_PLACEHOLDER = "Inserisci la partita IVA vera: «00000000000» vuol dire che manca";
}
