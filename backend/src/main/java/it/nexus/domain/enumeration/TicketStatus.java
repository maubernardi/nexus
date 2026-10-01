package it.nexus.domain.enumeration;

/** Stati della segnalazione (FSM del brief, fasi 1–7). */
public enum TicketStatus {
    NUOVA,
    IN_ATTESA_APPROVAZIONE_ADMIN,
    IN_LAVORAZIONE,
    PROPOSTA_AZIENDA,
    PROPOSTA_ACCOLTA,
    APPUNTAMENTO,
    IN_TIROCINIO,
    FORM_RESTITUZIONE,
    RIAPERTO;

    /** Unico stato conclusivo: in ogni altro stato la segnalazione è aperta (una sola per beneficiario). */
    public static final TicketStatus CLOSED = FORM_RESTITUZIONE;
}
