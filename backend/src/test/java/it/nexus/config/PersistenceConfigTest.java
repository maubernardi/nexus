package it.nexus.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;

import it.nexus.config.security.AuthenticatedUser;
import it.nexus.config.security.UserAuthenticationToken;
import it.nexus.domain.enumeration.Role;

class PersistenceConfigTest {

    private final AuditorAware<String> auditorAware = new PersistenceConfig().auditorProvider();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void auditor_isUsername_whenAuthenticated() {
        AuthenticatedUser user = new AuthenticatedUser("id-1", "tutor1", "Tutor", "Uno", "t@x.it", Set.of(Role.TUTOR));
        SecurityContextHolder.getContext().setAuthentication(new UserAuthenticationToken(user, null));

        assertThat(auditorAware.getCurrentAuditor()).contains("tutor1");
    }

    @Test
    void auditor_isSystem_whenNoUserAuthenticated() {
        assertThat(auditorAware.getCurrentAuditor()).contains(PersistenceConfig.SYSTEM_AUDITOR);
    }
}
