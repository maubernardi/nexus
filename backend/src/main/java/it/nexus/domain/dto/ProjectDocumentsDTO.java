package it.nexus.domain.dto;

import java.util.List;

/**
 * Dati dei documenti di un progetto per l'ADMIN: il riferimento all'avviso in vigore ({@code defaultNotice} = è il
 * testo predefinito) e la riga di intestazione con codice e CUP (US-1011), i dati del promotore e le variabili libere
 * (US-1013).
 */
public record ProjectDocumentsDTO(ReferenceItemDTO project, boolean active, String noticeReference,
        boolean defaultNotice, String headerLine, ProjectPromoterDTO promoter, List<ProjectVariableDTO> variables) {
}
