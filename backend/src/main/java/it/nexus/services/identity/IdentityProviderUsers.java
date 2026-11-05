package it.nexus.services.identity;

/** Utenti dell'identity provider (Keycloak): gli aggiornamenti che NEXUS deve riportare anche lì. */
public interface IdentityProviderUsers {

    /**
     * Cambia l'indirizzo email dell'utente, già verificato da NEXUS con il codice.
     *
     * @param externalId soggetto dell'utente ({@code app_user.external_id})
     * @throws IdentityProviderException se l'indirizzo è già usato o l'identity provider non risponde
     */
    void updateEmail(String externalId, String email);
}
