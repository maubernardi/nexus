package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import com.jayway.jsonpath.JsonPath;

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.security.MockHeaderAuthenticationFilter;
import it.nexus.domain.BoardPost;
import it.nexus.domain.CompetenceSite;
import it.nexus.domain.Project;
import it.nexus.domain.UserProject;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.CompetenceSiteRepository;
import it.nexus.repository.UserProjectRepository;
import it.nexus.support.WorkflowScenarios;
import it.nexus.support.WorkflowScenarios.Actors;
import it.nexus.support.WorkflowScenarios.Place;
import jakarta.persistence.EntityManager;

/** Sede di competenza della segnalazione (US-308): scelta alla creazione, corretta da ADMIN e Coordinatore. */
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, WorkflowScenarios.class})
class TicketCompetenceSiteIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;

    @Autowired MockMvc mockMvc;
    @Autowired WorkflowScenarios scenarios;
    @Autowired CompetenceSiteRepository sites;
    @Autowired UserProjectRepository userProjects;
    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManager em;

    private Actors actors;
    private Place place;
    private Project gol;
    private Project other;
    private CompetenceSite firenze;
    private CompetenceSite sesto;
    private CompetenceSite elsewhere;

    @BeforeEach
    void setUp() {
        actors = scenarios.actors();
        place = scenarios.place();
        gol = scenarios.project("SGOL", actors.tutor1(), actors.tutor2());
        other = scenarios.project("SNONE", actors.tutor1());
        firenze = site("SF_" + gol.getId(), "Firenze di prova " + gol.getId(), gol);
        sesto = site("SS_" + gol.getId(), "Sesto di prova " + gol.getId(), gol);
        elsewhere = site("SE_" + gol.getId(), "Altrove di prova " + gol.getId(),
                scenarios.project("SALTRO", actors.tutor1()));
        em.flush();
    }

    private CompetenceSite site(String code, String name, Project project) {
        CompetenceSite site = new CompetenceSite(code, name);
        site.getProjects().add(project);
        return sites.save(site);
    }

    private static String id(Object entity) {
        return entity instanceof CompetenceSite s ? TsidMapper.toExternal(s.getId())
                : TsidMapper.toExternal(((Project) entity).getId());
    }

    private ResultActions submit(Project project, String siteId) throws Exception {
        String beneficiary = TsidMapper.toExternal(scenarios.beneficiary(actors.tutor1(), place).getId());
        String site = siteId == null ? "" : ", \"competenceSiteId\": \"%s\"".formatted(siteId);
        return mockMvc.perform(post("/api/v1/tickets").header(USER, "tutor1").contentType(MediaType.APPLICATION_JSON)
                .content("{\"beneficiaryId\": \"%s\", \"projectId\": \"%s\", \"jobCategoryId\": \"%s\"%s}".formatted(
                        beneficiary, id(project), TsidMapper.toExternal(place.category().getId()), site)));
    }

    private ResultActions change(String user, String ticketId, CompetenceSite site) throws Exception {
        return mockMvc.perform(put("/api/v1/tickets/" + ticketId + "/competence-site").header(USER, user)
                .contentType(MediaType.APPLICATION_JSON).content("{\"competenceSiteId\": \"%s\"}".formatted(id(site))));
    }

    @Test
    void creazione_sedeObbligatoriaEDelProgetto() throws Exception {
        submit(gol, null).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("competenceSiteId"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Scegli la sede di competenza"));
        submit(gol, id(elsewhere)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("competenceSiteId"));
        String body = submit(gol, id(firenze)).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        mockMvc.perform(get("/api/v1/tickets/" + JsonPath.read(body, "$.id")).header(USER, "tutor1"))
                .andExpect(jsonPath("$.competenceSite.id").value(id(firenze)))
                .andExpect(jsonPath("$.canChangeCompetenceSite").value(false));
    }

    @Test
    void progettoSenzaSedi_nessunaSede() throws Exception {
        String body = submit(other, null).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        mockMvc.perform(get("/api/v1/tickets/" + JsonPath.read(body, "$.id")).header(USER, "tutor1"))
                .andExpect(jsonPath("$.competenceSite").doesNotExist());
        // l'ADMIN non ha nulla da proporre: niente comando di correzione
        mockMvc.perform(get("/api/v1/tickets/" + JsonPath.read(body, "$.id")).header(USER, "admin"))
                .andExpect(jsonPath("$.canChangeCompetenceSite").value(false));
    }

    @Test
    void sediTutteDisattivate_nessunaCorrezione() throws Exception {
        String ticketId = JsonPath.read(submit(gol, id(firenze)).andReturn().getResponse().getContentAsString(),
                "$.id");
        mockMvc.perform(get("/api/v1/tickets/" + ticketId).header(USER, "admin"))
                .andExpect(jsonPath("$.canChangeCompetenceSite").value(true));
        firenze.setActive(false);
        sesto.setActive(false);
        em.flush();
        mockMvc.perform(get("/api/v1/tickets/" + ticketId).header(USER, "admin"))
                .andExpect(jsonPath("$.competenceSite.id").value(id(firenze)))
                .andExpect(jsonPath("$.canChangeCompetenceSite").value(false));
    }

    @Test
    void sedeDisattivata_nonSiSceglie() throws Exception {
        firenze.setActive(false);
        em.flush();
        submit(gol, id(firenze)).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/reference/competence-sites").param("projectId", id(gol)).header(USER, "tutor1"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(id(sesto)));
    }

    @Test
    void correzione_soloAdminECoordinatore_conAudit() throws Exception {
        String ticketId = JsonPath.read(submit(gol, id(firenze)).andReturn().getResponse().getContentAsString(),
                "$.id");
        // il Tutor della segnalazione no, anche se è nel progetto
        change("tutor1", ticketId, sesto).andExpect(status().isForbidden());
        change("operatore.cc", ticketId, sesto).andExpect(status().isForbidden());

        UserProject coordinator = userProjects.findById(new UserProject.Id(actors.tutor2().getId(), gol.getId()))
                .orElseThrow();
        coordinator.setCoordinator(true);
        em.flush();
        mockMvc.perform(get("/api/v1/tickets/" + ticketId).header(USER, "tutor2"))
                .andExpect(jsonPath("$.canChangeCompetenceSite").value(true));
        change("tutor2", ticketId, elsewhere).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("competenceSiteId"));
        change("tutor2", ticketId, sesto).andExpect(status().isOk())
                .andExpect(jsonPath("$.competenceSite.id").value(id(sesto)));
        change("admin", ticketId, firenze).andExpect(status().isOk())
                .andExpect(jsonPath("$.competenceSite.id").value(id(firenze)));

        mockMvc.perform(get("/api/v1/tickets/" + ticketId + "/history").header(USER, "tutor1"))
                .andExpect(jsonPath("$[?(@.action == 'COMPETENCE_SITE_CHANGED')].length()").exists());
        em.flush();
        assertThat(jdbc.queryForList("SELECT changes::text FROM audit_event WHERE entity_type = 'TICKET' AND "
                + "entity_id = ? AND action = 'COMPETENCE_SITE_CHANGED' ORDER BY created_at, id", String.class,
                TsidMapper.toInternal(ticketId)))
                .hasSize(2)
                .first().asString().contains(firenze.getName(), sesto.getName());
    }

    @Test
    void candidaturaDallaBacheca_ancheLeiConLaSede() throws Exception {
        BoardPost post = scenarios.publishedPost(scenarios.freeSlot(place, "Cameriere"), gol);
        em.flush();
        String beneficiary = TsidMapper.toExternal(scenarios.beneficiary(actors.tutor1(), place).getId());
        String json = "{\"boardPostId\": \"%s\", \"beneficiaryId\": \"%s\", \"projectId\": null%s}";
        mockMvc.perform(post("/api/v1/tickets/special").header(USER, "tutor1").contentType(MediaType.APPLICATION_JSON)
                        .content(json.formatted(TsidMapper.toExternal(post.getId()), beneficiary, "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("competenceSiteId"));
        mockMvc.perform(post("/api/v1/tickets/special").header(USER, "tutor1").contentType(MediaType.APPLICATION_JSON)
                        .content(json.formatted(TsidMapper.toExternal(post.getId()), beneficiary,
                                ", \"competenceSiteId\": \"%s\"".formatted(id(sesto)))))
                .andExpect(status().isCreated());
    }
}
