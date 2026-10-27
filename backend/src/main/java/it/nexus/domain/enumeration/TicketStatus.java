package it.nexus.domain.enumeration;

/** Stati della segnalazione (FSM del brief, fasi 1–7). */
public enum TicketStatus {
    NUOVA,
    /** Nuova tipologia di mansione proposta dal Tutor, da approvare dal Call Center (US-302). */
    IN_ATTESA_APPROVAZIONE,
    IN_LAVORAZIONE,
    PROPOSTA_AZIENDA,
    PROPOSTA_ACCOLTA,
    APPUNTAMENTO,
    IN_TIROCINIO,
    FORM_RESTITUZIONE,
    RIAPERTO;

    /** Unico stato conclusivo: in ogni altro stato la segnalazione è aperta (una sola per beneficiario). */
    public static final TicketStatus CLOSED = FORM_RESTITUZIONE;

    /**
     * Stato da un valore registrato: tollera il nome precedente a US-302 ({@code IN_ATTESA_APPROVAZIONE_ADMIN}), che può
     * restare negli eventi dell'audit, immodificabili.
     */
    public static TicketStatus fromStored(String value) {
        return "IN_ATTESA_APPROVAZIONE_ADMIN".equals(value) ? IN_ATTESA_APPROVAZIONE : valueOf(value);
    }
}
