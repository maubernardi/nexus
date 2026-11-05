package it.nexus.services;

import it.nexus.domain.dto.EmailStatusDTO;

/**
 * Cambio del proprio indirizzo email (US-110), confermato con un codice di 6 cifre inviato al nuovo indirizzo.
 * Opera sempre sull'utente della richiesta corrente.
 */
public interface EmailChangeService {

    /** Indirizzo attuale e cambio in corso. */
    EmailStatusDTO status();

    /**
     * Crea (o sostituisce) la richiesta di cambio e invia il codice al nuovo indirizzo.
     *
     * @throws it.nexus.web.errors.FieldValidationException indirizzo uguale all'attuale o già usato
     * @throws it.nexus.web.errors.BadRequestException nuovo codice chiesto prima di 60 secondi
     * @throws it.nexus.web.errors.ServiceUnavailableException l'email col codice non è partita
     */
    EmailStatusDTO request(String newEmail);

    /**
     * Conferma il cambio: l'indirizzo cambia in Keycloak e in NEXUS, si registra l'audit e si avvisa il vecchio
     * indirizzo. Un codice sbagliato consuma un tentativo.
     *
     * @throws it.nexus.web.errors.BadRequestException nessuna richiesta, codice sbagliato, scaduto o tentativi finiti
     * @throws it.nexus.web.errors.ServiceUnavailableException Keycloak non aggiornato
     */
    EmailStatusDTO confirm(String code);

    /** Annulla la richiesta in corso, se c'è. */
    EmailStatusDTO cancel();
}
