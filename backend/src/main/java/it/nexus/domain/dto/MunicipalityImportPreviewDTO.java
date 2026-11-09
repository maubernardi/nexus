package it.nexus.domain.dto;

/** Anteprima (o esito) del caricamento del file ISTAT: comuni nel file e differenze rispetto all'elenco (US-114). */
public record MunicipalityImportPreviewDTO(int rows, MunicipalityChangesDTO municipalities,
        MunicipalityChangesDTO provinces) {
}
