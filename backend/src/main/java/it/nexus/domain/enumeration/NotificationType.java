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
    APPOINTMENT_FAILED,
    /** All'operatore: il Tutor ha rifiutato la proposta, la segnalazione è riaperta (US-702). */
    PROPOSAL_REJECTED,
    /** Al Tutor: promemoria al 4° giorno lavorativo dall'accettazione, senza appuntamento registrato (US-704). */
    TIMER_REMINDER,
    /** Al Tutor e all'operatore: il timer è scaduto senza appuntamento, la segnalazione è riaperta (US-705). */
    TIMER_EXPIRED,
    /** All'operatore: il Tutor chiede il rilancio in bacheca, c'è una bozza da controllare (US-803). */
    BOARD_RELAUNCH_REQUESTED,
    /** Al Tutor: il Call Center ha rifiutato la corsia veloce, la segnalazione continua normale (US-404). */
    FAST_TRACK_REJECTED,
    /** Al Tutor: l'annuncio è uscito dalla bacheca mentre la candidatura aspettava, la corsia veloce è decaduta. */
    FAST_TRACK_DROPPED,
    /** All'operatore: il Tutor ha avviato il tirocinio (US-1003). */
    INTERNSHIP_STARTED,
    /** All'operatore: il Tutor ha inviato il report di restituzione, il percorso è concluso (US-1101). */
    CLOSURE_REPORT_SUBMITTED,
    /** Al Tutor: gli è stata affidata la segnalazione di un altro Tutor, con il beneficiario (US-111). */
    TICKET_TRANSFERRED,
    /** Al Tutor: 7 e 3 giorni alla fine prevista, la convenzione è in chiusura: prorogarla o chiuderla (US-1203). */
    INTERNSHIP_ENDING,
    /**
     * Al Tutor: 3 e 7 giorni dopo la fine prevista, il tirocinio va chiuso o prorogato; intanto non apre nuove
     * segnalazioni (US-1203).
     */
    INTERNSHIP_OVERDUE,
    /**
     * All'operatore: nel report di restituzione il Tutor propone l'azienda come nave scuola (US-515). L'avviso porta alla
     * scheda dell'azienda.
     */
    TRAINING_SHIP_PROPOSED,
    /** Al Tutor: il Call Center ha chiuso la sua richiesta di contatto, con l'esito e la nota (US-512). */
    CONTACT_REQUEST_CLOSED
}
