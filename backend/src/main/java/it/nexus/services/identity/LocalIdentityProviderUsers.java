package it.nexus.services.identity;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/** Senza Keycloak (sviluppo locale con utenti mock, test): non c'è nulla da aggiornare. */
@Slf4j
@Component
@Profile("!keycloak")
public class LocalIdentityProviderUsers implements IdentityProviderUsers {

    @Override
    public void updateEmail(String externalId, String email) {
        log.info("Identity provider assente (profilo senza keycloak): email aggiornata solo in NEXUS");
    }
}
