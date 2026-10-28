package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
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

import com.jayway.jsonpath.JsonPath;

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
    private Company company;
    private JobSlot draftSlot;
    private JobSlot freeSlot;
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
        company = companies.save(TestEntities.company("99900000077"));
        draftSlot = jobSlots.save(TestEntities.jobSlot(company, category, zone));
        JobSlot publishedSlot = jobSlots.save(TestEntities.jobSlot(company, category, zone));
        freeSlot = jobSlots.save(TestEntities.jobSlot(company, category, zone));

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

    private ResultActions create(JobSlot slot, String user) throws Exception {
        return mockMvc.perform(post(POSTS).header(USER, user).contentType(MediaType.APPLICATION_JSON)
                .content("{\"jobSlotId\": \"" + TsidMapper.toExternal(slot.getId()) + "\"}"));
    }

    @Test
    void creazioneDaMansioneLibera_bozzaPerTutti_conAudit() throws Exception {
        String body = create(freeSlot, "operatore.cc")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.number").isNumber())
                .andExpect(jsonPath("$.title").value(freeSlot.getTitle()))
                .andExpect(jsonPath("$.project").doesNotExist())
                .andExpect(jsonPath("$.jobSlot.id").value(TsidMapper.toExternal(freeSlot.getId())))
                .andReturn().getResponse().getContentAsString();
        Long id = TsidMapper.toInternal(JsonPath.read(body, "$.id"));
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.BOARD_POST, id))
                .singleElement()
                .satisfies(e -> {
                    assertThat(e.getAction()).isEqualTo("CREATE");
                    assertThat(e.getChanges()).containsKeys("jobSlotId", "title", "status");
                });

        // la scheda dell'azienda indica l'annuncio attivo di ogni mansione
        mockMvc.perform(get("/api/v1/companies/" + TsidMapper.toExternal(company.getId()) + "/job-slots")
                        .header(USER, "operatore.cc"))
                .andExpect(jsonPath("$[?(@.id == '%s')].activeBoardPostNumber".formatted(
                        TsidMapper.toExternal(freeSlot.getId()))).value(contains(
                        JsonPath.<Integer>read(body, "$.number"))))
                .andExpect(jsonPath("$[?(@.id == '%s')].activeBoardPostNumber".formatted(
                        TsidMapper.toExternal(draftSlot.getId()))).value(contains(
                        draft.getNumber().intValue())));
    }

    @Test
    void creazione_unSoloAnnuncioAttivoPerMansione() throws Exception {
        create(draftSlot, "operatore.cc")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("ha già l'annuncio #" + draft.getNumber())));
        // archiviato l'annuncio, se ne può creare un altro
        action(draft, "archive", 0).andExpect(status().isOk());
        create(draftSlot, "admin").andExpect(status().isCreated());
    }

    @Test
    void creazione_mansioneNonProponibileOTutor_rifiutata() throws Exception {
        var ticket = tickets.save(TestEntities.ticket(tutor, gol, beneficiaries.save(TestEntities.beneficiary(tutor, zone)),
                category));
        freeSlot.blockFor(ticket);
        jobSlots.saveAndFlush(freeSlot);
        create(freeSlot, "operatore.cc")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("è bloccata dalla segnalazione n. " + ticket.getNumber())));
        create(freeSlot, "tutor1").andExpect(status().isForbidden());
        mockMvc.perform(post(POSTS).header(USER, "operatore.cc").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobSlotId\": \"xyz\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("jobSlotId"));
    }

    @Test
    void mansioneRitirataOAziendaDisattivata_annunciArchiviatiConMotivo() throws Exception {
        mockMvc.perform(post("/api/v1/job-slots/" + TsidMapper.toExternal(draftSlot.getId()) + "/deactivate")
                        .header(USER, "operatore.cc").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\": " + draftSlot.getVersion() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeBoardPostId").doesNotExist());
        mockMvc.perform(post("/api/v1/companies/" + TsidMapper.toExternal(company.getId()) + "/deactivate")
                        .header(USER, "operatore.cc").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\": " + company.getVersion() + "}"))
                .andExpect(status().isOk());
        em.flush();
        em.clear();

        assertThat(boardPosts.findById(draft.getId()).orElseThrow().getStatus()).isEqualTo(BoardPostStatus.ARCHIVED);
        assertThat(boardPosts.findById(published.getId()).orElseThrow().getStatus()).isEqualTo(BoardPostStatus.ARCHIVED);
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.BOARD_POST, draft.getId()))
                .singleElement().satisfies(e -> assertThat(e.getReason()).isEqualTo("Mansione ritirata"));
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.BOARD_POST, published.getId()))
                .singleElement().satisfies(e -> assertThat(e.getReason()).isEqualTo("Azienda disattivata"));
    }

    @Test
    void visibileATutti_soloDopo7GiorniDallaPubblicazione_conDataEAudit() throws Exception {
        // pubblicato da 2 giorni: troppo presto; il Tutor (senza progetti) non lo vede
        mockMvc.perform(get(POSTS + "/" + id(published)).header(USER, "operatore.cc"))
                .andExpect(jsonPath("$.makePublicFrom").value(publishedAt.plus(Duration.ofDays(7)).toString()));
        action(published, "make-public", 0)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("si potrà rendere visibile a tutti dal")));
        mockMvc.perform(get("/api/v1/board").header(USER, "tutor1")).andExpect(jsonPath("$.length()").value(0));

        em.createNativeQuery("UPDATE board_post SET published_at = now() - interval '8 days' WHERE id = :id")
                .setParameter("id", published.getId()).executeUpdate();
        em.clear();
        action(published, "make-public", 0)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.project").doesNotExist())
                .andExpect(jsonPath("$.madePublicAt").isNotEmpty())
                .andExpect(jsonPath("$.makePublicFrom").doesNotExist());
        // ora lo vede anche un Tutor di un altro progetto
        mockMvc.perform(get("/api/v1/board").header(USER, "tutor1"))
                .andExpect(jsonPath("$[*].number", contains(published.getNumber().intValue())));
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.BOARD_POST, published.getId()))
                .singleElement()
                .satisfies(e -> {
                    assertThat(e.getAction()).isEqualTo("MAKE_PUBLIC");
                    assertThat(e.getChanges()).containsKeys("projectId", "madePublicAt");
                });

        action(published, "make-public", 1)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("è già visibile a tutti")));
        action(draft, "make-public", 0)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("solo un annuncio pubblicato")));
    }
}
