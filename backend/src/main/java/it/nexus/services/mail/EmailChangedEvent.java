package it.nexus.services.mail;

/**
 * Indirizzo email di un utente cambiato (US-110): dopo il commit parte l'avviso al vecchio indirizzo.
 * {@code toString()} senza l'indirizzo: niente dati personali nei log.
 */
public record EmailChangedEvent(Long userId, String previousEmail) {

    @Override
    public String toString() {
        return "EmailChangedEvent[userId=" + userId + "]";
    }
}
