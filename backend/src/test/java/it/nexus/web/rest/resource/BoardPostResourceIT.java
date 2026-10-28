package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

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
import it.nexus.domain.BoardPost;
import it.nexus.domain.Company;
import it.nexus.domain.JobCategory;
import it.nexus.domain.JobSlot;
import it.nexus.domain.Project;
import it.nexus.domain.TestEntities;
import it.nexus.domain.Zone;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.BoardPostStatus;
import it.nexus.domain.enumeration.Role;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.AuditEventRepository;
import it.nexus.repository.BeneficiaryRepository;
import it.nexus.repository.BoardPostRepository;
import it.nexus.repository.CompanyRepository;
import it.nexus.repository.JobCategoryRepository;
import it.nexus.repository.JobSlotRepository;
import it.nexus.repository.ProjectRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.ZoneRepository;
import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
/** Gestione della bacheca (US-902): elenco per stato, modifica, pubblicazione, archiviazione, audit. */
class BoardPostResourceIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;
    private static final String POSTS = "/api/v1/board-posts";

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired AppUserRepository users;
    @Autowired CompanyRepository companies;
    @Autowired JobCategoryRepository categories;
    @Autowired ZoneRepository zones;
    @Autowired JobSlotRepository jobSlots;
    @Autowired ProjectRepository projects;
    @Autowired BeneficiaryRepository beneficiaries;
    @Autowired TicketRepository tickets;
    @Autowired BoardPostRepository boardPosts;
    @Autowired AuditEventRepository audit;

    private AppUser tutor;
    private Project gol;
    private Project polis;
    private Zone zone;
    private JobCategory category;
    private JobSlot draftSlot;
    private BoardPost draft;
    private BoardPost published;
    private Instant publishedAt;

    @BeforeEach
    void setUp() {
        tutor = users.save(TestEntities.user("tutor1", Role.TUTOR, "00000000-0000-4000-8000-000000000001"));
        users.save(TestEntities.user("operatore.cc", Role.CALL_CENTER, "00000000-0000-4000-8000-000000000003"));
        users.save(TestEntities.user("admin", Role.ADMIN, "00000000-0000-4000-8000-000000000004"));
        gol = projects.save(TestEntities.project("BGOL"));
        polis = projects.save(TestEntities.project("BPOLIS"));
        zone = zones.save(TestEntities.zone("BNORD"));
        category = categories.save(TestEntities.jobCategory("BMAG"));
        Company company = companies.save(TestEntities.company("99900000077"));
        draftSlot = jobSlots.save(TestEntities.jobSlot(company, category, zone));
        JobSlot publishedSlot = jobSlots.save(TestEntities.jobSlot(company, category, zone));

        BoardPost d = TestEntities.boardPost(draftSlot);
        d.setTitle("Magazziniere");
        d.setProject(gol);
        draft = boardPosts.save(d);
        BoardPost p = TestEntities.boardPost(publishedSlot);
        p.setTitle("Addetto pulizie");
        p.setProject(gol);
        p.setStatus(BoardPostStatus.PUBLISHED);
        publishedAt = Instant.now().minus(2, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
        p.setPublishedAt(publishedAt);
        published = boardPosts.save(p);
        em.flush();
    }

    private static String id(BoardPost post) {
        return TsidMapper.toExternal(post.getId());
    }

    private static String form(String title, Project project, String extra) {
        String projectId = project == null ? "null" : "\"" + TsidMapper.toExternal(project.getId()) + "\"";
        return """
                {"title": "%s", "projectId": %s, "weeklyHours": 20, "durationMonths": 6, "notes": "Turno del mattino",
                 "version": 0 %s}
                """.formatted(title, projectId, extra);
    }

    private ResultActions update(BoardPost post, String json) throws Exception {
        return mockMvc.perform(put(POSTS + "/" + id(post)).header(USER, "operatore.cc")
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private ResultActions action(BoardPost post, String action, long version) throws Exception {
        return mockMvc.perform(post(POSTS + "/" + id(post) + "/" + action).header(USER, "operatore.cc")
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\": " + version + "}"));
    }

    @Test
    void elencoPerStato_conAziendaEProgetto() throws Exception {
        mockMvc.perform(get(POSTS).header(USER, "operatore.cc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", contains("Magazziniere")))
                .andExpect(jsonPath("$[0].status").value("DRAFT"))
                .andExpect(jsonPath("$[0].jobSlot.companyName").isNotEmpty())
                .andExpect(jsonPath("$[0].jobSlot.zone.code").value("BNORD"))
                .andExpect(jsonPath("$[0].project.code").value("BGOL"));
        mockMvc.perform(get(POSTS).param("status", "PUBLISHED").header(USER, "admin"))
                .andExpect(jsonPath("$[*].title", contains("Addetto pulizie")))
                .andExpect(jsonPath("$[0].publishedAt").isNotEmpty());
        mockMvc.perform(get(POSTS + "/" + id(draft)).header(USER, "operatore.cc"))
                .andExpect(jsonPath("$.number").value(draft.getNumber()));
    }

    @Test
    void tutor_403() throws Exception {
        mockMvc.perform(get(POSTS).header(USER, "tutor1")).andExpect(status().isForbidden());
        mockMvc.perform(get(POSTS + "/" + id(draft)).header(USER, "tutor1")).andExpect(status().isForbidden());
        mockMvc.perform(post(POSTS + "/" + id(draft) + "/publish").header(USER, "tutor1")
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\": 0}")).andExpect(status().isForbidden());
    }

    @Test
    void modificaDellaBozza_ancheIlProgetto_conAudit() throws Exception {
        update(draft, form("Magazziniere turno serale", polis, ", \"ageMin\": 18, \"ageMax\": 29"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Magazziniere turno serale"))
                .andExpect(jsonPath("$.project.code").value("BPOLIS"))
                .andExpect(jsonPath("$.ageMin").value(18))
                .andExpect(jsonPath("$.version").value(1));
        update(draft, form("Magazziniere", null, "").replace("\"version\": 0", "\"version\": 1"))
                .andExpect(jsonPath("$.project").doesNotExist());

        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.BOARD_POST, draft.getId()))
                .hasSize(2)
                .first()
                .satisfies(e -> {
                    assertThat(e.getAction()).isEqualTo("UPDATE");
                    assertThat(e.getChanges()).containsKeys("title", "projectId", "ageMin", "ageMax", "notes");
                });
    }

    @Test
    void pubblicato_modificabileMaIlProgettoNo() throws Exception {
        update(published, form("Addetto pulizie uffici", gol, ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Addetto pulizie uffici"))
                .andExpect(jsonPath("$.publishedAt").value(publishedAt.toString()));
        update(published, form("Addetto pulizie uffici", null, "").replace("\"version\": 0", "\"version\": 1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("solo nella bozza")));
    }

    @Test
    void etaMinimaMaggioreDellaMassima_400SulCampo() throws Exception {
        update(draft, form("Magazziniere", gol, ", \"ageMin\": 30, \"ageMax\": 20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("ageMax"));
        update(draft, form("Magazziniere", gol, ", \"weeklyHours\": 0").replace("\"weeklyHours\": 20, ", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("weeklyHours"));
    }

    @Test
    void pubblicazione_conDataEAudit_unaSolaVolta() throws Exception {
        action(draft, "publish", 0)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.publishedAt").isNotEmpty());
        action(draft, "publish", 1).andExpect(status().isConflict());

        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.BOARD_POST, draft.getId()))
                .singleElement()
                .satisfies(e -> {
                    assertThat(e.getAction()).isEqualTo("PUBLISH");
                    assertThat(e.getChanges()).containsKeys("status", "publishedAt");
                });
    }

    @Test
    void pubblicazione_mansioneBloccataORitirata_409() throws Exception {
        var ticket = tickets.save(TestEntities.ticket(tutor, gol, beneficiaries.save(TestEntities.beneficiary(tutor, zone)),
                category));
        draftSlot.blockFor(ticket);
        jobSlots.saveAndFlush(draftSlot);
        action(draft, "publish", 0)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("bloccata dalla segnalazione n. " + ticket.getNumber())));

        draftSlot.release();
        draftSlot.setActive(false);
        jobSlots.saveAndFlush(draftSlot);
        action(draft, "publish", 0)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("non è a disposizione")));
    }

    @Test
    void archiviazione_poiSolaLettura() throws Exception {
        action(published, "archive", 0)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));
        update(published, form("Addetto pulizie", gol, "").replace("\"version\": 0", "\"version\": 1"))
                .andExpect(status().isConflict());
        action(published, "archive", 1).andExpect(status().isConflict());
        mockMvc.perform(get(POSTS).param("status", "ARCHIVED").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$[*].title", contains("Addetto pulizie")));
    }

    @Test
    void versioneSuperata_409() throws Exception {
        action(draft, "archive", 5).andExpect(status().isConflict());
        update(draft, form("Magazziniere", gol, "").replace("\"version\": 0", "\"version\": 3"))
                .andExpect(status().isConflict());
    }
}
