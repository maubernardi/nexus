package it.nexus.services.identity;

import java.time.Duration;
import java.util.List;

import it.nexus.domain.enumeration.Role;

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

    /** Abilita o disabilita l'account: disabilitato non accede più (US-105). */
    void setEnabled(String externalId, boolean enabled);

    /**
     * Sostituisce il ruolo NEXUS dell'account ({@code TUTOR|CALL_CENTER|ADMIN}) con quello indicato; gli altri ruoli del
     * realm restano (US-105). Se il vecchio ruolo non si toglie, il nuovo si ritira: o tutto o niente.
     */
    void changeRole(String externalId, Role role);

    /** Chiude le sessioni aperte: al prossimo accesso valgono stato e ruolo nuovi (US-105). */
    void logout(String externalId);

    /**
     * Accessi riusciti e falliti dell'account, dal più recente; Keycloak li conserva 12 mesi (US-113).
     *
     * @param first quanti saltarne, per le pagine successive
     */
    List<LoginEvent> loginEvents(String externalId, int first, int max);

    /** Tentativi di accesso falliti di tutti, anche con uno username inesistente, dal più recente (US-113). */
    List<LoginEvent> failedLogins(int first, int max);
}
