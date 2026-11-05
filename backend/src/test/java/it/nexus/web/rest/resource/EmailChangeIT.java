package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.security.MockHeaderAuthenticationFilter;
import it.nexus.domain.AppUser;
import it.nexus.domain.AuditEvent;
import it.nexus.domain.TestEntities;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.Role;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.AuditEventRepository;
import it.nexus.repository.EmailChangeRequestRepository;
import it.nexus.services.identity.IdentityProviderException;
import it.nexus.services.identity.IdentityProviderUsers;
import it.nexus.services.mail.EmailChangeMailer;
import it.nexus.services.mail.EmailChangedEvent;

/**
 * Cambio del proprio indirizzo email confermato con il codice (US-110). Senza transazione del test: si verifica anche
 * che una richiesta con invio fallito non resti salvata e che un tentativo sbagliato resti contato.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class EmailChangeIT {

    private static final String USER_HEADER = MockHeaderAuthenticationFilter.HEADER;
    private static final String TUTOR1_ID = "00000000-0000-4000-8000-000000000001";
    private static final String OPERATORE_ID = "00000000-0000-4000-8000-000000000003";
    private static final String NEW_EMAIL = "mario.rossi@example.org";

    @Autowired MockMvc mockMvc;
    @Autowired AppUserRepository users;
    @Autowired EmailChangeRequestRepository requests;
    @Autowired AuditEventRepository auditEvents;
    @Autowired JdbcTemplate jdbc;

    @MockitoBean EmailChangeMailer mailer;
    @MockitoBean IdentityProviderUsers identityProvider;

    private AppUser tutor;
    private AppUser operator;

    @BeforeEach
    void setUp() {
        tutor = users.save(TestEntities.user("tutor1", Role.TUTOR, TUTOR1_ID));
        operator = users.save(TestEntities.user("operatore.cc", Role.CALL_CENTER, OPERATORE_ID));
        when(mailer.sendCode(anyString(), anyString(), any())).thenReturn(true);
    }

    @AfterEach
    void cleanUp() {
        requests.deleteAll();
        users.deleteAll(List.of(tutor, operator));
        reset(mailer, identityProvider);
    }

    private ResultActions requestChange(String email) throws Exception {
        return mockMvc.perform(post("/api/v1/me/email-change").header(USER_HEADER, "tutor1")
                .contentType(MediaType.APPLICATION_JSON).content("{\"newEmail\":\"" + email + "\"}"));
    }

    private ResultActions confirm(String code) throws Exception {
        return mockMvc.perform(post("/api/v1/me/email-change/confirm").header(USER_HEADER, "tutor1")
                .contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"" + code + "\"}"));
    }

    /** Chiede il cambio e restituisce il codice inviato. */
    private String requestCode(String email) throws Exception {
        requestChange(email).andExpect(status().isOk());
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(mailer, org.mockito.Mockito.atLeastOnce()).sendCode(eq(email), code.capture(), any());
        return code.getValue();
    }

    private String wrong(String code) {
        return code.equals("000000") ? "111111" : "000000";
    }

    private String storedEmail() {
        return users.findById(tutor.getId()).orElseThrow().getEmail();
    }

    @Test
    void richiestaEConferma_cambiaInNexusEKeycloak_auditEAvvisoAlVecchio() throws Exception {
        mockMvc.perform(get("/api/v1/me/email").header(USER_HEADER, "tutor1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("tutor1@nexus.test"))
                .andExpect(jsonPath("$.pending").doesNotExist());

        // maiuscole e spazi: si salva come Keycloak, in minuscolo
        requestChange("  Mario.Rossi@Example.org ")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("tutor1@nexus.test"))
                .andExpect(jsonPath("$.pending.newEmail").value(NEW_EMAIL))
                .andExpect(jsonPath("$.pending.remainingAttempts").value(5))
                .andExpect(jsonPath("$.pending.expiresAt").exists())
                .andExpect(jsonPath("$.pending.resendAvailableAt").exists());
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Instant> expiresAt = ArgumentCaptor.forClass(Instant.class);
        verify(mailer).sendCode(eq(NEW_EMAIL), code.capture(), expiresAt.capture());
        assertThat(code.getValue()).matches("\\d{6}");
        assertThat(expiresAt.getValue()).isBetween(Instant.now().plusSeconds(29 * 60), Instant.now().plusSeconds(30 * 60));
        // il codice c'è solo come hash
        assertThat(requests.findByUserId(tutor.getId()).orElseThrow().getCodeHash())
                .startsWith("$2").doesNotContain(code.getValue());
        assertThat(storedEmail()).isEqualTo("tutor1@nexus.test");

        confirm(code.getValue())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(NEW_EMAIL))
                .andExpect(jsonPath("$.pending").doesNotExist());

        assertThat(storedEmail()).isEqualTo(NEW_EMAIL);
        assertThat(requests.findByUserId(tutor.getId())).isEmpty();
        verify(identityProvider).updateEmail(TUTOR1_ID, NEW_EMAIL);
        verify(mailer, timeout(5000)).onEmailChanged(new EmailChangedEvent(tutor.getId(), "tutor1@nexus.test"));
        List<AuditEvent> audit = auditEvents.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(
                AuditEntityType.APP_USER, tutor.getId());
        assertThat(audit).singleElement().satisfies(e -> {
            assertThat(e.getAction()).isEqualTo("EMAIL_CHANGED");
            assertThat(e.getCreatedBy()).isEqualTo("tutor1");
            assertThat(e.getChanges().toString()).doesNotContain(NEW_EMAIL).doesNotContain("tutor1@nexus.test");
        });
    }

    @Test
    void indirizzoNonValidoUgualeOGiaUsato_rifiutatoSulCampoSenzaCodice() throws Exception {
        requestChange("non-valido").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("newEmail"));
        requestChange("TUTOR1@nexus.test").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].message").value("È già il tuo indirizzo attuale"));
        requestChange("Operatore.CC@nexus.test").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Indirizzo già usato da un altro utente"));
        verify(mailer, never()).sendCode(anyString(), anyString(), any());
        assertThat(requests.findByUserId(tutor.getId())).isEmpty();
    }

    @Test
    void codiceSbagliato_contaITentativi_dopoCinqueNonValePiu() throws Exception {
        String code = requestCode(NEW_EMAIL);

        confirm(wrong(code)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Codice non valido: 4 tentativi rimasti"));
        mockMvc.perform(get("/api/v1/me/email").header(USER_HEADER, "tutor1"))
                .andExpect(jsonPath("$.pending.remainingAttempts").value(4));
        for (int i = 0; i < 3; i++) {
            confirm(wrong(code)).andExpect(status().isBadRequest());
        }
        confirm(wrong(code)).andExpect(jsonPath("$.message").value(containsString("Troppi tentativi")));

        // anche il codice giusto ormai non vale
        confirm(code).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Troppi tentativi sbagliati: chiedi un nuovo codice"));
        assertThat(storedEmail()).isEqualTo("tutor1@nexus.test");
        verify(identityProvider, never()).updateEmail(anyString(), anyString());
    }

    @Test
    void codiceScaduto_nonValePiu() throws Exception {
        String code = requestCode(NEW_EMAIL);
        jdbc.update("UPDATE email_change_request SET sent_at = now() - interval '31 minutes', "
                + "expires_at = now() - interval '1 minute'");

        confirm(code).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Il codice è scaduto: chiedine uno nuovo"));
        mockMvc.perform(get("/api/v1/me/email").header(USER_HEADER, "tutor1"))
                .andExpect(jsonPath("$.pending").doesNotExist());
        assertThat(storedEmail()).isEqualTo("tutor1@nexus.test");
    }

    @Test
    void nuovoCodice_soloDopo60Secondi_eIlPrecedenteNonValePiu() throws Exception {
        String first = requestCode(NEW_EMAIL);

        requestChange(NEW_EMAIL).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("puoi chiederne un altro tra")));

        jdbc.update("UPDATE email_change_request SET sent_at = sent_at - interval '61 seconds'");
        String second = requestCode("altro.indirizzo@example.org");
        if (!second.equals(first)) {
            confirm(first).andExpect(status().isBadRequest());
        }
        confirm(second).andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("altro.indirizzo@example.org"));
    }

    @Test
    void annullamento_ilCodiceNonValePiu() throws Exception {
        String code = requestCode(NEW_EMAIL);

        mockMvc.perform(delete("/api/v1/me/email-change").header(USER_HEADER, "tutor1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pending").doesNotExist());

        confirm(code).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nessun cambio di indirizzo in corso: chiedi un nuovo codice"));
        assertThat(storedEmail()).isEqualTo("tutor1@nexus.test");
    }

    @Test
    void invioFallito_laRichiestaNonResta() throws Exception {
        when(mailer.sendCode(anyString(), anyString(), any())).thenReturn(false);

        requestChange(NEW_EMAIL).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value(containsString("non è disponibile al momento")));
        assertThat(requests.findByUserId(tutor.getId())).isEmpty();
    }

    @Test
    void keycloakNonAggiornato_inNexusNonCambiaNulla() throws Exception {
        String code = requestCode(NEW_EMAIL);
        doThrow(new IdentityProviderException("giù", false, null))
                .when(identityProvider).updateEmail(anyString(), anyString());

        confirm(code).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value(containsString("l'indirizzo non è cambiato")));
        assertThat(storedEmail()).isEqualTo("tutor1@nexus.test");
        assertThat(auditEvents.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.APP_USER,
                tutor.getId())).isEmpty();
    }

    @Test
    void keycloakDiceGiaUsato_erroreSulCampo() throws Exception {
        String code = requestCode(NEW_EMAIL);
        doThrow(new IdentityProviderException("409", true, null))
                .when(identityProvider).updateEmail(anyString(), anyString());

        confirm(code).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Indirizzo già usato da un altro account"));
        assertThat(storedEmail()).isEqualTo("tutor1@nexus.test");
    }

    @Test
    void codiceNelFormatoSbagliato_rifiutatoSulCampo() throws Exception {
        requestCode(NEW_EMAIL);
        confirm("12ab").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("code"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Il codice è di 6 cifre"));
    }

    @Test
    void anonimo_401() throws Exception {
        mockMvc.perform(get("/api/v1/me/email")).andExpect(status().isUnauthorized());
    }
}
