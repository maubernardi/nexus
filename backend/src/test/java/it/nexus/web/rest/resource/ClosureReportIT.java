package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

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
import it.nexus.domain.Internship;
import it.nexus.domain.JobSlot;
import it.nexus.domain.NoHireReason;
import it.nexus.domain.Project;
import it.nexus.domain.Ticket;
import it.nexus.domain.enumeration.JobSlotStatus;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.HiringFormulaRepository;
import it.nexus.repository.InternshipRepository;
import it.nexus.repository.JobSlotRepository;
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
/**
 * Report di restituzione (US-1101): lo invia solo il Tutor della segnalazione in tirocinio, una volta sola; con la fine
 * effettiva del tirocinio (US-1009).
 */
class ClosureReportIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired JdbcTemplate jdbc;
    @Autowired WorkflowScenarios scenarios;
    @Autowired HiringFormulaRepository hiringFormulas;
    @Autowired NoHireReasonRepository noHireReasons;
    @Autowired TicketRepository tickets;
    @Autowired JobSlotRepository jobSlots;
    @Autowired InternshipRepository internships;

    /** Voci iniziali dei motivi di chiusura (V41). */
    private static final String REGULAR = TsidMapper.toExternal(893700000000000361L);
    private static final String BY_COMPANY = TsidMapper.toExternal(893700000000000362L);
    private static final String BY_BENEFICIARY = TsidMapper.toExternal(893700000000000363L);

    private Ticket ticket;
    private JobSlot slot;
    private Place place;
    private Project project;
    private Actors actors;
    private String ticketPath;
    /** Valutazione dell'azienda (US-1104) mandata con il report: si cambia nei test che la provano. */
    private String review = """
            "companyInteraction": 4, "companyWelcome": 5, "companyTraining": 4,
            "companyReviewNote": " Accoglienza buona; poca formazione all'inizio \"""";
    private String companyHistoryPath;
    private HiringFormula formula;
    private NoHireReason reason;

    @BeforeEach
    void setUp() {
        Actors a = scenarios.actors();
        actors = a;
        place = scenarios.place();
        project = scenarios.project("GOL", a.tutor1(), a.tutor2());
        slot = scenarios.freeSlot(place, "Aiuto cuoco");
        ticket = scenarios.appointmentTicket(a.tutor1(), project, slot, a.operator());
        companyHistoryPath = "/api/v1/companies/" + TsidMapper.toExternal(place.company().getId()) + "/history";
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
        return send(user, version, hired, formulaId, reasonId, false);
    }

    private ResultActions send(String user, long version, Boolean hired, String formulaId, String reasonId,
            boolean jobSlotStillAvailable) throws Exception {
        return send(user, version, "2027-05-31", hired, formulaId, reasonId, jobSlotStillAvailable);
    }

    private ResultActions send(String user, long version, String actualEndDate, Boolean hired, String formulaId,
            String reasonId, boolean jobSlotStillAvailable) throws Exception {
        return send(user, version, actualEndDate, null, hired, formulaId, reasonId, jobSlotStillAvailable);
    }

    /** Con un motivo d'interruzione manda anche la descrizione (PO, 16/11). */
    private ResultActions send(String user, long version, String actualEndDate, String closureReasonId, Boolean hired,
            String formulaId, String reasonId, boolean jobSlotStillAvailable) throws Exception {
        return send(user, version, actualEndDate, closureReasonId,
                closureReasonId == null ? null : " L'azienda ha chiuso il reparto ", hired, formulaId, reasonId,
                jobSlotStillAvailable);
    }

    private ResultActions send(String user, long version, String actualEndDate, String closureReasonId,
            String interruptionDetails, Boolean hired, String formulaId, String reasonId,
            boolean jobSlotStillAvailable) throws Exception {
        String json = """
                {"version": %d, "actualEndDate": %s, "closureReasonId": %s, "interruptionDetails": %s,
                 "companySatisfaction": 5, "beneficiarySatisfaction": 4,
                 "objectives": "Autonomia in cucina raggiunta", "difficulties": "   ",
                 "hired": %s, "hiringFormulaId": %s, "noHireReasonId": %s, "jobSlotStillAvailable": %s,
                 %s}"""
                .formatted(version, quoted(actualEndDate), quoted(closureReasonId), quoted(interruptionDetails),
                        hired, quoted(formulaId), quoted(reasonId), jobSlotStillAvailable, review);
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
                .andExpect(jsonPath("$.closureReport.actualEndDate").value("2027-05-31"))
                // senza scheda non c'è una fine prevista: chiusura regolare (US-1103)
                .andExpect(jsonPath("$.closureReport.closureReason.name").value("Chiusura regolare"))
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
                        .value("Chiusura regolare; Assunto: " + formula.getName() + "; mansione non più disponibile"));
        mockMvc.perform(get("/api/v1/notifications").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$.items[0].type").value("CLOSURE_REPORT_SUBMITTED"));

        // l'audit registra le scelte e la fine effettiva, mai i testi liberi
        String changes = jdbc.queryForObject(
                "SELECT changes::text FROM audit_event WHERE entity_id = ? AND action = 'CLOSE'", String.class,
                ticket.getId());
        assertThat(changes).contains("hiringFormula", formula.getCode(), "jobSlotStillAvailable", "actualEndDate",
                        "2027-05-31", "closureReason", "CHIUSURA_REGOLARE")
                .doesNotContain("Autonomia", "objectives", "noHireReason");
    }

    @Test
    void valutazioneDellAzienda_nelReport_nellAuditSenzaLaNota_nellaSchedaDellAzienda() throws Exception {
        send("tutor1", ticketVersion(), true, formulaId(), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.closureReport.companyInteraction").value(4))
                .andExpect(jsonPath("$.closureReport.companyWelcome").value(5))
                .andExpect(jsonPath("$.closureReport.companyTraining").value(4))
                .andExpect(jsonPath("$.closureReport.companyReviewNote")
                        .value("Accoglienza buona; poca formazione all'inizio"));
        em.flush();
        String changes = jdbc.queryForObject(
                "SELECT changes::text FROM audit_event WHERE entity_id = ? AND action = 'CLOSE'", String.class,
                ticket.getId());
        assertThat(changes).contains("companyInteraction", "companyWelcome", "companyTraining")
                .doesNotContain("Accoglienza", "companyReviewNote");

        // la valutazione è dell'azienda del tirocinio: la vedono Call Center, ADMIN e Tutor, con il nome del Tutor
        String companyId = TsidMapper.toExternal(place.company().getId());
        for (String user : new String[] {"operatore.cc", "admin", "tutor2"}) {
            mockMvc.perform(get("/api/v1/companies/" + companyId + "/reviews").header(USER, user))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.average").value(closeTo(13 / 3.0, 0.001)))
                    .andExpect(jsonPath("$.total").value(1))
                    .andExpect(jsonPath("$.size").value(10))
                    .andExpect(jsonPath("$.reviews[0].endDate").value("2027-05-31"))
                    .andExpect(jsonPath("$.reviews[0].average").value(closeTo(13 / 3.0, 0.001)))
                    .andExpect(jsonPath("$.reviews[0].note").value("Accoglienza buona; poca formazione all'inizio"))
                    .andExpect(jsonPath("$.reviews[0].tutorName").value(actors.tutor1().getFirstName() + " "
                            + actors.tutor1().getLastName()));
        }
        // nessun dato del beneficiario nelle valutazioni
        String reviews = mockMvc.perform(get("/api/v1/companies/" + companyId + "/reviews").header(USER, "tutor2"))
                .andReturn().getResponse().getContentAsString();
        assertThat(reviews).doesNotContain(ticket.getBeneficiary().getFirstName(),
                ticket.getBeneficiary().getLastName(), "Autonomia");
        // la media nella lista e nella scheda del Tutor
        mockMvc.perform(get("/api/v1/companies/directory").header(USER, "tutor1"))
                .andExpect(jsonPath("$.companies[?(@.id == '%s')].reviewCount".formatted(companyId), contains(1)));
        mockMvc.perform(get("/api/v1/companies/directory/" + companyId).header(USER, "tutor1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(place.company().getName()))
                .andExpect(jsonPath("$.reviewCount").value(1))
                .andExpect(jsonPath("$.vatCode").doesNotExist());
        mockMvc.perform(get("/api/v1/companies/directory/" + companyId).header(USER, "operatore.cc"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/companies/0000000000000/reviews").header(USER, "tutor1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void valutazioni_mediaDelleMedie_dallaPiuRecente_senzaValutazioni() throws Exception {
        String companyId = TsidMapper.toExternal(place.company().getId());
        mockMvc.perform(get("/api/v1/companies/" + companyId + "/reviews").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$.average").doesNotExist())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.reviews").isEmpty());

        // primo tirocinio finito il 31/05/2027: 4, 5, 4 (media 4,33)
        send("tutor1", ticketVersion(), true, formulaId(), null).andExpect(status().isOk());
        // secondo, finito il 30/06/2027, di tutor2: 5, 5, 5 senza nota
        Ticket second = scenarios.appointmentTicket(actors.tutor2(), project, scenarios.freeSlot(place, "Cameriere"),
                actors.operator());
        second.setStatus(TicketStatus.IN_TIROCINIO);
        tickets.saveAndFlush(second);
        ticket = second;
        ticketPath = "/api/v1/tickets/" + TsidMapper.toExternal(second.getId());
        review = """
                "companyInteraction": 5, "companyWelcome": 5, "companyTraining": 5""";
        send("tutor2", ticketVersion(), "2027-06-30", true, formulaId(), null, false).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/companies/" + companyId + "/reviews").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$.average").value(closeTo((13 / 3.0 + 5) / 2, 0.001)))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.reviews[*].endDate", contains("2027-06-30", "2027-05-31")))
                .andExpect(jsonPath("$.reviews[0].note").doesNotExist());
        mockMvc.perform(get("/api/v1/companies/" + companyId + "/reviews").param("page", "1")
                        .header(USER, "operatore.cc"))
                .andExpect(jsonPath("$.reviews").isEmpty())
                .andExpect(jsonPath("$.total").value(2));
        mockMvc.perform(get("/api/v1/companies/" + companyId + "/reviews").param("page", "-1")
                        .header(USER, "operatore.cc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void valutazione_votiMancantiOFuoriIntervallo_notaTroppoLunga_400() throws Exception {
        review = """
                "companyInteraction": 4, "companyWelcome": 6""";
        send("tutor1", ticketVersion(), true, formulaId(), null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("companyWelcome")))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("companyTraining")));
        review = """
                "companyInteraction": 4, "companyWelcome": 4, "companyTraining": 4, "companyReviewNote": "%s\""""
                .formatted("x".repeat(501));
        send("tutor1", ticketVersion(), true, formulaId(), null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("companyReviewNote")));
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
                        .value("Chiusura regolare; Non assunto: " + reason.getName() + "; mansione non più disponibile"));
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
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("beneficiarySatisfaction")))
                .andExpect(jsonPath("$.fieldErrors[*].message", hasItem("Indica la data di fine effettiva")));

        assertThat(tickets.findById(ticket.getId()).orElseThrow().getStatus()).isEqualTo(TicketStatus.IN_TIROCINIO);
    }

    @Test
    void fineEffettiva_nonPrimaDellInizio_eLaSchedaNonCambia() throws Exception {
        Internship internship = new Internship(ticket);
        internship.setStartDate(LocalDate.of(2026, 12, 1));
        internship.setExpectedEndDate(LocalDate.of(2027, 5, 31));
        internships.saveAndFlush(internship);
        long version = ticketVersion();

        send("tutor1", version, "2026-11-30", true, formulaId(), null, false)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("actualEndDate"))
                .andExpect(jsonPath("$.fieldErrors[0].message")
                        .value("La fine effettiva non può precedere l'inizio del tirocinio (01/12/2026)"));
        assertThat(tickets.findById(ticket.getId()).orElseThrow().getStatus()).isEqualTo(TicketStatus.IN_TIROCINIO);

        // fine anticipata, anche lo stesso giorno dell'inizio: vale solo per il report, con il motivo (US-1103)
        String early = "Il tirocinio finisce prima del 31/05/2027: scegli il motivo dell'interruzione";
        send("tutor1", version, "2026-12-01", true, formulaId(), null, false)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("closureReasonId"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value(early));
        send("tutor1", version, "2026-12-01", REGULAR, true, formulaId(), null, false)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].message").value(early));
        // il motivo vuole anche la descrizione dell'interruzione (PO, 16/11)
        send("tutor1", version, "2026-12-01", BY_COMPANY, "   ", true, formulaId(), null, false)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("interruptionDetails"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Descrivi l'interruzione"));
        send("tutor1", version, "2026-12-01", BY_COMPANY, true, formulaId(), null, false)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.closureReport.actualEndDate").value("2026-12-01"))
                .andExpect(jsonPath("$.closureReport.interruptionDetails").value("L'azienda ha chiuso il reparto"))
                .andExpect(jsonPath("$.closureReport.closureReason.name")
                        .value("Interruzione anticipata per volontà dell'azienda"));
        mockMvc.perform(get(ticketPath + "/history").header(USER, "tutor1"))
                .andExpect(jsonPath("$[-1].detail").value("Interruzione anticipata per volontà dell'azienda; Assunto: "
                        + formula.getName() + "; mansione non più disponibile"));
        // la descrizione è testo libero: mai nell'audit
        assertThat(jdbc.queryForObject(
                "SELECT changes::text FROM audit_event WHERE entity_id = ? AND action = 'CLOSE'", String.class,
                ticket.getId())).contains("INTERRUZIONE_AZIENDA").doesNotContain("reparto", "interruptionDetails");
        em.flush();
        em.clear();
        Internship kept = internships.findByTicketId(ticket.getId()).orElseThrow();
        assertThat(kept.getStartDate()).isEqualTo(LocalDate.of(2026, 12, 1));
        assertThat(kept.getExpectedEndDate()).isEqualTo(LocalDate.of(2027, 5, 31));
    }

    @Test
    void conLeProroghe_laFinePrevistaEQuellaDellUltimaConvenzione() throws Exception {
        Internship internship = new Internship(ticket);
        internship.setStartDate(LocalDate.of(2026, 12, 1));
        internship.setExpectedEndDate(LocalDate.of(2027, 5, 31));
        // proroga 1 (US-1012)
        internship.addExtension("2027/200", LocalDate.of(2027, 6, 1), LocalDate.of(2027, 6, 10),
                LocalDate.of(2027, 6, 15), LocalDate.of(2027, 9, 30));
        internships.saveAndFlush(internship);
        long version = ticketVersion();

        // alla fine della convenzione iniziale il tirocinio prorogato si interrompe
        send("tutor1", version, "2027-05-31", true, formulaId(), null, false)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("closureReasonId"))
                .andExpect(jsonPath("$.fieldErrors[0].message")
                        .value("Il tirocinio finisce prima del 30/09/2027: scegli il motivo dell'interruzione"));
        send("tutor1", version, "2027-09-30", true, formulaId(), null, false)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.closureReport.closureReason.name").value("Chiusura regolare"));
    }

    @Test
    void motivoDiChiusura_fineComePrevista_eInterruzioneDisattivata() throws Exception {
        Internship internship = new Internship(ticket);
        internship.setStartDate(LocalDate.of(2026, 12, 1));
        internship.setExpectedEndDate(LocalDate.of(2027, 5, 31));
        internships.saveAndFlush(internship);
        long version = ticketVersion();

        // un'interruzione disattivata non si sceglie più
        jdbc.update("UPDATE closure_reason SET active = false WHERE code = 'INTERRUZIONE_DESTINATARIO'");
        send("tutor1", version, "2027-03-31", BY_BENEFICIARY, false, null, reasonId(), false)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("closureReasonId"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Motivo non valido"));

        // fine come prevista: chiusura regolare, anche se il modulo indica un'interruzione con la descrizione
        send("tutor1", version, "2027-05-31", BY_COMPANY, false, null, reasonId(), false)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.closureReport.closureReason.name").value("Chiusura regolare"))
                .andExpect(jsonPath("$.closureReport.interruptionDetails").doesNotExist());
    }

    @Test
    void mansioneAncoraDisponibile_tornaLibera_eLoStoricoDellAziendaRegistraLEsito() throws Exception {
        send("tutor1", ticketVersion(), true, formulaId(), null, true).andExpect(status().isOk());

        em.flush();
        em.clear();
        JobSlot released = jobSlots.findById(slot.getId()).orElseThrow();
        assertThat(released.getStatus()).isEqualTo(JobSlotStatus.LIBERA);
        assertThat(released.getBlockedByTicket()).isNull();
        // la segnalazione conclusa resta collegata alla mansione, per lo storico
        assertThat(tickets.findById(ticket.getId()).orElseThrow().getJobSlot().getId()).isEqualTo(slot.getId());
        assertThat(jdbc.queryForObject(
                "SELECT count(*) FROM audit_event WHERE entity_id = ? AND action = 'RELEASE'", Long.class,
                slot.getId())).isEqualTo(1);

        mockMvc.perform(get(companyHistoryPath).header(USER, "operatore.cc"))
                .andExpect(jsonPath("$.summary.hirings").value(1))
                .andExpect(jsonPath("$.summary.negativeOutcomes").value(0))
                .andExpect(jsonPath("$.events[0].type").value("TIROCINIO_CONCLUSO"))
                .andExpect(jsonPath("$.events[0].ticketNumber").value(ticket.getNumber()))
                .andExpect(jsonPath("$.events[0].detail")
                        .value("Con assunzione: «" + formula.getName() + "»; mansione di nuovo libera"));
        mockMvc.perform(get(ticketPath + "/history").header(USER, "tutor1"))
                .andExpect(jsonPath("$[-1].detail")
                        .value("Chiusura regolare; Assunto: " + formula.getName() + "; mansione di nuovo libera"));
    }

    @Test
    void mansioneNonPiuDisponibile_restaBloccata_eLEsitoNegativoNelloStorico() throws Exception {
        send("tutor1", ticketVersion(), false, null, reasonId(), false).andExpect(status().isOk());

        em.flush();
        em.clear();
        JobSlot kept = jobSlots.findById(slot.getId()).orElseThrow();
        assertThat(kept.getStatus()).isEqualTo(JobSlotStatus.BLOCCATA);
        assertThat(kept.getBlockedByTicket().getId()).isEqualTo(ticket.getId());

        mockMvc.perform(get(companyHistoryPath).header(USER, "operatore.cc"))
                .andExpect(jsonPath("$.summary.hirings").value(0))
                .andExpect(jsonPath("$.summary.negativeOutcomes").value(1))
                .andExpect(jsonPath("$.events[0].detail")
                        .value("Senza assunzione: «" + reason.getName() + "»; mansione non più disponibile"));
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
