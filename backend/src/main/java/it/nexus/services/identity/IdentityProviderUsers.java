package it.nexus.services.identity;

import java.time.Duration;

/**
 * Utenti dell'identity provider (Keycloak): gli aggiornamenti che NEXUS deve riportare anche lì. Gli errori sono
 * {@link IdentityProviderException}, con il motivo se un dato è già usato da un altro account.
 */
public interface IdentityProviderUsers {

    /**
     * Cambia l'indirizzo email dell'utente, già verificato da NEXUS con il codice (US-110).
     *
     * @param externalId soggetto dell'utente ({@code app_user.external_id})
     */
    void updateEmail(String externalId, String email);

    /**
     * Crea l'account con il ruolo indicato, senza password (US-103). Se il ruolo non si assegna, l'account creato si
     * cancella: o tutto o niente.
     *
     * @return il soggetto del nuovo account, da salvare in {@code app_user.external_id}
     */
    String createUser(NewIdentityUser user);

    /** Cancella l'account: serve solo ad annullare una creazione non completata in NEXUS (US-103). */
    void deleteUser(String externalId);

    /**
     * Invia all'utente l'email per impostare la password, con un collegamento valido per {@code lifespan}; dopo, il
     * collegamento porta a {@code redirectUri} (US-103).
     */
    void sendPasswordSetupEmail(String externalId, Duration lifespan, String redirectUri);
}
