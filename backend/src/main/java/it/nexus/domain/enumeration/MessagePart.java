package it.nexus.domain.enumeration;

/** Parti di un messaggio con il testo modificabile dall'ADMIN (US-1204). */
public enum MessagePart {
    /** Testo della notifica nell'app. */
    APP,
    /** Parte distintiva dell'oggetto dell'email, dopo «PORTALE NEXUS - » (e «Segnalazione n. …» per le notifiche). */
    EMAIL_SUBJECT,
    /** Testo dell'email, prima delle parti fisse (collegamento, avvertenza, firma). */
    EMAIL_BODY
}
