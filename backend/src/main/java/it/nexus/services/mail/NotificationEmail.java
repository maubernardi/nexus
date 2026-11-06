package it.nexus.services.mail;

import it.nexus.domain.enumeration.NotificationType;

/**
 * Oggetto e testo dell'email di una notifica (US-1202). I testi sono quelli della notifica in app: solo il numero della
 * segnalazione, nessun dato personale.
 */
public record NotificationEmail(String subject, String body) {

    /** Email della notifica, con il collegamento alla segnalazione ({@code appUrl} senza barra finale). */
    public static NotificationEmail of(NotificationType type, String ticketId, Long ticketNumber, String appUrl) {
        // il modello del testo, con il numero della segnalazione al posto di %d
        String template = switch (type) {
            case CATEGORY_CHANGED -> "Il Call Center ha scelto una tipologia diversa da quella che avevi proposto per "
                    + "la segnalazione n. %d: il motivo è nella cronologia.";
            case CATEGORY_APPROVED -> "La nuova tipologia che avevi proposto per la segnalazione n. %d è stata approvata.";
            case PROPOSAL_SENT -> "C'è una proposta di tirocinio per la segnalazione n. %d: valutala e accettala.";
            case PROPOSAL_ACCEPTED -> "Il Tutor ha accettato la proposta della segnalazione n. %d: è partito il timer.";
            case APPOINTMENT_SET -> "Il Tutor ha registrato l'appuntamento con l'azienda per la segnalazione n. %d.";
            case APPOINTMENT_FAILED -> "L'appuntamento della segnalazione n. %d non è andato a buon fine: la "
                    + "segnalazione è riaperta, il motivo è nella cronologia.";
            case PROPOSAL_REJECTED -> "Il Tutor ha rifiutato la proposta della segnalazione n. %d: l'azienda è esclusa "
                    + "e la segnalazione è riaperta.";
            case TIMER_REMINDER -> "Promemoria: per la segnalazione n. %d manca la registrazione dell'appuntamento con "
                    + "l'azienda. La scadenza del timer è nel dettaglio.";
            case TIMER_EXPIRED -> "Il timer della segnalazione n. %d è scaduto senza appuntamento: la segnalazione è "
                    + "riaperta e la mansione è di nuovo libera.";
            case BOARD_RELAUNCH_REQUESTED -> "Il Tutor chiede di rilanciare in bacheca la mansione della segnalazione "
                    + "n. %d: c'è una bozza di annuncio da controllare e pubblicare.";
            case FAST_TRACK_REJECTED -> "Il Call Center non ha approvato la corsia veloce della segnalazione n. %d: "
                    + "continua in lavorazione normale, il motivo è nella cronologia.";
            case FAST_TRACK_DROPPED -> "L'annuncio della segnalazione n. %d non è più in bacheca: la corsia veloce è "
                    + "decaduta e la segnalazione continua in lavorazione normale.";
            case INTERNSHIP_STARTED -> "Il Tutor ha avviato il tirocinio della segnalazione n. %d.";
            case CLOSURE_REPORT_SUBMITTED -> "Il Tutor ha inviato il report di restituzione della segnalazione n. %d: "
                    + "il percorso è concluso.";
            case TICKET_TRANSFERRED -> "Ti è stata affidata la segnalazione n. %d, con il suo beneficiario: prima la "
                    + "seguiva un altro Tutor.";
        };
        String text = template.formatted(ticketNumber);
        String body = """
                %s

                Apri la segnalazione: %s/segnalazioni/%s

                Messaggio automatico di NEXUS, non rispondere. Trovi tutte le notifiche anche nell'app, alla voce \
                Notifiche.
                """.formatted(text, appUrl, ticketId);
        return new NotificationEmail("NEXUS · segnalazione n. %d".formatted(ticketNumber), body);
    }
}
