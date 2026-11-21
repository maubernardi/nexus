package it.nexus.jobs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.ApplicationProperties;
import it.nexus.config.PersistenceConfig;
import it.nexus.domain.AppUser;
import it.nexus.domain.AuditEvent;
import it.nexus.domain.TestEntities;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.Role;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.AuditEventRepository;
import it.nexus.services.identity.IdentityProviderException;
import it.nexus.services.identity.IdentityProviderUsers;
import it.nexus.services.identity.NewIdentityUser;

/**
 * Primo ADMIN della produzione (US-1306): dopo la pulizia della demo in NEXUS non c'è nessun utente; con i dati nel
 * {@code .env} l'ADMIN si crea come da «Utenti» (US-103), con Keycloak simulato.
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class AdminBootstrapJobIT {

    private static final String KC_ID = "22222222-3333-4444-8555-666666666666";

    @Autowired AdminBootstrapJob job;
    @Autowired ApplicationProperties properties;
    @Autowired AppUserRepository users;
    @Autowired AuditEventRepository audit;

    @MockitoBean IdentityProviderUsers identityProvider;

    private ApplicationProperties.Admin admin;

    @BeforeEach
    void productionWithoutUsers() {
        admin = properties.getBootstrap().getAdmin();
        admin.setUsername(" Mario.Rossi ");
        admin.setFirstName("Mario");
        admin.setLastName("Rossi");
        admin.setEmail("Mario.Rossi@Example.org");
        // come dopo la pulizia: nessun ADMIN (le modifiche si annullano alla fine del test)
        users.findAll().stream().filter(u -> u.getRole() == Role.ADMIN).forEach(u -> u.setRole(Role.TUTOR));
        users.flush();
        when(identityProvider.createUser(any())).thenReturn(KC_ID);
    }

    @AfterEach
    void emptyProperties() {
        admin.setUsername("");
        admin.setFirstName("");
        admin.setLastName("");
        admin.setEmail("");
    }

    @Test
    void senzaAdmin_loCreaInKeycloakEInNexus_conInvitoEAuditDiSistema() {
        assertThat(job.createIfMissing()).isTrue();

        verify(identityProvider).createUser(
                new NewIdentityUser("mario.rossi", "Mario", "Rossi", "mario.rossi@example.org", Role.ADMIN));
        verify(identityProvider).sendPasswordSetupEmail(eq(KC_ID), eq(Duration.ofDays(3)), anyString());
        AppUser created = users.findByUsername("mario.rossi").orElseThrow();
        assertThat(created.getRole()).isEqualTo(Role.ADMIN);
        assertThat(created.getExternalId()).isEqualTo(KC_ID);
        List<AuditEvent> events = audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.APP_USER,
                created.getId());
        assertThat(events).extracting(AuditEvent::getAction).containsExactly("CREATE", "INVITATION_SENT");
        assertThat(events).extracting(AuditEvent::getCreatedBy).containsOnly(PersistenceConfig.SYSTEM_AUDITOR);

        // il controllo successivo trova l'ADMIN e non fa altro
        assertThat(job.createIfMissing()).isTrue();
        verify(identityProvider).createUser(any());
    }

    @Test
    void conUnAdmin_nonFaNulla() {
        users.saveAndFlush(TestEntities.user("admin.esistente", Role.ADMIN, "00000000-0000-4000-8000-000000000009"));

        assertThat(job.createIfMissing()).isTrue();

        verify(identityProvider, never()).createUser(any());
    }

    @Test
    void datiIncompleti_nonCreaNulla() {
        admin.setEmail(" ");

        assertThat(job.createIfMissing()).isTrue();

        verify(identityProvider, never()).createUser(any());
        assertThat(users.findByUsername("mario.rossi")).isEmpty();
    }

    @Test
    void datiNonValidi_nonCreaNulla() {
        admin.setEmail("non-è-un-indirizzo");

        assertThat(job.createIfMissing()).isTrue();

        verify(identityProvider, never()).createUser(any());
    }

    @Test
    void keycloakNonRisponde_riprovaPoiLoCrea() {
        when(identityProvider.createUser(any()))
                .thenThrow(new IdentityProviderException("Keycloak non risponde", false, null))
                .thenReturn(KC_ID);

        assertThat(job.createIfMissing()).as("da ritentare").isFalse();
        assertThat(users.findByUsername("mario.rossi")).isEmpty();

        assertThat(job.createIfMissing()).isTrue();
        assertThat(users.findByUsername("mario.rossi")).isPresent();
    }
}
