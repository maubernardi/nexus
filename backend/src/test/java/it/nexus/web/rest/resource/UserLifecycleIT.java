package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.security.MockHeaderAuthenticationFilter;
import it.nexus.domain.AppUser;
import it.nexus.domain.AuditEvent;
import it.nexus.domain.Project;
import it.nexus.domain.TestEntities;
import it.nexus.domain.Ticket;
import it.nexus.domain.UserProject;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.Role;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.AuditEventRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.UserProjectRepository;
import it.nexus.services.identity.IdentityProviderException;
import it.nexus.services.identity.IdentityProviderException.Conflict;
import it.nexus.services.identity.IdentityProviderUsers;
import it.nexus.support.WorkflowScenarios;
import it.nexus.support.WorkflowScenarios.Actors;
import it.nexus.support.WorkflowScenarios.Place;

/** Disattivazione, riattivazione e cambio di ruolo da parte dell'ADMIN (US-105), con Keycloak simulato. */
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, WorkflowScenarios.class})
class UserLifecycleIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;

    @Autowired MockMvc mockMvc;
    @Autowired WorkflowScenarios scenarios;
    @Autowired AppUserRepository users;
    @Autowired TicketRepository tickets;
    @Autowired AuditEventRepository audit;
    @Autowired UserProjectRepository userProjects;

    @MockitoBean IdentityProviderUsers identityProvider;

    private Actors actors;
    private Place place;
    private Project gol;

    @BeforeEach
    void setUp() {
        actors = scenarios.actors();
        place = scenarios.place();
        gol = scenarios.project("GOL", actors.tutor1(), actors.tutor2());
    }

    private static String id(AppUser user) {
        return TsidMapper.toExternal(user.getId());
    }

    private Ticket inChargeOfOperator(AppUser tutor, String title) {
        return scenarios.appointmentTicket(tutor, gol, scenarios.freeSlot(place, title), actors.operator());
    }

    private ResultActions setActive(String user, AppUser target, boolean active) throws Exception {
        return mockMvc.perform(put("/api/v1/users/" + id(target) + "/active").header(USER, user)
                .contentType(MediaType.APPLICATION_JSON).content("{\"active\": " + active + "}"));
    }

    private ResultActions changeRole(String user, AppUser target, Role role) throws Exception {
        return mockMvc.perform(put("/api/v1/users/" + id(target) + "/role").header(USER, user)
                .contentType(MediaType.APPLICATION_JSON).content("{\"role\": \"" + role + "\"}"));
    }

    private List<String> actions(AuditEntityType type, Long id) {
        return audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(type, id).stream()
                .map(AuditEvent::getAction).toList();
    }

    @Test
    void operatoreDisattivato_ticketInCoda_nonAccedePiu_poiRiattivato() throws Exception {
        // tutor2 non ha segnalazioni aperte: il ticket in carico all'operatore è di tutor1
        Ticket ticket = inChargeOfOperator(actors.tutor1(), "Magazziniere");
        mockMvc.perform(get("/api/v1/users/" + id(actors.operator()) + "/impact").header(USER, "admin"))
                .andExpect(jsonPath("$.ticketsInCharge").value(1))
                .andExpect(jsonPath("$.openTickets").value(0))
                .andExpect(jsonPath("$.self").value(false));

        setActive("admin", actors.operator(), false)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        verify(identityProvider).setEnabled(actors.operator().getExternalId(), false);
        verify(identityProvider).logout(actors.operator().getExternalId());
        assertThat(tickets.findById(ticket.getId()).orElseThrow().getAssignedCcOperator()).isNull();
        assertThat(actions(AuditEntityType.TICKET, ticket.getId())).contains("RELEASE");
        assertThat(actions(AuditEntityType.APP_USER, actors.operator().getId())).containsExactly("DEACTIVATE");
        // non accede più a NEXUS
        mockMvc.perform(get("/api/v1/me").header(USER, "operatore.cc")).andExpect(status().isForbidden());

        // il ticket, ad appuntamento fissato, è tornato in coda: un collega lo riprende senza cambiare stato
        String ticketId = TsidMapper.toExternal(ticket.getId());
        mockMvc.perform(get("/api/v1/tickets").param("view", "TO_TAKE").header(USER, "admin"))
                .andExpect(jsonPath("$[*].id", hasItem(ticketId)));
        mockMvc.perform(post("/api/v1/tickets/" + ticketId + "/take-charge").header(USER, "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\": " + tickets.findById(ticket.getId()).orElseThrow().getVersion() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(TicketStatus.APPUNTAMENTO.name()));
        mockMvc.perform(get("/api/v1/tickets/" + ticketId + "/history").header(USER, "admin"))
                .andExpect(jsonPath("$[?(@.action == 'RELEASE')].detail",
                        hasItem(containsString("non è più disponibile"))));

        setActive("admin", actors.operator(), true).andExpect(jsonPath("$.active").value(true));
        verify(identityProvider).setEnabled(actors.operator().getExternalId(), true);
        mockMvc.perform(get("/api/v1/me").header(USER, "operatore.cc")).andExpect(status().isOk());
    }

    @Test
    void tutorConSegnalazioniAperte_bloccato_finchéNonLeSposta() throws Exception {
        Ticket ticket = inChargeOfOperator(actors.tutor1(), "Magazziniere");

        setActive("admin", actors.tutor1(), false)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Ha 1 segnalazione aperta")));
        changeRole("admin", actors.tutor1(), Role.CALL_CENTER).andExpect(status().isBadRequest());
        verify(identityProvider, never()).setEnabled(anyString(), anyBoolean());
        verify(identityProvider, never()).changeRole(anyString(), any());

        mockMvc.perform(post("/api/v1/tickets/transfer").header(USER, "admin").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticketIds\": [\"%s\"], \"tutorId\": \"%s\"}".formatted(
                                TsidMapper.toExternal(ticket.getId()), id(actors.tutor2()))))
                .andExpect(status().isOk());
        setActive("admin", actors.tutor1(), false).andExpect(status().isOk());
    }

    @Test
    void cambioRuolo_inKeycloakEInNexus_eChiDiventaTutorLasciaITicket() throws Exception {
        Ticket ticket = inChargeOfOperator(actors.tutor2(), "Magazziniere");
        AppUser operator = actors.operator();

        changeRole("admin", operator, Role.TUTOR)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("TUTOR"));
        verify(identityProvider).changeRole(operator.getExternalId(), Role.TUTOR);
        verify(identityProvider).logout(operator.getExternalId());
        assertThat(users.findById(operator.getId()).orElseThrow().getRole()).isEqualTo(Role.TUTOR);
        assertThat(tickets.findById(ticket.getId()).orElseThrow().getAssignedCcOperator()).isNull();
        assertThat(actions(AuditEntityType.APP_USER, operator.getId())).containsExactly("ROLE_CHANGE");

        // da Call Center ad ADMIN i ticket in carico restano: anche l'ADMIN li lavora
        AppUser other = users.save(TestEntities.user("operatore.due", Role.CALL_CENTER));
        Ticket second = scenarios.appointmentTicket(actors.tutor2(), gol, scenarios.freeSlot(place, "Cuoco"), other);
        changeRole("admin", other, Role.ADMIN).andExpect(status().isOk());
        assertThat(tickets.findById(second.getId()).orElseThrow().getAssignedCcOperator()).isEqualTo(other);
    }

    @Test
    void chiNonEPiuTutor_nonCoordinaPiu() throws Exception {
        UserProject assignment = userProjects.findById(new UserProject.Id(actors.tutor2().getId(), gol.getId()))
                .orElseThrow();
        assignment.setCoordinator(true);
        userProjects.saveAndFlush(assignment);

        changeRole("admin", actors.tutor2(), Role.CALL_CENTER)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coordinatedProjects", hasSize(0)));
        assertThat(userProjects.findCoordinatedBy(actors.tutor2().getId())).isEmpty();
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.APP_USER,
                actors.tutor2().getId()).getLast().getChanges()).containsKey("coordinated");
    }

    @Test
    void lAdminNonToccaSeStesso() throws Exception {
        setActive("admin", actors.admin(), false)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("tuo utente")));
        changeRole("admin", actors.admin(), Role.TUTOR).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/users/" + id(actors.admin()) + "/impact").header(USER, "admin"))
                .andExpect(jsonPath("$.self").value(true));
    }

    @Test
    void keycloakNonRisponde_503() throws Exception {
        doThrow(new IdentityProviderException("giù", Conflict.NONE, null))
                .when(identityProvider).setEnabled(anyString(), anyBoolean());
        doThrow(new IdentityProviderException("giù", Conflict.NONE, null))
                .when(identityProvider).changeRole(anyString(), any());
        setActive("admin", actors.tutor2(), false).andExpect(status().isServiceUnavailable());
        changeRole("admin", actors.operator(), Role.ADMIN).andExpect(status().isServiceUnavailable());
    }

    @Test
    void stessoStatoOStessoRuolo_nullaDaFare() throws Exception {
        setActive("admin", actors.tutor2(), true).andExpect(status().isOk());
        changeRole("admin", actors.tutor2(), Role.TUTOR).andExpect(status().isOk());
        verify(identityProvider, never()).setEnabled(anyString(), anyBoolean());
        verify(identityProvider, never()).changeRole(anyString(), any());
        assertThat(actions(AuditEntityType.APP_USER, actors.tutor2().getId())).isEmpty();
    }

    @Test
    void soloLAdmin() throws Exception {
        setActive("operatore.cc", actors.tutor2(), false).andExpect(status().isForbidden());
        changeRole("tutor1", actors.tutor2(), Role.ADMIN).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/users/" + id(actors.tutor2()) + "/impact").header(USER, "tutor1"))
                .andExpect(status().isForbidden());
    }
}
