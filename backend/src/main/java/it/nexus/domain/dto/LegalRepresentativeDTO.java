package it.nexus.domain.dto;

import java.time.LocalDate;

/** Legale rappresentante dell'azienda (US-506): dati personali per i documenti dei tirocini. */
public record LegalRepresentativeDTO(
        String firstName,
        String lastName,
        String fiscalCode,
        LocalDate birthDate,
        String birthPlace,
        String residence,
        long version) {
}
