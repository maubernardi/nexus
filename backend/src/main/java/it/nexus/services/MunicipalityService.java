package it.nexus.services;

import java.time.LocalDate;
import java.util.List;

import it.nexus.domain.dto.MunicipalityDTO;
import it.nexus.domain.dto.MunicipalityImportPreviewDTO;
import it.nexus.domain.dto.MunicipalityStatusDTO;

/** Comuni e province dall'elenco dell'ISTAT, temporizzati (US-114). */
public interface MunicipalityService {

    /** Comuni e province validi oggi e l'ultimo caricamento. */
    MunicipalityStatusDTO status();

    /**
     * Comuni il cui nome contiene il testo (dalla seconda lettera; prima nessun risultato): solo quelli validi oggi
     * oppure, con {@code history}, anche le versioni passate.
     */
    List<MunicipalityDTO> search(String text, boolean history);

    /** Differenze tra il file e l'elenco attuale, senza salvare nulla. */
    MunicipalityImportPreviewDTO preview(byte[] file);

    /**
     * Applica il file dalla data indicata: chiude le versioni soppresse o cambiate e apre quelle nuove. La data deve
     * essere successiva all'ultimo caricamento e non nel futuro.
     */
    MunicipalityImportPreviewDTO importFile(byte[] file, LocalDate validFrom);
}
