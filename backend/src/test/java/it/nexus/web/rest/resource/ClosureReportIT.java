package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.security.MockHeaderAuthenticationFilter;
import it.nexus.domain.HiringFormula;
import it.nexus.domain.NoHireReason;
import it.nexus.domain.Project;
import it.nexus.domain.Ticket;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.HiringFormulaRepository;
import it.nexus.repository.NoHireReasonRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.support.WorkflowScenarios;
import it.nexus.support.WorkflowScenarios.Actors;
import it.nexus.support.WorkflowScenarios.Place;
import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, WorkflowScenarios.class})
/** Report di restituzione (US-1101): lo invia solo il Tutor della segnalazione in tirocinio, una volta sola. */
class ClosureReportIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired JdbcTemplate jdbc;
    @Autowired WorkflowScenarios scenarios;
    @Autowired HiringFormulaRepository hiringFormulas;
    @Autowired NoHireReasonRepository noHireReasons;
    @Autowired TicketRepository tickets;

    private Ticket ticket;
    private String ticketPath;
    private HiringFormula formula;
    private NoHireReason reason;

    @BeforeEach
    void setUp() {
        Actors a = scenarios.actors();
        Place place = scenarios.place();
        Project project = scenarios.project("GOL", a.tutor1(), a.tutor2());
        ticket = scenarios.appointmentTicket(a.tutor1(), project, scenarios.freeSlot(place, "Aiuto cuoco"),
                a.operator());
        // tirocinio avviato (US-1003)
        ticket.setStatus(TicketStatus.IN_TIROCINIO);
        tickets.saveAndFlush(ticket);
        ticketPath = "/api/v1/tickets/" + TsidMapper.toExternal(ticket.getId());
        formula = hiringFormulas.findByActiveTrueOrderByNameAsc().getFirst();
        reason = noHireReasons.findByActiveTrueOrderByNameAsc().getFirst();
    }

    private long ticketVersion() {
        em.flush();
        em.clear();
        return tickets.findById(ticket.getId()).orElseThrow().getVersion();
    }

    private ResultActions send(String user, long version, Boolean hired, String formulaId, String reasonId)
            throws Exception {
        String json = """
                {"version": %d, "companySatisfaction": 5, "beneficiarySatisfaction": 4,
                 "objectives": "Autonomia in cucina raggiunta", "difficulties": "   ",
                 "hired": %s, "hiringFormulaId": %s, "noHireReasonId": %s, "jobSlotStillAvailable": false}"""
                .formatted(version, hired, quoted(formulaId), quoted(reasonId));
        return mockMvc.perform(post(ticketPath + "/closure-report").header(USER, user)
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private static String quoted(String value) {
        return value == null ? "null" : "\"" + value + "\"";
    }

    private String formulaId() {
        return TsidMapper.toExternal(formula.getId());
    }

    private String reasonId() {
        return TsidMapper.toExternal(reason.getId());
    }

    @Test
    void invioDelReport_chiudeIlPercorso_dettaglioCronologiaENotifica() throws Exception {
        // il motivo rimasto nel modulo non conta: il beneficiario è assunto
        send("tutor1", ticketVersion(), true, formulaId(), reasonId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FORM_RESTITUZIONE"))
                .andExpect(jsonPath("$.closureReport.companySatisfaction").value(5))
                .andExpect(jsonPath("$.closureReport.beneficiarySatisfaction").value(4))
                .andExpect(jsonPath("$.closureReport.objectives").value("Autonomia in cucina raggiunta"))
                .andExpect(jsonPath("$.closureReport.difficulties").doesNotExist())
                .andExpect(jsonPath("$.closureReport.hired").value(true))
                .andExpect(jsonPath("$.closureReport.hiringFormula.name").value(formula.getName()))
                .andExpect(jsonPath("$.closureReport.noHireReason").doesNotExist())
                .andExpect(jsonPath("$.closureReport.jobSlotStillAvailable").value(false))
                .andExpect(jsonPath("$.closureReport.submittedBy").exists());

        // lo vedono chi vede la segnalazione: colleghi di progetto e Call Center
        for (String user : new String[] {"tutor2", "operatore.cc", "admin"}) {
            mockMvc.perform(get(ticketPath).header(USER, user))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.closureReport.hiringFormula.name").value(formula.getName()));
        }
        mockMvc.perform(get(ticketPath + "/history").header(USER, "tutor1"))
                .andExpect(jsonPath("$[*].action", hasItem("CLOSE")))
                .andExpect(jsonPath("$[-1].toStatus").value("FORM_RESTITUZIONE"))
                .andExpect(jsonPath("$[-1].detail")
                        .value("Assunto: " + formula.getName() + "; mansione non più disponibile"));
        mockMvc.perform(get("/api/v1/notifications").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$.items[0].type").value("CLOSURE_REPORT_SUBMITTED"));

        // l'audit registra le scelte, mai i testi liberi
        String changes = jdbc.queryForObject(
                "SELECT changes::text FROM audit_event WHERE entity_id = ? AND action = 'CLOSE'", String.class,
                ticket.getId());
        assertThat(changes).contains("hiringFormula", formula.getCode(), "jobSlotStillAvailable")
                .doesNotContain("Autonomia", "objectives", "noHireReason");
    }

    @Test
    void nonAssunto_conIlMotivo() throws Exception {
        send("tutor1", ticketVersion(), false, null, reasonId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.closureReport.hired").value(false))
                .andExpect(jsonPath("$.closureReport.noHireReason.name").value(reason.getName()))
                .andExpect(jsonPath("$.closureReport.hiringFormula").doesNotExist());
        mockMvc.perform(get(ticketPath + "/history").header(USER, "tutor1"))
                .andExpect(jsonPath("$[-1].detail")
                        .value("Non assunto: " + reason.getName() + "; mansione non più disponibile"));
    }

    @Test
    void formulaOMotivoMancantiODisattivati_invioRifiutato() throws Exception {
        long version = ticketVersion();
        send("tutor1", version, true, null, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("hiringFormulaId"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Scegli la formula di assunzione"));
        send("tutor1", version, false, formulaId(), null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("noHireReasonId"));

        reason.setActive(false);
        noHireReasons.saveAndFlush(reason);
        send("tutor1", version, false, null, reasonId())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Motivo non valido"));

        mockMvc.perform(post(ticketPath + "/closure-report").header(USER, "tutor1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"version": %d, "companySatisfaction": 6, "hired": true,
                                 "hiringFormulaId": "%s", "jobSlotStillAvailable": true}"""
                                .formatted(version, formulaId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("companySatisfaction")))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("beneficiarySatisfaction")));

        assertThat(tickets.findById(ticket.getId()).orElseThrow().getStatus()).isEqualTo(TicketStatus.IN_TIROCINIO);
    }

    @Test
    void reportInviato_nonSiInviaDiNuovo() throws Exception {
        send("tutor1", ticketVersion(), true, formulaId(), null).andExpect(status().isOk());
        send("tutor1", ticketVersion(), false, null, reasonId())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Operazione non ammessa per un ticket in stato FORM_RESTITUZIONE"));
        mockMvc.perform(get(ticketPath).header(USER, "tutor1"))
                .andExpect(jsonPath("$.closureReport.hired").value(true));
    }

    @Test
    void soloIlTutorDellaSegnalazione_versioneEStato() throws Exception {
        long version = ticketVersion();
        send("tutor2", version, true, formulaId(), null).andExpect(status().isForbidden());
        send("operatore.cc", version, true, formulaId(), null).andExpect(status().isForbidden());
        send("tutor1", version + 1, true, formulaId(), null).andExpect(status().isConflict());

        Ticket current = tickets.findById(ticket.getId()).orElseThrow();
        current.setStatus(TicketStatus.APPUNTAMENTO);
        em.flush();
        send("tutor1", ticketVersion(), true, formulaId(), null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Operazione non ammessa per un ticket in stato APPUNTAMENTO"));
        mockMvc.perform(get(ticketPath).header(USER, "tutor1"))
                .andExpect(jsonPath("$.closureReport").doesNotExist());
    }
}
