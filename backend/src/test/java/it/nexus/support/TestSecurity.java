package it.nexus.support;

import java.util.Set;

import org.springframework.security.core.context.SecurityContextHolder;

import it.nexus.config.security.AuthenticatedUser;
import it.nexus.config.security.UserAuthenticationToken;
import it.nexus.domain.enumeration.Role;

/** Utente autenticato per i test che chiamano i service senza passare dai filtri HTTP. */
public final class TestSecurity {

    private TestSecurity() {
    }

    public static void authenticate(String username, Role... roles) {
        AuthenticatedUser user = new AuthenticatedUser("ext-" + username, username, "Nome", "Cognome",
                username + "@nexus.test", Set.of(roles));
        SecurityContextHolder.getContext().setAuthentication(new UserAuthenticationToken(user, null));
    }

    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}
