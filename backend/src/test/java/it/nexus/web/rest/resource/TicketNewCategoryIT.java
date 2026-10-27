package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
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
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.AuditEventRepository;
import it.nexus.repository.BeneficiaryRepository;
import it.nexus.repository.JobCategoryRepository;
import it.nexus.repository.ProjectRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.UserProjectRepository;
import it.nexus.repository.ZoneRepository;

/** Nuova tipologia di mansione proposta dal Tutor e decisa dal Call Center (US-302). */
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class TicketNewCategoryIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;

    @Autowired MockMvc mockMvc;
    @Autowired AppUserRepository users;
    @Autowired ProjectRepository projects;
    @Autowired UserProjectRepository userProjects;
    @Autowired ZoneRepository zones;
    @Autowired JobCategoryRepository categories;
    @Autowired BeneficiaryRepository beneficiaries;
    @Autowired TicketRepository tickets;
    @Autowired AuditEventRepository audit;

    private AppUser tutor;
    private Zone zone;
    private String projectId;
    private String catalogId;

    @BeforeEach
    void setUp() {
        tutor = users.save(TestEntities.user("tutor1", Role.TUTOR, "00000000-0000-4000-8000-000000000001"));
        users.save(TestEntities.user("operatore.cc", Role.CALL_CENTER, "00000000-0000-4000-8000-000000000003"));
        Project project = projects.save(TestEntities.project("PNEWCAT"));
        userProjects.save(new UserProject(tutor, project));
        projectId = TsidMapper.toExternal(project.getId());
        zone = zones.save(TestEntities.zone("ZNEWCAT"));
        JobCategory magazziniere = TestEntities.jobCategory("QMAGAZZ");
        magazziniere.setName("Magazziniere QA");
        catalogId = TsidMapper.toExternal(categories.saveAndFlush(magazziniere).getId());
    }

    private String beneficiary() {
        return TsidMapper.toExternal(beneficiaries.save(TestEntities.beneficiary(tutor, zone)).getId());
    }

    private ResultActions submit(String category, String freeText) throws Exception {
        String json = """
                {"beneficiaryId": "%s", "projectId": "%s", "jobCategoryId": %s, "jobFreeText": %s}
                """.formatted(beneficiary(), projectId, category == null ? "null" : "\"" + category + "\"",
                freeText == null ? "null" : "\"" + freeText + "\"");
        return mockMvc.perform(post("/api/v1/tickets").header(USER, "tutor1").contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    /** Invia una segnalazione con tipologia nuova e ne restituisce l'id. */
    private String submitNew(String freeText) throws Exception {
        return JsonPath.read(submit(null, freeText).andExpect(status().isCreated()).andReturn().getResponse()
                .getContentAsString(), "$.id");
    }

    private long version(String id) {
        return tickets.findById(TsidMapper.toInternal(id)).orElseThrow().getVersion();
    }

    private ResultActions decide(String user, String id, String action, String json) throws Exception {
        return mockMvc.perform(post("/api/v1/tickets/" + id + "/category/" + action).header(USER, user)
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private ResultActions approve(String user, String id, String name) throws Exception {
        return decide(user, id, "approve", "{\"name\": \"%s\", \"version\": %d}".formatted(name, version(id)));
    }

    @Test
    void invioConTipologiaNuova_inAttesaEVisibileNellaVistaDaApprovare() throws Exception {
        String id = submitNew("  Tecnico del suono ");

        mockMvc.perform(get("/api/v1/tickets/" + id).header(USER, "tutor1"))
                .andExpect(jsonPath("$.status").value("IN_ATTESA_APPROVAZIONE"))
                .andExpect(jsonPath("$.requestedJobFreeText").value("Tecnico del suono"));
        mockMvc.perform(get("/api/v1/tickets").param("view", "TO_APPROVE").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$[*].id", hasItem(id)))
                .andExpect(jsonPath("$[0].requestedJobFreeText").value("Tecnico del suono"));
        mockMvc.perform(get("/api/v1/tickets").param("view", "TO_TAKE").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$[*].id", not(hasItem(id))));
        mockMvc.perform(get("/api/v1/tickets").param("view", "TO_APPROVE").header(USER, "tutor1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invio_tipologiaGiaNelCatalogoOSceltaDoppiaOAssente_400() throws Exception {
        submit(null, "magazziniere qa").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("jobFreeText"));
        submit(catalogId, "Fonico").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("jobCategoryId"));
        submit(null, "  ").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("jobCategoryId"));
    }

    @Test
    void approvazione_conNomeCorretto_entraNelCatalogoEVaInCoda() throws Exception {
        String id = submitNew("Tecnico del suono");

        approve("operatore.cc", id, "Tecnico audio")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NUOVA"))
                .andExpect(jsonPath("$.requestedJobCategory.name").value("Tecnico audio"))
                .andExpect(jsonPath("$.requestedJobCategory.code").value("TECNICO_AUDIO"))
                .andExpect(jsonPath("$.requestedJobFreeText").doesNotExist());

        JobCategory created = categories.findByCode("TECNICO_AUDIO").orElseThrow();
        assertThat(created.isActive()).isTrue();
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.JOB_CATEGORY,
                created.getId())).singleElement().satisfies(e -> assertThat(e.getAction()).isEqualTo("CREATE"));
        mockMvc.perform(get("/api/v1/tickets").param("view", "TO_TAKE").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$[*].id", hasItem(id)));
        mockMvc.perform(get("/api/v1/tickets/" + id + "/history").header(USER, "tutor1"))
                .andExpect(jsonPath("$[-1].action").value("CATEGORY_APPROVE"))
                .andExpect(jsonPath("$[-1].detail").value("«Tecnico audio» aggiunta al catalogo"))
                .andExpect(jsonPath("$[-1].toStatus").value("NUOVA"));
    }

    @Test
    void approvazione_nomeGiaNelCatalogo_400_eCodiceUnivoco() throws Exception {
        approve("operatore.cc", submitNew("Fonico"), "MAGAZZINIERE QA")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"));

        approve("operatore.cc", submitNew("Tecnico del suono"), "Tecnico del suono").andExpect(status().isOk());
        approve("operatore.cc", submitNew("Tecnico-del-suono"), "Tecnico-del-suono")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestedJobCategory.code").value("TECNICO_DEL_SUONO_2"));
    }

    @Test
    void respingimento_tipologiaDelCatalogoMotivoEAvvisoAlTutor() throws Exception {
        String id = submitNew("Tecnico del suono");

        decide("operatore.cc", id, "reject", "{\"jobCategoryId\": \"%s\", \"reason\": \"%s\", \"version\": %d}"
                        .formatted(catalogId, "Rientra nel catalogo", version(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NUOVA"))
                .andExpect(jsonPath("$.requestedJobCategory.name").value("Magazziniere QA"))
                .andExpect(jsonPath("$.categoryChanged").value(false));

        mockMvc.perform(get("/api/v1/tickets/" + id + "/history").header(USER, "tutor1"))
                .andExpect(jsonPath("$[-1].action").value("CATEGORY_REJECT"))
                .andExpect(jsonPath("$[-1].detail").value("«Tecnico del suono» → «Magazziniere QA»"))
                .andExpect(jsonPath("$[-1].note").value("Rientra nel catalogo"));
        // evidenza per il Tutor finché non apre la segnalazione
        mockMvc.perform(get("/api/v1/tickets").param("view", "MINE").header(USER, "tutor1"))
                .andExpect(jsonPath("$[0].categoryChangeUnseen").value(true));
        mockMvc.perform(get("/api/v1/tickets/" + id).header(USER, "tutor1"))
                .andExpect(jsonPath("$.categoryChanged").value(true));
        mockMvc.perform(post("/api/v1/tickets/" + id + "/category-change-seen").header(USER, "tutor1"))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/tickets").param("view", "MINE").header(USER, "tutor1"))
                .andExpect(jsonPath("$[0].categoryChangeUnseen").value(false));
    }

    @Test
    void respingimento_senzaMotivo_400() throws Exception {
        String id = submitNew("Tecnico del suono");
        decide("operatore.cc", id, "reject", "{\"jobCategoryId\": \"%s\", \"reason\": \" \", \"version\": %d}"
                .formatted(catalogId, version(id)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("reason"));
    }

    @Test
    void soloCallCenter_eSoloSeInAttesa() throws Exception {
        String id = submitNew("Tecnico del suono");
        approve("tutor1", id, "Tecnico del suono").andExpect(status().isForbidden());
        approve("operatore.cc", id, "Tecnico del suono").andExpect(status().isOk());
        approve("operatore.cc", id, "Altro nome").andExpect(status().isConflict());
    }
}
