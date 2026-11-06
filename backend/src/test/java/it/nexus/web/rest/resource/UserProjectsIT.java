package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.security.MockHeaderAuthenticationFilter;
import it.nexus.domain.AppUser;
import it.nexus.domain.AuditEvent;
import it.nexus.domain.Project;
import it.nexus.domain.Ticket;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AuditEventRepository;
import it.nexus.repository.ProjectRepository;
import it.nexus.support.WorkflowScenarios;
import it.nexus.support.WorkflowScenarios.Actors;
import it.nexus.support.WorkflowScenarios.Place;

/** Progetti assegnati ai Tutor dall'ADMIN (US-104). */
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, WorkflowScenarios.class})
class UserProjectsIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;

    @Autowired MockMvc mockMvc;
    @Autowired WorkflowScenarios scenarios;
    @Autowired ProjectRepository projects;
    @Autowired AuditEventRepository audit;

    private Actors actors;

    @BeforeEach
    void setUp() {
        actors = scenarios.actors();
    }

    private static String id(AppUser user) {
        return TsidMapper.toExternal(user.getId());
    }

    private static String id(Project project) {
        return TsidMapper.toExternal(project.getId());
    }

    private ResultActions assign(String user, AppUser target, Project... chosen) throws Exception {
        String ids = Stream.of(chosen).map(p -> "\"" + id(p) + "\"").collect(Collectors.joining(","));
        return mockMvc.perform(put("/api/v1/users/" + id(target) + "/projects").header(USER, user)
                .contentType(MediaType.APPLICATION_JSON).content("{\"projectIds\": [" + ids + "]}"));
    }

    private ResultActions ticketAs(String user, Ticket ticket) throws Exception {
        return mockMvc.perform(get("/api/v1/tickets/" + TsidMapper.toExternal(ticket.getId())).header(USER, user));
    }

    private List<AuditEvent> events(AppUser user) {
        return audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.APP_USER, user.getId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void assegnatoUnProgetto_ilTutorNeVedeITicket_eLoTogliePoi() throws Exception {
        Project gol = scenarios.project("GOL", actors.tutor1(), actors.tutor2());
        Project polis = scenarios.project("POLIS", actors.tutor2());
        Place place = scenarios.place();
        Ticket ofTutor2 = scenarios.appointmentTicket(actors.tutor2(), polis,
                scenarios.freeSlot(place, "Magazziniere"), actors.operator());
        ticketAs("tutor1", ofTutor2).andExpect(status().isNotFound());

        assign("admin", actors.tutor1(), gol, polis)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projects[*].code", contains(gol.getCode(), polis.getCode())));

        // effetto immediato: ticket di POLIS in sola lettura e POLIS tra i progetti per segnalare
        ticketAs("tutor1", ofTutor2).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/me/projects").header(USER, "tutor1"))
                .andExpect(jsonPath("$[*].code", contains(gol.getCode(), polis.getCode())));
        mockMvc.perform(get("/api/v1/users/" + id(actors.tutor1())).header(USER, "admin"))
                .andExpect(jsonPath("$.projects[*].name", contains(gol.getName(), polis.getName())));
        Map<String, Object> change = (Map<String, Object>) events(actors.tutor1()).getLast().getChanges().get("projects");
        assertThat(events(actors.tutor1())).extracting(AuditEvent::getAction).containsExactly("PROJECTS_CHANGED");
        assertThat(change).containsEntry("before", List.of(gol.getCode()))
                .containsEntry("after", List.of(gol.getCode(), polis.getCode()));

        assign("admin", actors.tutor1(), gol).andExpect(status().isOk());
        ticketAs("tutor1", ofTutor2).andExpect(status().isNotFound());
        // il Tutor che ha segnalato continua a vedere il suo ticket anche senza il progetto
        assign("admin", actors.tutor2()).andExpect(jsonPath("$.projects", empty()));
        ticketAs("tutor2", ofTutor2).andExpect(status().isOk());
    }

    @Test
    void stessiProgetti_nessunaRigaDiAudit() throws Exception {
        Project gol = scenarios.project("GOL", actors.tutor1());
        assign("admin", actors.tutor1(), gol, gol).andExpect(status().isOk());
        assertThat(events(actors.tutor1())).isEmpty();
    }

    @Test
    void progettoDisattivato_nonSiAssegna_maSiToglie() throws Exception {
        Project gol = scenarios.project("GOL", actors.tutor1());
        Project old = scenarios.project("OLD", actors.tutor1());
        Project closed = scenarios.project("CLOSED");
        old.setActive(false);
        closed.setActive(false);
        projects.saveAllAndFlush(List.of(old, closed));

        assign("admin", actors.tutor1(), gol, old, closed)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("projectIds"));
        // il disattivato già assegnato può restare
        assign("admin", actors.tutor1(), gol, old).andExpect(status().isOk());
        assign("admin", actors.tutor1(), gol)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projects[*].code", contains(gol.getCode())));
    }

    @Test
    void soloPerITutor_eConProgettiEsistenti() throws Exception {
        Project gol = scenarios.project("GOL");
        assign("admin", actors.operator(), gol).andExpect(status().isBadRequest());
        assign("admin", actors.admin(), gol).andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/v1/users/" + id(actors.tutor1()) + "/projects").header(USER, "admin")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"projectIds\": [\"0000000000000\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("projectIds"));
        mockMvc.perform(put("/api/v1/users/" + id(actors.tutor1()) + "/projects").header(USER, "admin")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/v1/users/0000000000000/projects").header(USER, "admin")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"projectIds\": []}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void elencoDegliUtenti_conIProgetti() throws Exception {
        Project gol = scenarios.project("GOL", actors.tutor2());
        mockMvc.perform(get("/api/v1/users").header(USER, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username == 'tutor2')].projects[*].code", contains(gol.getCode())));
    }

    @Test
    void soloLAdmin() throws Exception {
        Project gol = scenarios.project("GOL");
        assign("tutor1", actors.tutor1(), gol).andExpect(status().isForbidden());
        assign("operatore.cc", actors.tutor1(), gol).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/users/" + id(actors.tutor1())).header(USER, "tutor1"))
                .andExpect(status().isForbidden());
    }
}
