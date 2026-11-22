package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
import it.nexus.config.TimeConfig;
import it.nexus.config.security.MockHeaderAuthenticationFilter;
import it.nexus.domain.Beneficiary;
import it.nexus.domain.Internship;
import it.nexus.domain.Project;
import it.nexus.domain.TestEntities;
import it.nexus.domain.Ticket;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.NotificationType;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AuditEventRepository;
import it.nexus.repository.InternshipRepository;
import it.nexus.repository.NotificationRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.services.InternshipEndReminderService;
import it.nexus.support.WorkflowScenarios;
import it.nexus.support.WorkflowScenarios.Actors;
import it.nexus.support.WorkflowScenarios.Place;
import jakarta.persistence.EntityManager;

/**
 * Fine del tirocinio (US-1203, PO 22/11): avvisi al Tutor 7 e 3 giorni prima della fine prevista e 3 e 7 giorni dopo,
 * in giorni di calendario; dal giorno dopo la fine, niente nuove segnalazioni finché il tirocinio non è chiuso o
 * prorogato.
 */
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, WorkflowScenarios.class})
class InternshipEndRemindersIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;
    private static final LocalDate END = LocalDate.of(2026, 12, 10);

    @Autowired MockMvc mockMvc;
    @Autowired WorkflowScenarios scenarios;
    @Autowired InternshipEndReminderService reminders;
    @Autowired TicketRepository tickets;
    @Autowired InternshipRepository internships;
    @Autowired NotificationRepository notifications;
    @Autowired AuditEventRepository audit;
    @Autowired EntityManager em;

    private Actors actors;
    private Place place;
    private Project project;

    @BeforeEach
    void setUp() {
        actors = scenarios.actors();
        place = scenarios.place();
        project = scenarios.project("GOL", actors.tutor1(), actors.tutor2());
    }

    /** Tirocinio avviato (US-1003) con la fine prevista indicata. */
    private Internship running(LocalDate end) {
        Ticket ticket = scenarios.appointmentTicket(actors.tutor1(), project,
                scenarios.freeSlot(place, "Aiuto cuoco " + end), actors.operator());
        ticket.setStatus(TicketStatus.IN_TIROCINIO);
        tickets.saveAndFlush(ticket);
        Internship internship = new Internship(ticket);
        internship.setStartDate(end.minusMonths(3));
        internship.setExpectedEndDate(end);
        return internships.saveAndFlush(internship);
    }

    /** Il controllo delle 8 di quel giorno, in una sessione nuova come in produzione. */
    private int runOn(LocalDate day) {
        em.flush();
        em.clear();
        int sent = reminders.sendDue(day.atTime(LocalTime.of(8, 0)).atZone(TimeConfig.ZONE).toInstant());
        em.flush();
        em.clear();
        return sent;
    }

    private List<NotificationType> sentTo(Ticket ticket) {
        return notifications.findAll().stream()
                .filter(n -> n.getTicket() != null && n.getTicket().getId().equals(ticket.getId()))
                .filter(n -> n.getRecipient().getId().equals(actors.tutor1().getId()))
                .map(n -> n.getType())
                .toList();
    }

    @Test
    void quattroAvvisiAlTutor_primaEDopoLaFine_unaVoltaSola() {
        Internship internship = running(END);
        Ticket ticket = internship.getTicket();
        long version = internship.getVersion();

        assertThat(runOn(END.minusDays(8))).as("8 giorni prima").isZero();
        assertThat(runOn(END.minusDays(7))).as("7 giorni prima").isEqualTo(1);
        assertThat(runOn(END.minusDays(7))).as("stesso giorno, di nuovo").isZero();
        assertThat(runOn(END.minusDays(4))).isZero();
        assertThat(runOn(END.minusDays(3))).as("3 giorni prima").isEqualTo(1);
        assertThat(runOn(END)).as("il giorno della fine").isZero();
        assertThat(runOn(END.plusDays(2))).isZero();
        assertThat(runOn(END.plusDays(3))).as("3 giorni dopo").isEqualTo(1);
        assertThat(runOn(END.plusDays(7))).as("7 giorni dopo").isEqualTo(1);
        assertThat(runOn(END.plusDays(20))).as("poi basta").isZero();

        assertThat(sentTo(ticket)).containsExactly(NotificationType.INTERNSHIP_ENDING,
                NotificationType.INTERNSHIP_ENDING, NotificationType.INTERNSHIP_OVERDUE,
                NotificationType.INTERNSHIP_OVERDUE);
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.TICKET, ticket.getId()))
                .filteredOn(e -> e.getAction().equals("INTERNSHIP_END_REMINDER"))
                .hasSize(4);
        // la scheda non cambia versione: il Tutor che la sta modificando non trova un conflitto
        assertThat(internships.findById(internship.getId()).orElseThrow().getVersion()).isEqualTo(version);
    }

    @Test
    void giorniSaltati_soloLAvvisoPiuRecente() {
        Ticket ticket = running(END).getTicket();

        assertThat(runOn(END.plusDays(5))).isEqualTo(1);
        assertThat(runOn(END.plusDays(7))).isEqualTo(1);

        assertThat(sentTo(ticket)).containsExactly(NotificationType.INTERNSHIP_OVERDUE,
                NotificationType.INTERNSHIP_OVERDUE);
    }

    @Test
    void prorogaGliAvvisiRipartonoDallaNuovaFine() {
        Internship internship = running(END);
        Ticket ticket = internship.getTicket();
        assertThat(runOn(END.minusDays(3))).isEqualTo(1);

        Internship reloaded = internships.findById(internship.getId()).orElseThrow();
        LocalDate newEnd = END.plusMonths(2);
        reloaded.addExtension("PRO-1", END.minusDays(5), END.minusDays(4), END.plusDays(1), newEnd);
        internships.saveAndFlush(reloaded);

        assertThat(runOn(END.plusDays(3))).as("la vecchia fine non conta più").isZero();
        assertThat(runOn(newEnd.minusDays(7))).isEqualTo(1);

        assertThat(sentTo(ticket)).containsExactly(NotificationType.INTERNSHIP_ENDING,
                NotificationType.INTERNSHIP_ENDING);
    }

    @Test
    void tirocinioChiuso_nessunAvviso() {
        Ticket ticket = running(END).getTicket();
        ticket.setStatus(TicketStatus.FORM_RESTITUZIONE);
        tickets.saveAndFlush(ticket);

        assertThat(runOn(END.plusDays(3))).isZero();
    }

    private ResultActions submit(String user, Beneficiary beneficiary) throws Exception {
        String json = """
                {"beneficiaryId": "%s", "projectId": "%s", "jobCategoryId": "%s", "internshipTypeId": "%s"}
                """.formatted(TsidMapper.toExternal(beneficiary.getId()), TsidMapper.toExternal(project.getId()),
                TsidMapper.toExternal(place.category().getId()), TestEntities.EXTRACURRICULAR);
        return mockMvc.perform(post("/api/v1/tickets").header(USER, user).contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    @Test
    void tirocinioFinitoDaChiudere_nuoveSegnalazioniBloccate_finoAllaProroga() throws Exception {
        LocalDate yesterday = LocalDate.now(TimeConfig.ZONE).minusDays(1);
        Internship internship = running(yesterday);
        Ticket ticket = internship.getTicket();
        Beneficiary other = scenarios.beneficiary(actors.tutor1(), place);

        mockMvc.perform(get("/api/v1/tickets/internships-to-close").header(USER, "tutor1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ticketId").value(TsidMapper.toExternal(ticket.getId())))
                .andExpect(jsonPath("$[0].ticketNumber").value(ticket.getNumber()))
                .andExpect(jsonPath("$[0].endDate").value(yesterday.toString()));
        submit("tutor1", other)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("segnalazione n. " + ticket.getNumber())));
        // un altro Tutor lavora normalmente
        mockMvc.perform(get("/api/v1/tickets/internships-to-close").header(USER, "tutor2"))
                .andExpect(jsonPath("$", hasSize(0)));

        // prorogato: si riparte
        internship.addExtension("PRO-1", yesterday.minusDays(10), yesterday.minusDays(9), yesterday.plusDays(1),
                yesterday.plusMonths(1));
        internships.saveAndFlush(internship);
        mockMvc.perform(get("/api/v1/tickets/internships-to-close").header(USER, "tutor1"))
                .andExpect(jsonPath("$", hasSize(0)));
        submit("tutor1", other).andExpect(status().isCreated());
    }

    @Test
    void fineOggi_nonAncoraBloccato() throws Exception {
        running(LocalDate.now(TimeConfig.ZONE));

        mockMvc.perform(get("/api/v1/tickets/internships-to-close").header(USER, "tutor1"))
                .andExpect(jsonPath("$", hasSize(0)));
        submit("tutor1", scenarios.beneficiary(actors.tutor1(), place)).andExpect(status().isCreated());
    }

    @Test
    void candidaturaDallaBacheca_ancheLeiBloccata() throws Exception {
        running(LocalDate.now(TimeConfig.ZONE).minusDays(2));
        var post = scenarios.publishedPost(scenarios.freeSlot(place, "Magazziniere"), null);
        String json = """
                {"boardPostId": "%s", "beneficiaryId": "%s", "projectId": "%s", "internshipTypeId": "%s"}
                """.formatted(TsidMapper.toExternal(post.getId()),
                TsidMapper.toExternal(scenarios.beneficiary(actors.tutor1(), place).getId()),
                TsidMapper.toExternal(project.getId()), TestEntities.EXTRACURRICULAR);

        mockMvc.perform(post("/api/v1/tickets/special").header(USER, "tutor1").contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("non chiudi o proroghi i tirocini finiti")));
    }

    @Test
    void soloIlTutor_vedeITirociniDaChiudere() throws Exception {
        mockMvc.perform(get("/api/v1/tickets/internships-to-close").header(USER, "operatore.cc"))
                .andExpect(status().isForbidden());
    }
}
