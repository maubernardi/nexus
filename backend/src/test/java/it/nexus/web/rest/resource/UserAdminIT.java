package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.security.MockHeaderAuthenticationFilter;
import it.nexus.domain.AppUser;
import it.nexus.domain.AuditEvent;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.Role;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.AuditEventRepository;
import it.nexus.services.identity.IdentityProviderException;
import it.nexus.services.identity.IdentityProviderException.Conflict;
import it.nexus.services.identity.IdentityProviderUsers;
import it.nexus.services.identity.NewIdentityUser;
import it.nexus.support.WorkflowScenarios;
import it.nexus.support.WorkflowScenarios.Actors;

/** Creazione degli utenti da parte dell'ADMIN (US-103), con Keycloak simulato. */
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, WorkflowScenarios.class})
class UserAdminIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;
    private static final String USERS = "/api/v1/users";
    private static final String KC_ID = "11111111-2222-4333-8444-555555555555";

    @Autowired MockMvc mockMvc;
    @Autowired WorkflowScenarios scenarios;
    @Autowired AppUserRepository users;
    @Autowired AuditEventRepository audit;

    @MockitoBean IdentityProviderUsers identityProvider;

    private Actors actors;

    @BeforeEach
    void setUp() {
        actors = scenarios.actors();
        when(identityProvider.createUser(any())).thenReturn(KC_ID);
    }

    private ResultActions create(String user, String json) throws Exception {
        return mockMvc.perform(post(USERS).header(USER, user).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private static String newUser(String username, String email) {
        return """
                {"username": "%s", "firstName": " Mario ", "lastName": "Rossi", "email": "%s",
                 "phone": "055 123456", "role": "CALL_CENTER"}
                """.formatted(username, email);
    }

    private List<String> actions(String id) {
        return audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.APP_USER,
                TsidMapper.toInternal(id)).stream().map(AuditEvent::getAction).toList();
    }

    @Test
    void nuovoUtente_inKeycloakEInNexusConLoStessoIdentificativo_invitoEAudit() throws Exception {
        String body = create("admin", newUser(" Mario.Rossi ", "Mario.Rossi@Example.org"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.invitationSent").value(true))
                .andExpect(jsonPath("$.user.username").value("mario.rossi"))
                .andExpect(jsonPath("$.user.email").value("mario.rossi@example.org"))
                .andExpect(jsonPath("$.user.firstName").value("Mario"))
                .andExpect(jsonPath("$.user.role").value("CALL_CENTER"))
                .andExpect(jsonPath("$.user.active").value(true))
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(body, "$.user.id");

        ArgumentCaptor<NewIdentityUser> sent = ArgumentCaptor.forClass(NewIdentityUser.class);
        verify(identityProvider).createUser(sent.capture());
        assertThat(sent.getValue()).isEqualTo(new NewIdentityUser("mario.rossi", "Mario", "Rossi",
                "mario.rossi@example.org", Role.CALL_CENTER));
        AppUser saved = users.findById(TsidMapper.toInternal(id)).orElseThrow();
        assertThat(saved.getExternalId()).isEqualTo(KC_ID);
        assertThat(saved.getPhone()).isEqualTo("055 123456");
        // collegamento valido 3 giorni, che riporta all'app
        verify(identityProvider).sendPasswordSetupEmail(KC_ID, Duration.ofDays(3), "http://localhost:5173/");
        assertThat(actions(id)).containsExactly("CREATE", "INVITATION_SENT");
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.APP_USER,
                TsidMapper.toInternal(id)).getFirst().getChanges().toString())
                .contains("mario.rossi", "CALL_CENTER").doesNotContain("Rossi", "example.org", "055");

        mockMvc.perform(get(USERS).header(USER, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].username", hasItem("mario.rossi")));
    }

    @Test
    void emailPerLaPasswordNonPartita_utenteCreatoComunque_ePoiReinvio() throws Exception {
        doThrow(new IdentityProviderException("smtp", Conflict.NONE, null))
                .when(identityProvider).sendPasswordSetupEmail(anyString(), any(), anyString());

        String id = JsonPath.read(create("admin", newUser("mario.rossi", "mario@example.org"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.invitationSent").value(false))
                .andReturn().getResponse().getContentAsString(), "$.user.id");
        mockMvc.perform(post(USERS + "/" + id + "/invitation").header(USER, "admin"))
                .andExpect(status().isServiceUnavailable());

        // il server di posta torna disponibile
        reset(identityProvider);
        mockMvc.perform(post(USERS + "/" + id + "/invitation").header(USER, "admin"))
                .andExpect(status().isNoContent());
        verify(identityProvider).sendPasswordSetupEmail(eq(KC_ID), eq(Duration.ofDays(3)), anyString());
        assertThat(actions(id)).containsExactly("CREATE", "INVITATION_SENT");
    }

    @Test
    void usernameOEmailGiaUsatiInNexus_rifiutatiSulCampo_senzaToccareKeycloak() throws Exception {
        create("admin", newUser("TUTOR1", "nuovo@example.org"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("username"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Username già usato da un altro utente"));
        create("admin", newUser("nuovo.utente", actors.operator().getEmail().toUpperCase()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("email"));
        verify(identityProvider, never()).createUser(any());
    }

    @Test
    void giaUsatiInKeycloak_rifiutatiSulCampo() throws Exception {
        when(identityProvider.createUser(any()))
                .thenThrow(new IdentityProviderException("409", Conflict.USERNAME, null))
                .thenThrow(new IdentityProviderException("409", Conflict.EMAIL, null))
                .thenThrow(new IdentityProviderException("giù", Conflict.NONE, null));

        create("admin", newUser("mario.rossi", "mario@example.org"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("username"));
        create("admin", newUser("mario.rossi", "mario@example.org"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("email"));
        create("admin", newUser("mario.rossi", "mario@example.org"))
                .andExpect(status().isServiceUnavailable());
        assertThat(users.findByUsername("mario.rossi")).isEmpty();
    }

    @Test
    void nexusNonSalva_lAccountKeycloakSiCancella() throws Exception {
        // Keycloak restituisce un identificativo già in uso in NEXUS: il salvataggio viola uq_app_user_external_id
        when(identityProvider.createUser(any())).thenReturn(actors.tutor1().getExternalId());

        create("admin", newUser("mario.rossi", "mario@example.org")).andExpect(status().isConflict());

        verify(identityProvider).deleteUser(actors.tutor1().getExternalId());
        verify(identityProvider, never()).sendPasswordSetupEmail(anyString(), any(), anyString());
    }

    @Test
    void datiNonValidi_erroriSuiCampi() throws Exception {
        create("admin", """
                {"username": "mario rossi", "firstName": "", "lastName": "Rossi", "email": "non-valida",
                 "phone": "abc", "role": null}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field",
                        containsInAnyOrder("username", "firstName", "email", "phone", "role")));
        verify(identityProvider, never()).createUser(any());
    }

    @Test
    void soloLAdmin() throws Exception {
        mockMvc.perform(get(USERS).header(USER, "operatore.cc")).andExpect(status().isForbidden());
        create("tutor1", newUser("mario.rossi", "mario@example.org")).andExpect(status().isForbidden());
        mockMvc.perform(post(USERS + "/" + TsidMapper.toExternal(actors.tutor2().getId()) + "/invitation")
                .header(USER, "operatore.cc")).andExpect(status().isForbidden());
    }

    @Test
    void reinvioPerUnUtenteDisattivatoOInesistente() throws Exception {
        AppUser tutor2 = actors.tutor2();
        tutor2.setActive(false);
        users.saveAndFlush(tutor2);
        mockMvc.perform(post(USERS + "/" + TsidMapper.toExternal(tutor2.getId()) + "/invitation").header(USER, "admin"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(USERS + "/0000000000000/invitation").header(USER, "admin"))
                .andExpect(status().isNotFound());
    }
}
