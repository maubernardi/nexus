package it.nexus.domain.enumeration;

/**
 * Conservazione dei dati di un beneficiario (US-1303). Nessuno stato: dati conservati normalmente.
 */
public enum RetentionStatus {
    /** Nessuna attività da più del periodo di conservazione: il Tutor o l'ADMIN decidono. */
    TO_ANONYMIZE,
    /** Tenuto per sempre, con un motivo. */
    KEPT,
    /** Dati identificativi cancellati: restano le iniziali e i dati statistici. */
    ANONYMIZED
}
