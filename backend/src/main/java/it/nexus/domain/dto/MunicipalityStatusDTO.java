package it.nexus.domain.dto;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Stato dell'elenco dei comuni (US-114): comuni e province validi oggi e l'ultimo caricamento (assente se non ce ne
 * sono stati).
 */
public record MunicipalityStatusDTO(long municipalities, long provinces, LastImport lastImport) {

    /** Ultimo caricamento: data da cui vale, quando e da chi è stato fatto. */
    public record LastImport(LocalDate validFrom, Instant importedAt, String importedBy) {
    }
}
