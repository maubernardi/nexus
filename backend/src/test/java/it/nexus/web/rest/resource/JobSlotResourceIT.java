package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

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
import it.nexus.domain.Company;
import it.nexus.domain.JobCategory;
import it.nexus.domain.JobSlot;
import it.nexus.domain.Project;
import it.nexus.domain.TestEntities;
import it.nexus.domain.Ticket;
import it.nexus.domain.Zone;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.Role;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.AuditEventRepository;
import it.nexus.repository.BeneficiaryRepository;
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
class JobSlotResourceIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;

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
    @Autowired AuditEventRepository audit;

    private AppUser tutor;
    private Company company;
    private Zone zone;
    private JobCategory category;
    private String companyId;
    private String zoneId;
    private String categoryId;
    private String inactiveZoneId;

    @BeforeEach
    void setUp() {
        tutor = users.save(TestEntities.user("tutor1", Role.TUTOR, "00000000-0000-4000-8000-000000000001"));
        users.save(TestEntities.user("operatore.cc", Role.CALL_CENTER, "00000000-0000-4000-8000-000000000003"));
        company = companies.save(TestEntities.company("99900000088"));
        zone = zones.save(TestEntities.zone("ZMANS"));
        Zone closed = TestEntities.zone("ZMANSCHIUSA");
        closed.setActive(false);
        inactiveZoneId = TsidMapper.toExternal(zones.save(closed).getId());
        category = categories.save(TestEntities.jobCategory("CMANS"));
        companyId = TsidMapper.toExternal(company.getId());
        zoneId = TsidMapper.toExternal(zone.getId());
        categoryId = TsidMapper.toExternal(category.getId());
        em.flush();
    }

    private String body(String title, String zone, String extra) {
        return """
                {"title": "%s", "jobCategoryId": "%s", "zoneId": "%s", "description": "Turno del mattino" %s}
                """.formatted(title, categoryId, zone, extra);
    }

    private ResultActions create(String user, String json) throws Exception {
        return mockMvc.perform(post("/api/v1/companies/" + companyId + "/job-slots").header(USER, user)
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private String createdId(String title) throws Exception {
        return JsonPath.read(create("operatore.cc", body(title, zoneId, "")).andReturn().getResponse().getContentAsString(), "$.id");
    }

    @Test
    void creazione_liberaAttivaEAudit() throws Exception {
        String id = JsonPath.read(create("operatore.cc", body("Magazziniere", zoneId, ""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("LIBERA"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.zone.code").value("ZMANS"))
                .andExpect(jsonPath("$.jobCategory.code").value("CMANS"))
                .andReturn().getResponse().getContentAsString(), "$.id");

        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.JOB_SLOT, TsidMapper.toInternal(id)))
                .singleElement()
                .satisfies(e -> {
                    assertThat(e.getAction()).isEqualTo("CREATE");
                    assertThat(e.getChanges()).containsKeys("companyId", "title", "jobCategoryId", "zoneId");
                });
    }

    @Test
    void elenco_primaLeAttive_poiPerTitolo() throws Exception {
        createdId("Cuoco");
        String ritirata = createdId("Addetto pulizie");
        createdId("Barista");
        mockMvc.perform(post("/api/v1/job-slots/" + ritirata + "/deactivate").header(USER, "operatore.cc")
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\": 0}")).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/companies/" + companyId + "/job-slots").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$[*].title", contains("Barista", "Cuoco", "Addetto pulizie")))
                .andExpect(jsonPath("$[2].active").value(false));
    }

    @Test
    void zonaDisattivata_400SulCampo_aziendaDisattivata_409() throws Exception {
        create("operatore.cc", body("Magazziniere", inactiveZoneId, ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("zoneId"));
        company.setActive(false);
        companies.saveAndFlush(company);
        create("operatore.cc", body("Magazziniere", zoneId, "")).andExpect(status().isConflict());
    }

    @Test
    void modifica_auditDeiSoliCampiCambiati_versioneSuperata409() throws Exception {
        String id = createdId("Magazziniere");
        mockMvc.perform(put("/api/v1/job-slots/" + id).header(USER, "operatore.cc").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Magazziniere carrellista", zoneId, ", \"version\": 0")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Magazziniere carrellista"));
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.JOB_SLOT, TsidMapper.toInternal(id)))
                .last()
                .satisfies(e -> assertThat(e.getChanges()).containsOnlyKeys("title")
                        .containsEntry("title", Map.of("before", "Magazziniere", "after", "Magazziniere carrellista")));

        mockMvc.perform(put("/api/v1/job-slots/" + id).header(USER, "operatore.cc").contentType(MediaType.APPLICATION_JSON)
                .content(body("Altro", zoneId, ", \"version\": 0"))).andExpect(status().isConflict());
    }

    @Test
    void lostatoNonSiModificaAMano() throws Exception {
        String id = createdId("Magazziniere");
        mockMvc.perform(put("/api/v1/job-slots/" + id).header(USER, "operatore.cc").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Magazziniere", zoneId, ", \"version\": 0, \"status\": \"BLOCCATA\"")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LIBERA"));
    }

    @Test
    void mansioneBloccata_nonSiRitira_eMostraLaSegnalazione() throws Exception {
        Project project = projects.save(TestEntities.project("PMANS"));
        Ticket ticket = tickets.save(TestEntities.ticket(tutor, project, beneficiaries.save(TestEntities.beneficiary(tutor, zone)),
                category));
        JobSlot slot = jobSlots.save(TestEntities.jobSlot(company, category, zone));
        slot.blockFor(ticket);
        jobSlots.saveAndFlush(slot);
        em.refresh(ticket);
        String id = TsidMapper.toExternal(slot.getId());

        mockMvc.perform(get("/api/v1/companies/" + companyId + "/job-slots").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$[0].status").value("BLOCCATA"))
                .andExpect(jsonPath("$[0].blockedByTicketNumber").value(ticket.getNumber().intValue()));
        mockMvc.perform(post("/api/v1/job-slots/" + id + "/deactivate").header(USER, "operatore.cc")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"version\": " + slot.getVersion() + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("bloccata dalla segnalazione")));
    }

    @Test
    void tutor403_aziendaInesistente404() throws Exception {
        create("tutor1", body("X", zoneId, "")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/companies/0000000000000/job-slots").header(USER, "operatore.cc"))
                .andExpect(status().isNotFound());
    }
}
