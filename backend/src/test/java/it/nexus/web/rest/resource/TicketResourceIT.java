package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.security.MockHeaderAuthenticationFilter;
import it.nexus.domain.AppUser;
import it.nexus.domain.JobCategory;
import it.nexus.domain.Project;
import it.nexus.domain.TestEntities;
import it.nexus.domain.UserProject;
import it.nexus.domain.Zone;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.Role;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.AuditEventRepository;
import it.nexus.repository.BeneficiaryRepository;
import it.nexus.repository.JobCategoryRepository;
import it.nexus.repository.ProjectRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.TicketStatusHistoryRepository;
import it.nexus.repository.UserProjectRepository;
import it.nexus.repository.ZoneRepository;
import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class TicketResourceIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;

    @Autowired MockMvc mockMvc;
    @Autowired AppUserRepository users;
    @Autowired ProjectRepository projects;
    @Autowired UserProjectRepository userProjects;
    @Autowired ZoneRepository zones;
    @Autowired JobCategoryRepository categories;
    @Autowired BeneficiaryRepository beneficiaries;
    @Autowired TicketRepository tickets;
    @Autowired TicketStatusHistoryRepository history;
    @Autowired EntityManager em;
    @Autowired AuditEventRepository audit;

    private String beneficiaryId;
    private String secondBeneficiaryId;
    private String otherTutorBeneficiaryId;
    private String projectId;
    private String inactiveProjectId;
    private String unassignedProjectId;
    private String categoryId;
    private String inactiveCategoryId;

    @BeforeEach
    void setUp() {
        AppUser tutor1 = users.save(TestEntities.user("tutor1", Role.TUTOR, "00000000-0000-4000-8000-000000000001"));
        AppUser tutor2 = users.save(TestEntities.user("tutor2", Role.TUTOR, "00000000-0000-4000-8000-000000000002"));
        users.save(TestEntities.user("operatore.cc", Role.CALL_CENTER, "00000000-0000-4000-8000-000000000003"));
        users.save(TestEntities.user("admin", Role.ADMIN, "00000000-0000-4000-8000-000000000004"));
        Zone zone = zones.save(TestEntities.zone("ZNORD"));

        Project gol = projects.save(TestEntities.project("TGOL"));
        Project polis = projects.save(TestEntities.project("TPOLIS"));
        Project closed = TestEntities.project("TCHIUSO");
        closed.setActive(false);
        closed = projects.save(closed);
        Project unassigned = projects.save(TestEntities.project("TALTRO"));
        userProjects.save(new UserProject(tutor1, polis));
        userProjects.save(new UserProject(tutor1, gol));
        userProjects.save(new UserProject(tutor1, closed));
        userProjects.save(new UserProject(tutor2, unassigned));

        JobCategory category = categories.save(TestEntities.jobCategory("TMAG"));
        JobCategory inactiveCategory = TestEntities.jobCategory("TOLD");
        inactiveCategory.setActive(false);
        inactiveCategory = categories.save(inactiveCategory);

        beneficiaryId = TsidMapper.toExternal(beneficiaries.save(TestEntities.beneficiary(tutor1, zone)).getId());
        secondBeneficiaryId = TsidMapper.toExternal(beneficiaries.save(TestEntities.beneficiary(tutor1, zone)).getId());
        otherTutorBeneficiaryId = TsidMapper.toExternal(beneficiaries.save(TestEntities.beneficiary(tutor2, zone)).getId());
        projectId = TsidMapper.toExternal(gol.getId());
        inactiveProjectId = TsidMapper.toExternal(closed.getId());
        unassignedProjectId = TsidMapper.toExternal(unassigned.getId());
        categoryId = TsidMapper.toExternal(category.getId());
        inactiveCategoryId = TsidMapper.toExternal(inactiveCategory.getId());
        userProjects.flush();
    }

    private ResultActions submit(String user, String beneficiary, String project, String category) throws Exception {
        String json = """
                {"beneficiaryId": "%s", "projectId": "%s", "jobCategoryId": "%s"}
                """.formatted(beneficiary, project, category);
        return mockMvc.perform(post("/api/v1/tickets").header(USER, user).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void segnalazioneRiuscita_nuovaConNumeroECronologia() throws Exception {
        String response = submit("tutor1", beneficiaryId, projectId, categoryId)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("NUOVA"))
                .andExpect(jsonPath("$.type").value("NORMAL"))
                .andExpect(jsonPath("$.fastTrack").value(false))
                .andExpect(jsonPath("$.number").isNumber())
                .andExpect(jsonPath("$.beneficiary.id").value(beneficiaryId))
                .andExpect(jsonPath("$.project.code").value("TGOL"))
                .andExpect(jsonPath("$.requestedJobCategory.code").value("TMAG"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        Long id = TsidMapper.toInternal(JsonPath.read(response, "$.id"));
        var ticket = tickets.findById(id).orElseThrow();
        assertThat(ticket.getTutor().getUsername()).isEqualTo("tutor1");
        assertThat(history.findByTicketIdOrderByCreatedAtAsc(id))
                .singleElement()
                .satisfies(row -> {
                    assertThat(row.getFromStatus()).isNull();
                    assertThat(row.getToStatus()).isEqualTo(TicketStatus.NUOVA);
                    assertThat(row.getCreatedBy()).isEqualTo("tutor1");
                });
    }

    @Test
    void numeriProgressivi() throws Exception {
        Number first = JsonPath.read(submit("tutor1", beneficiaryId, projectId, categoryId)
                .andReturn().getResponse().getContentAsString(), "$.number");
        Number second = JsonPath.read(submit("tutor1", secondBeneficiaryId, projectId, categoryId)
                .andReturn().getResponse().getContentAsString(), "$.number");
        assertThat(second.longValue()).isGreaterThan(first.longValue());
    }

    @Test
    void beneficiarioConSegnalazioneAperta_400ConNumero() throws Exception {
        Number first = JsonPath.read(submit("tutor1", beneficiaryId, projectId, categoryId)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.number");

        submit("tutor1", beneficiaryId, projectId, categoryId)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("beneficiaryId"))
                .andExpect(jsonPath("$.fieldErrors[0].message")
                        .value("Il beneficiario ha già una segnalazione aperta (n. %d)".formatted(first.longValue())));
        assertThat(tickets.count()).isEqualTo(1);
    }

    @Test
    void segnalazioneConclusa_neConsenteUnaNuova() throws Exception {
        Long id = TsidMapper.toInternal(JsonPath.read(submit("tutor1", beneficiaryId, projectId, categoryId)
                .andReturn().getResponse().getContentAsString(), "$.id"));
        em.createNativeQuery("UPDATE ticket SET status = 'FORM_RESTITUZIONE' WHERE id = :id").setParameter("id", id)
                .executeUpdate();
        em.clear();

        submit("tutor1", beneficiaryId, projectId, categoryId).andExpect(status().isCreated());
    }

    @Test
    void elencoBeneficiari_riportaLaSegnalazioneAperta() throws Exception {
        Number number = JsonPath.read(submit("tutor1", beneficiaryId, projectId, categoryId)
                .andReturn().getResponse().getContentAsString(), "$.number");

        mockMvc.perform(get("/api/v1/beneficiaries").header(USER, "tutor1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')].openTicketNumber".formatted(beneficiaryId)).value(number.intValue()))
                .andExpect(jsonPath("$[?(@.id == '%s')].openTicketNumber".formatted(secondBeneficiaryId)).isEmpty());
    }

    @Test
    void progettoNonAssegnato_400SulCampo() throws Exception {
        submit("tutor1", beneficiaryId, unassignedProjectId, categoryId)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("projectId"));
        assertThat(tickets.count()).isZero();
    }

    @Test
    void progettoDisattivato_400SulCampo() throws Exception {
        submit("tutor1", beneficiaryId, inactiveProjectId, categoryId)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("projectId"));
    }

    @Test
    void beneficiarioDiUnAltroTutor_400SulCampoComeSeNonEsistesse() throws Exception {
        String altrui = submit("tutor1", otherTutorBeneficiaryId, projectId, categoryId)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("beneficiaryId"))
                .andReturn().getResponse().getContentAsString();
        String inesistente = submit("tutor1", "0000000000000", projectId, categoryId)
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        assertThat(JsonPath.<String>read(altrui, "$.fieldErrors[0].message"))
                .isEqualTo(JsonPath.<String>read(inesistente, "$.fieldErrors[0].message"));
    }

    @Test
    void mansioneDisattivata_400SulCampo() throws Exception {
        submit("tutor1", beneficiaryId, projectId, inactiveCategoryId)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("jobCategoryId"));
    }

    @Test
    void campiMancanti_400ConTuttiGliErrori() throws Exception {
        mockMvc.perform(post("/api/v1/tickets").header(USER, "tutor1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors", hasSize(3)));
    }

    @Test
    void callCenterEAdmin_403() throws Exception {
        submit("operatore.cc", beneficiaryId, projectId, categoryId).andExpect(status().isForbidden());
        submit("admin", beneficiaryId, projectId, categoryId).andExpect(status().isForbidden());
        assertThat(tickets.count()).isZero();
    }

    @Test
    void mieiProgetti_soloAttiviAssegnatiOrdinatiPerNome() throws Exception {
        mockMvc.perform(get("/api/v1/me/projects").header(USER, "tutor1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].code", contains("TGOL", "TPOLIS")));
        mockMvc.perform(get("/api/v1/me/projects").header(USER, "operatore.cc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void invio_registraNellAuditLeSceltePerAutore() throws Exception {
        Long id = TsidMapper.toInternal(JsonPath.read(submit("tutor1", beneficiaryId, projectId, categoryId)
                .andReturn().getResponse().getContentAsString(), "$.id"));

        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.TICKET, id))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.getAction()).isEqualTo("SUBMIT");
                    assertThat(event.getCreatedBy()).isEqualTo("tutor1");
                    assertThat(event.getChanges())
                            .containsKeys("status", "beneficiaryId", "projectId", "requestedJobCategoryId", "type");
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> statusChange = (java.util.Map<String, Object>) event.getChanges().get("status");
                    assertThat(statusChange).containsEntry("before", null).containsEntry("after", "NUOVA");
                    assertThat(event.getChanges().get("beneficiaryId").toString()).contains(beneficiaryId);
                });
    }

    @Test
    void invioRifiutato_nessunEventoDiAudit() throws Exception {
        long before = audit.count();
        submit("tutor1", beneficiaryId, unassignedProjectId, categoryId).andExpect(status().isBadRequest());
        assertThat(audit.count()).isEqualTo(before);
    }
}
