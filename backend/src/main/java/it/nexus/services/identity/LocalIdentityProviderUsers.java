package it.nexus.services.identity;

import java.time.Duration;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Senza Keycloak (sviluppo locale con utenti mock, test): non c'è nulla da aggiornare. Un utente creato qui esiste solo
 * in NEXUS e non può accedere con gli utenti mock.
 */
@Slf4j
@Component
@Profile("!keycloak")
public class LocalIdentityProviderUsers implements IdentityProviderUsers {

    @Override
    public void updateEmail(String externalId, String email) {
        log.info("Identity provider assente (profilo senza keycloak): email aggiornata solo in NEXUS");
    }

    @Override
    public String createUser(NewIdentityUser user) {
        log.info("Identity provider assente (profilo senza keycloak): utente creato solo in NEXUS");
        return UUID.randomUUID().toString();
    }

    @Override
    public void deleteUser(String externalId) {
        // nulla da annullare
    }

    @Override
    public void sendPasswordSetupEmail(String externalId, Duration lifespan, String redirectUri) {
        log.info("Identity provider assente (profilo senza keycloak): nessuna email per la password");
    }
}
