package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

import com.jayway.jsonpath.JsonPath;

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.security.MockHeaderAuthenticationFilter;
import it.nexus.domain.CompetenceSite;
import it.nexus.domain.JobCategory;
import it.nexus.domain.Project;
import it.nexus.domain.TestEntities;
import it.nexus.domain.Ticket;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.CompetenceSiteRepository;
import it.nexus.repository.JobCategoryRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.support.WorkflowScenarios;
import it.nexus.support.WorkflowScenarios.Actors;
import it.nexus.support.WorkflowScenarios.Place;
import jakarta.persistence.EntityManager;

/**
 * Correzione di progetto e mansione richiesta (US-309): solo il Tutor della segnalazione, prima della proposta
 * all'azienda (PO, 28/11); con sede e tipo validi per il progetto nuovo, audit e storico.
 */
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, WorkflowScenarios.class})
class TicketRequestCorrectionIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;

    @Autowired MockMvc mockMvc;
    @Autowired WorkflowScenarios scenarios;
    @Autowired CompetenceSiteRepository sites;
    @Autowired JobCategoryRepository categories;
    @Autowired TicketRepository tickets;
    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManager em;

    private Actors actors;
    private Place place;
    private Project gol;
    private Project polis;
    private CompetenceSite firenze;
    private CompetenceSite prato;
    private JobCategory cameriere;

    @BeforeEach
    void setUp() {
        actors = scenarios.actors();
        place = scenarios.place();
        gol = scenarios.project("CGOL", actors.tutor1());
        polis = scenarios.project("CPOLIS", actors.tutor1());
        firenze = site("CF_" + gol.getId(), "Firenze di prova " + gol.getId(), gol);
        prato = site("CP_" + polis.getId(), "Prato di prova " + polis.getId(), polis);
        cameriere = categories.save(TestEntities.jobCategory("CAM_" + gol.getId()));
        em.flush();
    }

    private CompetenceSite site(String code, String name, Project project) {
        CompetenceSite site = new CompetenceSite(code, name);
        site.getProjects().add(project);
        return sites.save(site);
    }

    private static String id(Long internal) {
        return TsidMapper.toExternal(internal);
    }

    /** Segnalazione normale del tutor1 su GOL (Firenze), con la tipologia del luogo o con il testo libero. */
    private String submit(String freeText) throws Exception {
        String beneficiary = id(scenarios.beneficiary(actors.tutor1(), place).getId());
        String job = freeText == null
                ? "\"jobCategoryId\": \"%s\"".formatted(id(place.category().getId()))
                : "\"jobFreeText\": \"%s\"".formatted(freeText);
        String body = mockMvc.perform(post("/api/v1/tickets").header(USER, "tutor1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(("{\"beneficiaryId\": \"%s\", \"projectId\": \"%s\", \"competenceSiteId\": \"%s\", "
                                + "\"internshipTypeId\": \"0RSRFD8XW8G36\", %s}").formatted(beneficiary,
                                        id(gol.getId()), id(firenze.getId()), job)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private Map<String, Object> correction(String ticketId) throws Exception {
        String body = mockMvc.perform(get("/api/v1/tickets/" + ticketId).header(USER, "tutor1"))
                .andReturn().getResponse().getContentAsString();
        Map<String, Object> form = new LinkedHashMap<>();
        form.put("version", ((Number) JsonPath.read(body, "$.version")).longValue());
        form.put("projectId", id(gol.getId()));
        form.put("jobCategoryId", id(place.category().getId()));
        form.put("competenceSiteId", id(firenze.getId()));
        form.put("internshipTypeId", "0RSRFD8XW8G36");
        return form;
    }

    private ResultActions correct(String user, String ticketId, Map<String, Object> form) throws Exception {
        return mockMvc.perform(put("/api/v1/tickets/" + ticketId + "/request").header(USER, user)
                .contentType(MediaType.APPLICATION_JSON).content(toJson(form)));
    }

    private static String toJson(Map<String, Object> form) {
        return form.entrySet().stream()
                .map(e -> "\"%s\": %s".formatted(e.getKey(),
                        e.getValue() instanceof String text ? "\"" + text + "\"" : e.getValue()))
                .collect(Collectors.joining(", ", "{", "}"));
    }

    private Ticket ticket(String ticketId) {
        return tickets.findById(TsidMapper.toInternal(ticketId)).orElseThrow();
    }

    @Test
    void progettoSedeEMansione_conAuditEStorico() throws Exception {
        String ticketId = submit(null);
        mockMvc.perform(get("/api/v1/tickets/" + ticketId).header(USER, "tutor1"))
                .andExpect(jsonPath("$.canCorrectRequest").value(true));

        Map<String, Object> form = correction(ticketId);
        form.put("projectId", id(polis.getId()));
        form.put("competenceSiteId", id(prato.getId()));
        form.put("jobCategoryId", id(cameriere.getId()));
        correct("tutor1", ticketId, form).andExpect(status().isOk())
                .andExpect(jsonPath("$.project.id").value(id(polis.getId())))
                .andExpect(jsonPath("$.competenceSite.id").value(id(prato.getId())))
                .andExpect(jsonPath("$.requestedJobCategory.id").value(id(cameriere.getId())))
                .andExpect(jsonPath("$.internshipType.id").value("0RSRFD8XW8G36"))
                .andExpect(jsonPath("$.status").value("NUOVA"));

        mockMvc.perform(get("/api/v1/tickets/" + ticketId + "/history").header(USER, "tutor1"))
                .andExpect(jsonPath("$[?(@.action == 'REQUEST_CORRECTED')].detail").value(
                        "Progetto: %s → %s; Mansione: %s → %s; Sede di competenza: %s → %s".formatted(gol.getName(),
                                polis.getName(), place.category().getName(), cameriere.getName(), firenze.getName(),
                                prato.getName())));
        em.flush();
        assertThat(jdbc.queryForList("SELECT changes::text FROM audit_event WHERE entity_type = 'TICKET' AND "
                + "entity_id = ? AND action = 'REQUEST_CORRECTED'", String.class, TsidMapper.toInternal(ticketId)))
                .singleElement().asString().contains(gol.getName(), polis.getName(), cameriere.getName());
    }

    @Test
    void nienteDaCorreggere_nessunEvento() throws Exception {
        String ticketId = submit(null);
        correct("tutor1", ticketId, correction(ticketId)).andExpect(status().isOk());
        em.flush();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM audit_event WHERE entity_type = 'TICKET' AND "
                + "entity_id = ? AND action = 'REQUEST_CORRECTED'", Long.class, TsidMapper.toInternal(ticketId)))
                .isZero();
    }

    @Test
    void progettoNuovo_sedeETipoValidiPerLui() throws Exception {
        String ticketId = submit(null);
        Map<String, Object> form = correction(ticketId);
        form.put("projectId", id(polis.getId()));
        // la sede di GOL non è di POLIS
        correct("tutor1", ticketId, form).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("competenceSiteId"));
        form.remove("competenceSiteId");
        correct("tutor1", ticketId, form).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Scegli la sede di competenza"));

        // POLIS ammette solo il curriculare: l'extracurriculare della segnalazione non vale più
        polis.getAllowedInternshipTypes().add(scenarios.curricular());
        em.flush();
        form.put("competenceSiteId", id(prato.getId()));
        correct("tutor1", ticketId, form).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("internshipTypeId"));
        form.put("internshipTypeId", TsidMapper.toExternal(scenarios.curricular().getId()));
        correct("tutor1", ticketId, form).andExpect(status().isOk())
                .andExpect(jsonPath("$.internshipType.id").value(TsidMapper.toExternal(scenarios.curricular().getId())));

        // un progetto che il Tutor non ha
        Project other = scenarios.project("CALTRO", actors.tutor2());
        Map<String, Object> again = correction(ticketId);
        again.put("projectId", id(other.getId()));
        correct("tutor1", ticketId, again).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("projectId"));
    }

    @Test
    void mansione_attivaDelCatalogo_quellaGiaSceltaResta() throws Exception {
        String ticketId = submit(null);
        Map<String, Object> form = correction(ticketId);
        form.remove("jobCategoryId");
        correct("tutor1", ticketId, form).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("jobCategoryId"));
        cameriere.setActive(false);
        em.flush();
        form.put("jobCategoryId", id(cameriere.getId()));
        correct("tutor1", ticketId, form).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Mansione non valida"));
        // la tipologia già scelta, disattivata nel frattempo, non blocca la correzione del progetto
        place.category().setActive(false);
        em.flush();
        form.put("jobCategoryId", id(place.category().getId()));
        form.put("projectId", id(polis.getId()));
        form.put("competenceSiteId", id(prato.getId()));
        correct("tutor1", ticketId, form).andExpect(status().isOk())
                .andExpect(jsonPath("$.project.id").value(id(polis.getId())));
    }

    @Test
    void tipologiaInAttesaDiApprovazione_siCorreggeIlTesto() throws Exception {
        String ticketId = submit("Adetto al banco");
        assertThat(ticket(ticketId).getStatus()).isEqualTo(TicketStatus.IN_ATTESA_APPROVAZIONE);
        Map<String, Object> form = correction(ticketId);
        form.remove("jobCategoryId");
        form.put("jobFreeText", "  ");
        correct("tutor1", ticketId, form).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("jobFreeText"));
        form.put("jobFreeText", " Addetto al banco del pesce ");
        correct("tutor1", ticketId, form).andExpect(status().isOk())
                .andExpect(jsonPath("$.requestedJobFreeText").value("Addetto al banco del pesce"))
                .andExpect(jsonPath("$.status").value("IN_ATTESA_APPROVAZIONE"));
        mockMvc.perform(get("/api/v1/tickets/" + ticketId + "/history").header(USER, "tutor1"))
                .andExpect(jsonPath("$[?(@.action == 'REQUEST_CORRECTED')].detail")
                        .value("Mansione: Adetto al banco → Addetto al banco del pesce"));
    }

    @Test
    void dopoLaProposta_nonPiu() throws Exception {
        String ticketId = submit(null);
        Map<String, Object> form = correction(ticketId);
        Ticket t = ticket(ticketId);
        t.setStatus(TicketStatus.IN_LAVORAZIONE);
        tickets.saveAndFlush(t);
        mockMvc.perform(get("/api/v1/tickets/" + ticketId).header(USER, "tutor1"))
                .andExpect(jsonPath("$.canCorrectRequest").value(true));

        for (TicketStatus after : new TicketStatus[] {TicketStatus.PROPOSTA_AZIENDA, TicketStatus.APPUNTAMENTO,
                TicketStatus.RIAPERTO}) {
            t = ticket(ticketId);
            t.setStatus(after);
            tickets.saveAndFlush(t);
            mockMvc.perform(get("/api/v1/tickets/" + ticketId).header(USER, "tutor1"))
                    .andExpect(jsonPath("$.canCorrectRequest").value(false));
            form.put("version", ticket(ticketId).getVersion());
            correct("tutor1", ticketId, form).andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message")
                            .value("Dopo la proposta all'azienda progetto e mansione non si correggono più"));
        }
    }

    @Test
    void candidaturaDallaBacheca_no() throws Exception {
        Ticket special = scenarios.specialTicket(actors.tutor1(), gol,
                scenarios.publishedPost(scenarios.freeSlot(place, "Cameriere"), gol), null);
        String ticketId = TsidMapper.toExternal(special.getId());
        mockMvc.perform(get("/api/v1/tickets/" + ticketId).header(USER, "tutor1"))
                .andExpect(jsonPath("$.canCorrectRequest").value(false));
        Map<String, Object> form = correction(ticketId);
        correct("tutor1", ticketId, form).andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "La candidatura dalla bacheca ha la mansione dell'annuncio: progetto e mansione non si correggono"));
    }

    @Test
    void soloIlTutorDellaSegnalazione() throws Exception {
        String ticketId = submit(null);
        Map<String, Object> form = correction(ticketId);
        form.put("jobCategoryId", id(cameriere.getId()));
        correct("operatore.cc", ticketId, form).andExpect(status().isForbidden());
        correct("admin", ticketId, form).andExpect(status().isForbidden());
        // un altro Tutor non la vede
        correct("tutor2", ticketId, form).andExpect(status().isNotFound());
        for (String user : new String[] {"operatore.cc", "admin"}) {
            mockMvc.perform(get("/api/v1/tickets/" + ticketId).header(USER, user))
                    .andExpect(jsonPath("$.canCorrectRequest").value(false));
        }
        assertThat(ticket(ticketId).getRequestedJobCategory()).isEqualTo(place.category());
    }

    @Test
    void versioneSuperata_conflitto() throws Exception {
        String ticketId = submit(null);
        Map<String, Object> form = correction(ticketId);
        form.put("version", ((Long) form.get("version")) + 1);
        form.put("jobCategoryId", id(cameriere.getId()));
        correct("tutor1", ticketId, form).andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("ricarica la pagina")));
        form.remove("version");
        correct("tutor1", ticketId, form).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("version"));
    }
}
