package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.security.MockHeaderAuthenticationFilter;
import it.nexus.domain.AppUser;
import it.nexus.domain.AuditEvent;
import it.nexus.domain.EmailChangeRequest;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.AuditEventRepository;
import it.nexus.repository.EmailChangeRequestRepository;
import it.nexus.services.identity.IdentityProviderException;
import it.nexus.services.identity.IdentityProviderException.Conflict;
import it.nexus.services.identity.IdentityProviderUsers;
import it.nexus.services.identity.IdentityUserDetails;
import it.nexus.services.mail.EmailChangedEvent;
import it.nexus.support.WorkflowScenarios;
import it.nexus.support.WorkflowScenarios.Actors;

/** Correzione dei dati di un utente da parte dell'ADMIN (US-116), con Keycloak simulato. */
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@RecordApplicationEvents
@Import({TestcontainersConfiguration.class, WorkflowScenarios.class})
class UserDetailsIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;
    private static final String TUTOR2_KC = "00000000-0000-4000-8000-000000000002";

    @Autowired MockMvc mockMvc;
    @Autowired WorkflowScenarios scenarios;
    @Autowired AppUserRepository users;
    @Autowired AuditEventRepository audit;
    @Autowired EmailChangeRequestRepository emailChangeRequests;
    @Autowired ApplicationEvents events;

    @MockitoBean IdentityProviderUsers identityProvider;

    private Actors actors;

    @BeforeEach
    void setUp() {
        actors = scenarios.actors();
    }

    private static String id(AppUser user) {
        return TsidMapper.toExternal(user.getId());
    }

    private ResultActions details(String user, AppUser target) throws Exception {
        return mockMvc.perform(get("/api/v1/users/" + id(target) + "/details").header(USER, user));
    }

    private ResultActions update(String user, AppUser target, String username, String lastName, String email,
            String phone) throws Exception {
        String json = """
                {"username": "%s", "firstName": "Nome", "lastName": "%s", "email": "%s", "phone": %s}
                """.formatted(username, lastName, email, phone == null ? "null" : "\"" + phone + "\"");
        return mockMvc.perform(put("/api/v1/users/" + id(target) + "/details").header(USER, user)
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private List<AuditEvent> auditOf(AppUser user) {
        return audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.APP_USER, user.getId());
    }

    @Test
    void usernameSbagliato_diUnUtenteMaiEntrato_correttoInNexusEInKeycloak_conLAudit() throws Exception {
        when(identityProvider.hasPassword(TUTOR2_KC)).thenReturn(false);
        details("admin", actors.tutor2())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("tutor2"))
                .andExpect(jsonPath("$.email").value("tutor2@nexus.test"))
                .andExpect(jsonPath("$.usernameEditable").value(true));

        update("admin", actors.tutor2(), " Tutor2.Corretto ", "Cognome", "tutor2@nexus.test", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("tutor2.corretto"))
                .andExpect(jsonPath("$.user.usernameEditable").value(true))
                // l'invito già inviato resta valido: non ne parte un altro
                .andExpect(jsonPath("$.invitationSent").doesNotExist());

        verify(identityProvider).updateDetails(TUTOR2_KC,
                new IdentityUserDetails("tutor2.corretto", "Nome", "Cognome", "tutor2@nexus.test"));
        verify(identityProvider, never()).sendPasswordSetupEmail(anyString(), any(), anyString());
        assertThat(users.findById(actors.tutor2().getId()).orElseThrow().getUsername()).isEqualTo("tutor2.corretto");
        assertThat(auditOf(actors.tutor2())).singleElement().satisfies(e -> {
            assertThat(e.getAction()).isEqualTo("DETAILS_CHANGED");
            assertThat(e.getChanges().toString()).contains("tutor2", "tutor2.corretto");
        });
    }

    @Test
    void utenteGiaEntrato_usernameInSolaLettura_gliAltriDatiSiCorreggonoSenzaValori() throws Exception {
        when(identityProvider.hasPassword(TUTOR2_KC)).thenReturn(true);
        details("admin", actors.tutor2()).andExpect(jsonPath("$.usernameEditable").value(false));

        update("admin", actors.tutor2(), "tutor2.nuovo", "Cognome", "tutor2@nexus.test", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("username"))
                .andExpect(jsonPath("$.fieldErrors[0].message")
                        .value("È già entrato in NEXUS: lo username non si può più cambiare"));
        verify(identityProvider, never()).updateDetails(anyString(), any());

        // stesso username, anche con le maiuscole: non è un cambio
        update("admin", actors.tutor2(), "TUTOR2", "Bianchi", "tutor2@nexus.test", "055 987654")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("tutor2"))
                .andExpect(jsonPath("$.user.lastName").value("Bianchi"))
                .andExpect(jsonPath("$.user.phone").value("055 987654"));
        verify(identityProvider).updateDetails(TUTOR2_KC,
                new IdentityUserDetails("tutor2", "Nome", "Bianchi", "tutor2@nexus.test"));
        assertThat(auditOf(actors.tutor2())).singleElement().satisfies(e -> assertThat(e.getChanges().toString())
                .contains("lastName", "phone").doesNotContain("Bianchi", "987654", "username"));
    }

    @Test
    void emailDiUnUtenteMaiEntrato_nuovoInvitoAlNuovoIndirizzo() throws Exception {
        when(identityProvider.hasPassword(TUTOR2_KC)).thenReturn(false);

        update("admin", actors.tutor2(), "tutor2", "Cognome", " Tutor2.Giusta@Example.org ", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("tutor2.giusta@example.org"))
                .andExpect(jsonPath("$.invitationSent").value(true));

        verify(identityProvider).sendPasswordSetupEmail(TUTOR2_KC, Duration.ofDays(3), "http://localhost:5173/");
        assertThat(auditOf(actors.tutor2())).extracting(AuditEvent::getAction)
                .containsExactly("DETAILS_CHANGED", "INVITATION_SENT");
        assertThat(auditOf(actors.tutor2()).getFirst().getChanges().toString()).doesNotContain("example.org");
        assertThat(events.stream(EmailChangedEvent.class)).isEmpty();
    }

    @Test
    void emailDiUnUtenteMaiEntrato_invitoNonPartito_correzioneFattaComunque() throws Exception {
        when(identityProvider.hasPassword(TUTOR2_KC)).thenReturn(false);
        doThrow(new IdentityProviderException("smtp", Conflict.NONE, null))
                .when(identityProvider).sendPasswordSetupEmail(anyString(), any(), anyString());

        update("admin", actors.tutor2(), "tutor2", "Cognome", "tutor2.giusta@example.org", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invitationSent").value(false));
        assertThat(users.findById(actors.tutor2().getId()).orElseThrow().getEmail())
                .isEqualTo("tutor2.giusta@example.org");
    }

    @Test
    void emailDiUnUtenteGiaEntrato_avvisoAlVecchioIndirizzo_eRichiestaInAttesaAnnullata() throws Exception {
        when(identityProvider.hasPassword(TUTOR2_KC)).thenReturn(true);
        emailChangeRequests.saveAndFlush(new EmailChangeRequest(actors.tutor2(), "scelta.da.lui@example.org", "hash",
                Instant.now(), Instant.now().plus(Duration.ofMinutes(30))));

        update("admin", actors.tutor2(), "tutor2", "Cognome", "tutor2.nuova@example.org", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invitationSent").doesNotExist());

        verify(identityProvider, never()).sendPasswordSetupEmail(anyString(), any(), anyString());
        assertThat(events.stream(EmailChangedEvent.class)).singleElement()
                .isEqualTo(new EmailChangedEvent(actors.tutor2().getId(), "tutor2@nexus.test"));
        assertThat(emailChangeRequests.findByUserId(actors.tutor2().getId())).isEmpty();
    }

    @Test
    void datiGiaUsati_inNexusOInKeycloak_erroreSulCampo() throws Exception {
        when(identityProvider.hasPassword(TUTOR2_KC)).thenReturn(false);
        update("admin", actors.tutor2(), "tutor1", "Cognome", "tutor2@nexus.test", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("username"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Username già usato da un altro utente"));
        update("admin", actors.tutor2(), "tutor2", "Cognome", "ADMIN@nexus.test", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("email"));

        doThrow(new IdentityProviderException("409", Conflict.USERNAME, null))
                .when(identityProvider).updateDetails(anyString(), any());
        update("admin", actors.tutor2(), "solo.in.keycloak", "Cognome", "tutor2@nexus.test", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("username"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Username già usato da un account di accesso"));
    }

    @Test
    void keycloakNonRisponde_503() throws Exception {
        when(identityProvider.hasPassword(TUTOR2_KC)).thenThrow(new IdentityProviderException("giù", Conflict.NONE,
                null));
        details("admin", actors.tutor2()).andExpect(status().isServiceUnavailable());
        update("admin", actors.tutor2(), "tutor2", "Altro", "tutor2@nexus.test", null)
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void nessunCambiamento_nienteKeycloakNeAudit() throws Exception {
        when(identityProvider.hasPassword(TUTOR2_KC)).thenReturn(true);
        update("admin", actors.tutor2(), "tutor2", "Cognome", "tutor2@nexus.test", null).andExpect(status().isOk());
        verify(identityProvider, never()).updateDetails(anyString(), any());
        assertThat(auditOf(actors.tutor2())).isEmpty();
    }

    @Test
    void datiNonValidi_400() throws Exception {
        update("admin", actors.tutor2(), "no", "", "non-una-email", "abc").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.length()").value(4));
    }

    @Test
    void soloLAdmin() throws Exception {
        details("tutor1", actors.tutor2()).andExpect(status().isForbidden());
        update("operatore.cc", actors.tutor2(), "tutor2", "Cognome", "tutor2@nexus.test", null)
                .andExpect(status().isForbidden());
    }
}
