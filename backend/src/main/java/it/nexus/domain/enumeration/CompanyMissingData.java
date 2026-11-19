package it.nexus.domain.enumeration;

/** Dato di un'azienda arrivato vuoto dal database attuale, con un segnaposto da sostituire (US-508). */
public enum CompanyMissingData {
    /** Ragione sociale: «Azienda senza nome n. ‹numero›». */
    NAME,
    /** Partita IVA: «00000000000». */
    VAT_CODE
}
