package it.nexus.web.rest.resource;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.security.MockHeaderAuthenticationFilter;
import it.nexus.mapper.TsidMapper;
import it.nexus.services.identity.IdentityProviderException;
import it.nexus.services.identity.IdentityProviderException.Conflict;
import it.nexus.services.identity.IdentityProviderUsers;
import it.nexus.services.identity.LoginEvent;
import it.nexus.support.WorkflowScenarios;
import it.nexus.support.WorkflowScenarios.Actors;

/** Registro degli accessi (US-113), con il registro degli eventi di Keycloak simulato. */
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, WorkflowScenarios.class})
class AccessLogIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;
    private static final Instant AT = Instant.parse("2026-11-07T11:46:00Z");

    @Autowired MockMvc mockMvc;
    @Autowired WorkflowScenarios scenarios;

    @MockitoBean IdentityProviderUsers identityProvider;

    private Actors actors;

    @BeforeEach
    void setUp() {
        actors = scenarios.actors();
    }

    private static LoginEvent ok(String externalId, String ip) {
        return new LoginEvent(AT, true, externalId, "x", ip, null);
    }

    @Test
    void accessiDiUnUtente_perLAdmin_unaPaginaAllaVolta() throws Exception {
        String tutor = actors.tutor1().getExternalId();
        // 21 righe: la ventunesima dice solo che c'è un'altra pagina
        when(identityProvider.loginEvents(tutor, 20, 21)).thenReturn(IntStream.range(0, 21)
                .mapToObj(i -> ok(tutor, "203.0.113." + i)).toList());

        mockMvc.perform(get("/api/v1/users/" + TsidMapper.toExternal(actors.tutor1().getId()) + "/access-log")
                        .param("page", "1").header(USER, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries", hasSize(20)))
                .andExpect(jsonPath("$.hasMore").value(true))
                .andExpect(jsonPath("$.entries[0].at").value("2026-11-07T11:46:00Z"))
                .andExpect(jsonPath("$.entries[0].success").value(true))
                .andExpect(jsonPath("$.entries[0].ipAddress").value("203.0.113.0"));
    }

    @Test
    void iMieiAccessi_perOgniRuolo() throws Exception {
        String tutor = actors.tutor2().getExternalId();
        when(identityProvider.loginEvents(tutor, 0, 21)).thenReturn(List.of(ok(tutor, "203.0.113.5"),
                new LoginEvent(AT, false, tutor, "tutor2", "203.0.113.6", "invalid_user_credentials")));

        mockMvc.perform(get("/api/v1/me/access-log").header(USER, "tutor2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries", hasSize(2)))
                .andExpect(jsonPath("$.hasMore").value(false))
                .andExpect(jsonPath("$.entries[1].success").value(false))
                .andExpect(jsonPath("$.entries[1].error").value("invalid_user_credentials"));
        verify(identityProvider).loginEvents(tutor, 0, 21);
    }

    @Test
    void tentativiFalliti_conIlNomeDegliUtentiNexus() throws Exception {
        when(identityProvider.failedLogins(0, 21)).thenReturn(List.of(
                new LoginEvent(AT, false, null, "nessuno.cosi", "203.0.113.8", "user_not_found"),
                new LoginEvent(AT, false, actors.operator().getExternalId(), "operatore.cc", "203.0.113.7",
                        "invalid_user_credentials")));

        mockMvc.perform(get("/api/v1/access-log/failed").header(USER, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries[0].username").value("nessuno.cosi"))
                .andExpect(jsonPath("$.entries[0].userName").doesNotExist())
                .andExpect(jsonPath("$.entries[1].userName").value("Nome Cognome"))
                .andExpect(jsonPath("$.entries[1].error").value("invalid_user_credentials"));
    }

    @Test
    void keycloakNonRisponde_503() throws Exception {
        when(identityProvider.failedLogins(anyInt(), anyInt()))
                .thenThrow(new IdentityProviderException("giù", Conflict.NONE, null));
        when(identityProvider.loginEvents(anyString(), anyInt(), anyInt()))
                .thenThrow(new IdentityProviderException("giù", Conflict.NONE, null));

        mockMvc.perform(get("/api/v1/access-log/failed").header(USER, "admin"))
                .andExpect(status().isServiceUnavailable());
        mockMvc.perform(get("/api/v1/me/access-log").header(USER, "tutor1"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void paginaNonValida_eSoloLAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/me/access-log").param("page", "-1").header(USER, "tutor1"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/access-log/failed").header(USER, "operatore.cc"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/users/" + TsidMapper.toExternal(actors.tutor1().getId()) + "/access-log")
                .header(USER, "tutor2")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/users/0000000000000/access-log").header(USER, "admin"))
                .andExpect(status().isNotFound());
    }
}
