package it.nexus.domain.dto;

import java.util.List;

/** Una pagina del registro degli accessi, dal più recente; {@code hasMore} se ce ne sono di più vecchi (US-113). */
public record AccessLogPageDTO(List<AccessLogEntryDTO> entries, boolean hasMore) {
}
