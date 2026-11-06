package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.security.MockHeaderAuthenticationFilter;
import it.nexus.domain.AppUser;
import it.nexus.domain.AuditEvent;
import it.nexus.domain.Notification;
import it.nexus.domain.Project;
import it.nexus.domain.TestEntities;
import it.nexus.domain.Ticket;
import it.nexus.domain.UserProject;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.NotificationType;
import it.nexus.domain.enumeration.Role;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.AuditEventRepository;
import it.nexus.repository.NotificationRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.UserProjectRepository;
import it.nexus.support.WorkflowScenarios;
import it.nexus.support.WorkflowScenarios.Actors;
import it.nexus.support.WorkflowScenarios.Place;

/**
 * Spostamento delle segnalazioni aperte da un Tutor a un altro dello stesso progetto (US-111), anche da parte del
 * Coordinatore del progetto (US-112).
 */
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, WorkflowScenarios.class})
class TicketTransferIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;

    @Autowired MockMvc mockMvc;
    @Autowired WorkflowScenarios scenarios;
    @Autowired TicketRepository tickets;
    @Autowired AppUserRepository users;
    @Autowired AuditEventRepository audit;
    @Autowired NotificationRepository notifications;
    @Autowired UserProjectRepository userProjects;

    private Actors actors;
    private Place place;
    private Project gol;

    @BeforeEach
    void setUp() {
        actors = scenarios.actors();
        place = scenarios.place();
        gol = scenarios.project("GOL", actors.tutor1(), actors.tutor2());
    }

    private Ticket ticket(AppUser tutor, Project project, String title) {
        return scenarios.appointmentTicket(tutor, project, scenarios.freeSlot(place, title), actors.operator());
    }

    private static String id(Ticket ticket) {
        return TsidMapper.toExternal(ticket.getId());
    }

    private ResultActions transfer(String user, AppUser target, Ticket... chosen) throws Exception {
        String ids = Stream.of(chosen).map(t -> "\"" + id(t) + "\"").collect(Collectors.joining(","));
        return mockMvc.perform(post("/api/v1/tickets/transfer").header(USER, user)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ticketIds\": [%s], \"tutorId\": \"%s\"}".formatted(ids,
                        TsidMapper.toExternal(target.getId()))));
    }

    private List<String> actions(AuditEntityType type, Long id) {
        return audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(type, id).stream()
                .map(AuditEvent::getAction).toList();
    }

    @Test
    void segnalazioniAperte_perProgetto_conITutorCheLePossonoRicevere() throws Exception {
        Project polis = scenarios.project("POLIS", actors.tutor1());
        Ticket a = ticket(actors.tutor1(), gol, "A");
        Ticket b = ticket(actors.tutor1(), polis, "B");
        Ticket closed = ticket(actors.tutor1(), gol, "C");
        closed.setStatus(TicketStatus.CLOSED);
        tickets.saveAndFlush(closed);
        ticket(actors.tutor2(), gol, "D");

        mockMvc.perform(get("/api/v1/users/" + TsidMapper.toExternal(actors.tutor1().getId()) + "/open-tickets")
                        .header(USER, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].project.code").value(gol.getCode()))
                .andExpect(jsonPath("$[0].tickets[*].id", contains(id(a))))
                // il Tutor stesso non è tra quelli che ricevono
                .andExpect(jsonPath("$[0].targets[*].id", contains(TsidMapper.toExternal(actors.tutor2().getId()))))
                .andExpect(jsonPath("$[1].tickets[*].id", contains(id(b))))
                .andExpect(jsonPath("$[1].targets", hasSize(0)));
    }

    @Test
    void spostamento_conBeneficiario_auditCronologiaENotifica() throws Exception {
        Ticket a = ticket(actors.tutor1(), gol, "A");
        Ticket b = ticket(actors.tutor1(), gol, "B");

        transfer("admin", actors.tutor2(), a, b)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moved").value(2));

        for (Ticket t : List.of(a, b)) {
            Ticket moved = tickets.findById(t.getId()).orElseThrow();
            assertThat(moved.getTutor().getId()).isEqualTo(actors.tutor2().getId());
            assertThat(moved.getBeneficiary().getOwnerTutor().getId()).isEqualTo(actors.tutor2().getId());
            assertThat(actions(AuditEntityType.TICKET, t.getId())).contains("TRANSFER");
            assertThat(actions(AuditEntityType.BENEFICIARY, moved.getBeneficiary().getId())).containsExactly("TRANSFER");
        }
        mockMvc.perform(get("/api/v1/tickets/" + id(a) + "/history").header(USER, "tutor2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.action == 'TRANSFER')].detail", contains("Nome Cognome → Nome Cognome")));
        List<Notification> received = notifications.findByRecipientIdOrderByCreatedAtDescIdDesc(
                actors.tutor2().getId(), Pageable.unpaged());
        assertThat(received).extracting(Notification::getType)
                .containsExactly(NotificationType.TICKET_TRANSFERRED, NotificationType.TICKET_TRANSFERRED);
        // il vecchio Tutor, ancora assegnato a GOL, la vede in sola lettura come le altre del progetto
        mockMvc.perform(get("/api/v1/tickets/" + id(a)).header(USER, "tutor1")).andExpect(status().isOk());
    }

    @Test
    void tutorNonValido_nullaCambia() throws Exception {
        Ticket a = ticket(actors.tutor1(), gol, "A");
        AppUser outside = users.save(TestEntities.user("tutor3", Role.TUTOR));
        AppUser inactive = users.save(TestEntities.user("tutor4", Role.TUTOR));
        inactive.setActive(false);
        users.saveAndFlush(inactive);

        transfer("admin", outside, a).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("tutorId"));
        transfer("admin", inactive, a).andExpect(status().isBadRequest());
        transfer("admin", actors.operator(), a).andExpect(status().isBadRequest());
        transfer("admin", actors.tutor1(), a).andExpect(status().isBadRequest());
        assertThat(tickets.findById(a.getId()).orElseThrow().getTutor().getId()).isEqualTo(actors.tutor1().getId());
        assertThat(actions(AuditEntityType.TICKET, a.getId())).doesNotContain("TRANSFER");
    }

    @Test
    void chiusaODiProgettiDiversi_nullaCambia() throws Exception {
        Project polis = scenarios.project("POLIS", actors.tutor1(), actors.tutor2());
        Ticket a = ticket(actors.tutor1(), gol, "A");
        Ticket b = ticket(actors.tutor1(), polis, "B");
        Ticket closed = ticket(actors.tutor1(), gol, "C");
        closed.setStatus(TicketStatus.CLOSED);
        tickets.saveAndFlush(closed);

        transfer("admin", actors.tutor2(), a, b).andExpect(status().isBadRequest());
        transfer("admin", actors.tutor2(), a, closed).andExpect(status().isBadRequest());
        assertThat(tickets.findById(a.getId()).orElseThrow().getTutor().getId()).isEqualTo(actors.tutor1().getId());
    }

    private void coordinate(AppUser tutor, Project project) {
        UserProject assignment = userProjects.findById(new UserProject.Id(tutor.getId(), project.getId())).orElseThrow();
        assignment.setCoordinator(true);
        userProjects.saveAndFlush(assignment);
    }

    @Test
    void coordinatore_spostaTraITutorDelProgettoCheCoordina() throws Exception {
        Project polis = scenarios.project("POLIS", actors.tutor1(), actors.tutor2());
        coordinate(actors.tutor1(), gol);
        Ticket ofTutor2 = ticket(actors.tutor2(), gol, "A");
        Ticket mine = ticket(actors.tutor1(), gol, "B");
        Ticket otherProject = ticket(actors.tutor2(), polis, "C");

        // tutte le segnalazioni aperte del progetto coordinato; il Coordinatore stesso può riceverle
        mockMvc.perform(get("/api/v1/me/coordination").header(USER, "tutor1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].project.code").value(gol.getCode()))
                .andExpect(jsonPath("$[0].tickets[*].id", contains(id(ofTutor2), id(mine))))
                .andExpect(jsonPath("$[0].targets", hasSize(2)));
        mockMvc.perform(get("/api/v1/me").header(USER, "tutor1")).andExpect(jsonPath("$.coordinator").value(true));

        transfer("tutor1", actors.tutor1(), ofTutor2).andExpect(status().isOk());
        assertThat(tickets.findById(ofTutor2.getId()).orElseThrow().getTutor().getId())
                .isEqualTo(actors.tutor1().getId());
        transfer("tutor1", actors.tutor2(), mine).andExpect(status().isOk());

        // in POLIS è solo Tutor: non sposta
        transfer("tutor1", actors.tutor1(), otherProject).andExpect(status().isForbidden());
        assertThat(tickets.findById(otherProject.getId()).orElseThrow().getTutor().getId())
                .isEqualTo(actors.tutor2().getId());
        mockMvc.perform(get("/api/v1/me/coordination").header(USER, "tutor2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void soloLAdminOIlCoordinatore() throws Exception {
        Ticket a = ticket(actors.tutor1(), gol, "A");
        transfer("tutor1", actors.tutor2(), a).andExpect(status().isForbidden());
        transfer("operatore.cc", actors.tutor2(), a).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/users/" + TsidMapper.toExternal(actors.tutor1().getId()) + "/open-tickets")
                .header(USER, "tutor2")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/me/coordination").header(USER, "operatore.cc")).andExpect(status().isForbidden());
    }
}
