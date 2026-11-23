package it.nexus.services.messages;

import java.util.EnumMap;
import java.util.Map;

import it.nexus.domain.enumeration.MessageKey;
import it.nexus.domain.enumeration.MessagePart;

/**
 * Testi predefiniti dei messaggi (US-1204): quelli di prima della storia, più la parte distintiva dell'oggetto delle
 * email delle notifiche (PO, 22/11). Valgono finché l'ADMIN non li cambia, e tornano con «ripristina».
 */
final class MessageDefaults {

    private static final Map<MessageKey, Map<MessagePart, String>> TEXTS = new EnumMap<>(MessageKey.class);

    static {
        notification(MessageKey.CATEGORY_CHANGED,
                "Il Call Center ha scelto una tipologia diversa da quella che avevi proposto per la segnalazione n. {numero}: il motivo è nella cronologia.",
                "Tipologia cambiata dal Call Center",
                "Il Call Center ha scelto una tipologia diversa da quella che avevi proposto per la segnalazione n. {numero}: il motivo è nella cronologia.");
        notification(MessageKey.CATEGORY_APPROVED,
                "La nuova tipologia che avevi proposto per la segnalazione n. {numero} è stata approvata.",
                "Tipologia approvata",
                "La nuova tipologia che avevi proposto per la segnalazione n. {numero} è stata approvata.");
        notification(MessageKey.PROPOSAL_SENT,
                "C’è una proposta di tirocinio per la segnalazione n. {numero}: valutala e accettala.",
                "Nuova proposta di tirocinio",
                "C'è una proposta di tirocinio per la segnalazione n. {numero}: valutala e accettala.");
        notification(MessageKey.PROPOSAL_ACCEPTED,
                "Il Tutor ha accettato la proposta della segnalazione n. {numero}: è partito il timer.",
                "Proposta accettata",
                "Il Tutor ha accettato la proposta della segnalazione n. {numero}: è partito il timer.");
        notification(MessageKey.APPOINTMENT_SET,
                "Il Tutor ha registrato l’appuntamento con l’azienda per la segnalazione n. {numero}.",
                "Appuntamento fissato",
                "Il Tutor ha registrato l'appuntamento con l'azienda per la segnalazione n. {numero}.");
        notification(MessageKey.APPOINTMENT_FAILED,
                "L’appuntamento della segnalazione n. {numero} non è andato a buon fine: la segnalazione è riaperta, il motivo è nella cronologia.",
                "Appuntamento non andato a buon fine",
                "L'appuntamento della segnalazione n. {numero} non è andato a buon fine: la segnalazione è riaperta, il motivo è nella cronologia.");
        notification(MessageKey.PROPOSAL_REJECTED,
                "Il Tutor ha rifiutato la proposta della segnalazione n. {numero}: l’azienda è esclusa e la segnalazione è riaperta.",
                "Proposta rifiutata",
                "Il Tutor ha rifiutato la proposta della segnalazione n. {numero}: l'azienda è esclusa e la segnalazione è riaperta.");
        notification(MessageKey.TIMER_REMINDER,
                "Promemoria: per la segnalazione n. {numero} manca la registrazione dell’appuntamento con l’azienda. La scadenza del timer è nel dettaglio.",
                "Promemoria dell'appuntamento",
                "Promemoria: per la segnalazione n. {numero} manca la registrazione dell'appuntamento con l'azienda. La scadenza del timer è nel dettaglio.");
        notification(MessageKey.TIMER_EXPIRED,
                "Il timer della segnalazione n. {numero} è scaduto senza appuntamento: la segnalazione è riaperta e la mansione è di nuovo libera.",
                "Timer scaduto",
                "Il timer della segnalazione n. {numero} è scaduto senza appuntamento: la segnalazione è riaperta e la mansione è di nuovo libera.");
        notification(MessageKey.BOARD_RELAUNCH_REQUESTED,
                "Il Tutor chiede di rilanciare in bacheca la mansione della segnalazione n. {numero}: c’è una bozza di annuncio da controllare e pubblicare.",
                "Rilancio in bacheca",
                "Il Tutor chiede di rilanciare in bacheca la mansione della segnalazione n. {numero}: c'è una bozza di annuncio da controllare e pubblicare.");
        notification(MessageKey.FAST_TRACK_REJECTED,
                "Il Call Center non ha approvato la corsia veloce della segnalazione n. {numero}: continua in lavorazione normale, il motivo è nella cronologia.",
                "Corsia veloce non approvata",
                "Il Call Center non ha approvato la corsia veloce della segnalazione n. {numero}: continua in lavorazione normale, il motivo è nella cronologia.");
        notification(MessageKey.FAST_TRACK_DROPPED,
                "L’annuncio della segnalazione n. {numero} non è più in bacheca: la corsia veloce è decaduta e la segnalazione continua in lavorazione normale.",
                "Corsia veloce decaduta",
                "L'annuncio della segnalazione n. {numero} non è più in bacheca: la corsia veloce è decaduta e la segnalazione continua in lavorazione normale.");
        notification(MessageKey.INTERNSHIP_STARTED,
                "Il Tutor ha avviato il tirocinio della segnalazione n. {numero}.",
                "Tirocinio avviato",
                "Il Tutor ha avviato il tirocinio della segnalazione n. {numero}.");
        notification(MessageKey.CLOSURE_REPORT_SUBMITTED,
                "Il Tutor ha inviato il report di restituzione della segnalazione n. {numero}: il percorso è concluso.",
                "Percorso concluso",
                "Il Tutor ha inviato il report di restituzione della segnalazione n. {numero}: il percorso è concluso.");
        notification(MessageKey.TICKET_TRANSFERRED,
                "Ti è stata affidata la segnalazione n. {numero}, con il suo beneficiario: prima la seguiva un altro Tutor.",
                "Segnalazione affidata a te",
                "Ti è stata affidata la segnalazione n. {numero}, con il suo beneficiario: prima la seguiva un altro Tutor.");
        notification(MessageKey.INTERNSHIP_ENDING,
                "Il tirocinio della segnalazione n. {numero} sta per finire: la convenzione è in chiusura. Se serve, prorogalo; altrimenti ricordati di chiuderlo con il report di restituzione.",
                "Convenzione in chiusura",
                "Il tirocinio della segnalazione n. {numero} sta per finire: la convenzione è in chiusura. Se serve, prorogalo; altrimenti ricordati di chiuderlo con il report di restituzione.");
        notification(MessageKey.INTERNSHIP_OVERDUE,
                "Il tirocinio della segnalazione n. {numero} è finito alla data prevista: chiudilo con il report di restituzione o prorogalo. Finché non chiudi i tirocini finiti non puoi aprire nuove segnalazioni.",
                "Tirocinio da chiudere o prorogare",
                "Il tirocinio della segnalazione n. {numero} è finito alla data prevista: chiudilo con il report di restituzione o prorogalo. Finché non chiudi i tirocini finiti non puoi aprire nuove segnalazioni.");
        email(MessageKey.EMAIL_CHANGE_CODE, "Codice per confermare il nuovo indirizzo email", """
                Ciao,

                per confermare questo indirizzo come nuova email del tuo account NEXUS scrivi il codice:

                    {codice}

                Il codice vale fino alle {ora} (30 minuti). Se non hai chiesto tu il cambio, ignora questa email: \
                l'indirizzo del tuo account resta quello di prima.""");
        email(MessageKey.EMAIL_CHANGE_NOTICE, "Il tuo indirizzo email è stato cambiato", """
                Ciao,

                l'indirizzo email del tuo account NEXUS è stato cambiato: da ora le notifiche arrivano al nuovo \
                indirizzo e questo non riceverà più messaggi da NEXUS.

                Se non sei stato tu, contatta subito l'amministratore di NEXUS.""");
    }

    private MessageDefaults() {
    }

    /** Il testo predefinito della parte; nessuno se il messaggio non ha quella parte. */
    static String get(MessageKey key, MessagePart part) {
        return TEXTS.getOrDefault(key, Map.of()).get(part);
    }

    private static void notification(MessageKey key, String app, String subject, String body) {
        Map<MessagePart, String> texts = new EnumMap<>(MessagePart.class);
        texts.put(MessagePart.APP, app);
        texts.put(MessagePart.EMAIL_SUBJECT, subject);
        texts.put(MessagePart.EMAIL_BODY, body);
        TEXTS.put(key, texts);
    }

    private static void email(MessageKey key, String subject, String body) {
        Map<MessagePart, String> texts = new EnumMap<>(MessagePart.class);
        texts.put(MessagePart.EMAIL_SUBJECT, subject);
        texts.put(MessagePart.EMAIL_BODY, body);
        TEXTS.put(key, texts);
    }
}
