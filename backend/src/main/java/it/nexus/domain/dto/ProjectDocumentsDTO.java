package it.nexus.domain.dto;

/**
 * Dati dei documenti di un progetto per l'ADMIN (US-1011): il riferimento all'avviso in vigore ({@code defaultNotice}
 * = è il testo predefinito) e la riga di intestazione con codice e CUP.
 */
public record ProjectDocumentsDTO(ReferenceItemDTO project, boolean active, String noticeReference,
        boolean defaultNotice, String headerLine) {
}
