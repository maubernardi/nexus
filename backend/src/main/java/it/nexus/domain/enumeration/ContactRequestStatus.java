package it.nexus.domain.enumeration;

/** Stato di una richiesta di contatto (US-507): la chiusura con l'esito arriva con US-512. */
public enum ContactRequestStatus {
    /** Inviata dal Tutor, da prendere in carico. */
    APERTA,
    /** Un operatore del Call Center se ne occupa. */
    IN_CARICO,
    /** Chiusa dall'operatore con l'esito (US-512). */
    CHIUSA
}
