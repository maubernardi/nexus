package it.nexus.domain.enumeration;

/**
 * Tipi di evento dello storico dell'azienda ({@code company_audit_event.event_type}). Oggi nasce solo
 * {@link #PROPOSTA_INVIATA} (US-602); gli altri arrivano con le fasi successive (R2, R3), ma il riepilogo li conta già
 * (US-503).
 */
public enum CompanyEventType {
    PROPOSTA_INVIATA,
    PROPOSTA_ACCOLTA,
    PROPOSTA_RIFIUTATA,
    APPUNTAMENTO_FISSATO,
    APPUNTAMENTO_FALLITO,
    TIROCINIO_AVVIATO,
    /** Dettaglio {@code assunto}: true se il tirocinio si è concluso con un'assunzione. */
    TIROCINIO_CONCLUSO
}
