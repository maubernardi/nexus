package it.nexus.domain.enumeration;

/**
 * Tipi di notifica in app (US-1201). Ogni passaggio avvisa la controparte: il Call Center avvisa il Tutor della
 * segnalazione, il Tutor avvisa l'operatore che l'ha in carico. Le storie successive aggiungono i propri tipi.
 */
public enum NotificationType {
    /** Al Tutor: il Call Center ha scelto una tipologia diversa da quella proposta (US-302). */
    CATEGORY_CHANGED,
    /** Al Tutor: la nuova tipologia proposta è stata approvata (US-302). */
    CATEGORY_APPROVED,
    /** Al Tutor: c'è una proposta per il suo beneficiario (US-602). */
    PROPOSAL_SENT,
    /** All'operatore: il Tutor ha accettato la proposta (US-701). */
    PROPOSAL_ACCEPTED,
    /** All'operatore: il Tutor ha registrato l'appuntamento fissato con l'azienda (US-801). */
    APPOINTMENT_SET,
    /** All'operatore: l'appuntamento non è andato a buon fine, la segnalazione è riaperta (US-802). */
    APPOINTMENT_FAILED
}
