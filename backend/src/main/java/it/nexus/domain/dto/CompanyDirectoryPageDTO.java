package it.nexus.domain.dto;

import java.util.List;

/** Una pagina dell'elenco delle aziende per il Tutor (US-511): {@code page} parte da 0. */
public record CompanyDirectoryPageDTO(List<CompanyDirectoryItemDTO> companies, long total, int page, int size) {
}
