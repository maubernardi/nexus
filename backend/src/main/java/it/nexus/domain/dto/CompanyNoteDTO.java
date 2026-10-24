package it.nexus.domain.dto;

import java.time.Instant;

/** Nota del diario dell'azienda, con il nome dell'autore. */
public record CompanyNoteDTO(String id, String text, Instant createdAt, String authorName) {
}
