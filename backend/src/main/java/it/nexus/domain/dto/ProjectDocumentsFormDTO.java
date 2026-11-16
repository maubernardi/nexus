package it.nexus.domain.dto;

import jakarta.validation.constraints.Size;

/** Dati dei documenti di un progetto dal modulo dell'ADMIN (US-1011): vuoti = avviso predefinito, nessuna intestazione. */
public record ProjectDocumentsFormDTO(
        @Size(max = 1000, message = "Massimo 1000 caratteri") String noticeReference,
        @Size(max = 300, message = "Massimo 300 caratteri") String headerLine) {
}
