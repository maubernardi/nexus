package it.nexus.domain.dto;

import java.util.List;

/**
 * Una pagina dell'elenco delle aziende (US-513): {@code page} parte da 0, {@code total} conta tutte le aziende che
 * rispondono ai filtri.
 */
public record CompanyPageDTO(List<CompanyDTO> companies, long total, int page, int size) {
}
