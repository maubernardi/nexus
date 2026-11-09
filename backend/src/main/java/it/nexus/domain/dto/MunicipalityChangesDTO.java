package it.nexus.domain.dto;

import java.util.List;

/**
 * Differenze di un caricamento per comuni o province (US-114): i conteggi e, per ogni gruppo, i primi esempi da
 * mostrare all'ADMIN (es. «Sesto Fiorentino (FI)», «Vecchio (FI) → Nuovo (PO)»).
 */
public record MunicipalityChangesDTO(int added, int removed, int changed, List<String> addedExamples,
        List<String> removedExamples, List<String> changedExamples) {
}
