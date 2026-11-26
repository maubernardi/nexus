package it.nexus.domain.dto;

import java.util.List;

/**
 * Valutazioni dei tirocini di un'azienda (US-1104), dalla più recente, una pagina alla volta ({@code page} da 0).
 * {@code average} è la media delle medie di tutte le valutazioni, assente se non ce ne sono; {@code total} quante sono.
 */
public record CompanyReviewsDTO(Double average, List<CompanyReviewDTO> reviews, long total, int page, int size) {
}
