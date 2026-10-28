package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
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
import it.nexus.domain.UserProject;
import it.nexus.domain.Zone;
import it.nexus.domain.enumeration.BoardPostStatus;
import it.nexus.domain.enumeration.Role;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.BoardPostRepository;
import it.nexus.repository.CompanyRepository;
import it.nexus.repository.JobCategoryRepository;
import it.nexus.repository.JobSlotRepository;
import it.nexus.repository.ProjectRepository;
import it.nexus.repository.UserProjectRepository;
import it.nexus.repository.ZoneRepository;
import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
/** Bacheca del Tutor (US-901): pubblicati per tutti o per i suoi progetti, mai azienda, titolo e note. */
class TutorBoardIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;
    private static final String BOARD = "/api/v1/board";
    private static final String COMPANY_NAME = "Officina Segreta";

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired AppUserRepository users;
    @Autowired ProjectRepository projects;
    @Autowired UserProjectRepository userProjects;
    @Autowired ZoneRepository zones;
    @Autowired JobCategoryRepository categories;
    @Autowired CompanyRepository companies;
    @Autowired JobSlotRepository jobSlots;
    @Autowired BoardPostRepository boardPosts;

    private Company company;
    private Zone north;
    private JobCategory warehouse;
    private BoardPost forAll;
    private BoardPost forGol;
    private BoardPost forPolis;

    @BeforeEach
    void setUp() {
        AppUser tutor1 = users.save(TestEntities.user("tutor1", Role.TUTOR, "00000000-0000-4000-8000-000000000001"));
        users.save(TestEntities.user("operatore.cc", Role.CALL_CENTER, "00000000-0000-4000-8000-000000000003"));
        Project gol = projects.save(TestEntities.project("TGOL"));
        Project polis = projects.save(TestEntities.project("TPOLIS"));
        userProjects.save(new UserProject(tutor1, gol));
        north = zones.save(TestEntities.zone("TNORD"));
        Zone south = zones.save(TestEntities.zone("TSUD"));
        warehouse = categories.save(TestEntities.jobCategory("TMAG"));
        JobCategory cleaning = categories.save(TestEntities.jobCategory("TPUL"));
        Company c = TestEntities.company("99900000066");
        c.setName(COMPANY_NAME);
        company = companies.save(c);

        forAll = published(jobSlots.save(TestEntities.jobSlot(company, warehouse, north)), null, 3);
        forAll.setAgeMin(18);
        forAll.setAgeMax(29);
        forAll.setWeeklyHours(30);
        forAll.setDurationMonths(6);
        forGol = published(jobSlots.save(TestEntities.jobSlot(company, cleaning, south)), gol, 1);
        forPolis = published(jobSlots.save(TestEntities.jobSlot(company, warehouse, north)), polis, 2);
        BoardPost draft = TestEntities.boardPost(jobSlots.save(TestEntities.jobSlot(company, warehouse, north)));
        boardPosts.save(draft);
        BoardPost archived = published(jobSlots.save(TestEntities.jobSlot(company, warehouse, north)), null, 4);
        archived.setStatus(BoardPostStatus.ARCHIVED);
        em.flush();
    }

    /** Annuncio pubblicato {@code daysAgo} giorni fa, con titolo e note che citano l'azienda. */
    private BoardPost published(JobSlot slot, Project project, int daysAgo) {
        BoardPost post = TestEntities.boardPost(slot);
        post.setTitle("Tirocinio presso " + COMPANY_NAME);
        post.setNotes("Chiedere di " + COMPANY_NAME);
        post.setProject(project);
        post.setStatus(BoardPostStatus.PUBLISHED);
        post.setPublishedAt(Instant.now().minus(daysAgo, ChronoUnit.DAYS));
        return boardPosts.save(post);
    }

    @Test
    void perTuttiEPerIProgettiDelTutor_senzaAziendaTitoloENote() throws Exception {
        String body = mockMvc.perform(get(BOARD).header(USER, "tutor1"))
                .andExpect(status().isOk())
                // dal più recente: GOL (1 giorno fa), poi per tutti (3 giorni); mai POLIS, bozze o archiviati
                .andExpect(jsonPath("$[*].number", contains(forGol.getNumber().intValue(), forAll.getNumber().intValue())))
                .andExpect(jsonPath("$[0].project.code").value("TGOL"))
                .andExpect(jsonPath("$[0].zone.code").value("TSUD"))
                .andExpect(jsonPath("$[1].project").doesNotExist())
                .andExpect(jsonPath("$[1].jobCategory.code").value("TMAG"))
                .andExpect(jsonPath("$[1].ageMin").value(18))
                .andExpect(jsonPath("$[1].ageMax").value(29))
                .andExpect(jsonPath("$[1].weeklyHours").value(30))
                .andExpect(jsonPath("$[1].durationMonths").value(6))
                .andExpect(jsonPath("$[1].publishedAt").isNotEmpty())
                .andExpect(jsonPath("$[0].title").doesNotExist())
                .andExpect(jsonPath("$[0].notes").doesNotExist())
                .andExpect(jsonPath("$[0].jobSlot").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        // il nome dell'azienda non arriva mai al browser del Tutor, in nessun campo
        assertThat(body).doesNotContain(COMPANY_NAME).doesNotContain(TsidMapper.toExternal(company.getId()));
    }

    @Test
    void filtriPerZonaETipologia() throws Exception {
        mockMvc.perform(get(BOARD).param("zoneId", TsidMapper.toExternal(north.getId())).header(USER, "tutor1"))
                .andExpect(jsonPath("$[*].number", contains(forAll.getNumber().intValue())));
        mockMvc.perform(get(BOARD).param("jobCategoryId", TsidMapper.toExternal(warehouse.getId()))
                        .param("zoneId", "").header(USER, "tutor1"))
                .andExpect(jsonPath("$[*].number", contains(forAll.getNumber().intValue())));
        mockMvc.perform(get(BOARD).param("zoneId", "xyz").header(USER, "tutor1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void soloIlTutor() throws Exception {
        mockMvc.perform(get(BOARD).header(USER, "operatore.cc")).andExpect(status().isForbidden());
    }
}
