package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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
import it.nexus.domain.Beneficiary;
import it.nexus.domain.Zone;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AuditEventRepository;
import it.nexus.support.WorkflowScenarios;
import it.nexus.support.WorkflowScenarios.Actors;
import it.nexus.support.WorkflowScenarios.Place;
import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, WorkflowScenarios.class})
/** Cataloghi di zone e tipologie per l'ADMIN (US-106): aggiunta, nome, disattivazione, audit. */
class CatalogResourceIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;
    private static final String ZONES = "/api/v1/catalogs/zones";
    private static final String CATEGORIES = "/api/v1/catalogs/job-categories";

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired WorkflowScenarios scenarios;
    @Autowired AuditEventRepository audit;

    private Actors actors;

    @BeforeEach
    void setUp() {
        actors = scenarios.actors();
        em.flush();
    }

    private ResultActions create(String path, String name) throws Exception {
        return mockMvc.perform(post(path).header(USER, "admin").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"" + name + "\"}"));
    }

    private String createdId(String path, String name) throws Exception {
        return JsonPath.read(create(path, name).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id");
    }

    private ResultActions action(String path, String id, String action) throws Exception {
        return mockMvc.perform(post(path + "/" + id + "/" + action).header(USER, "admin"));
    }

    @Test
    void nuovaZona_codiceDalNome_attivaEPerTutti_conAudit() throws Exception {
        String id = JsonPath.read(create(ZONES, "Sesto Fiorentino Nord")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SESTO_FIORENTINO_NORD"))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn().getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/api/v1/reference/zones").header(USER, "tutor1"))
                .andExpect(jsonPath("$[*].name", hasItem("Sesto Fiorentino Nord")));
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.ZONE,
                TsidMapper.toInternal(id)))
                .singleElement()
                .satisfies(e -> {
                    assertThat(e.getAction()).isEqualTo("CREATE");
                    assertThat(e.getChanges()).containsKeys("code", "name");
                });
    }

    @Test
    void nomeGiaAttivo_400SulCampo_ancheConMaiuscoleDiverse() throws Exception {
        createdId(CATEGORIES, "Fonico per eventi");
        create(CATEGORIES, "fonico PER eventi")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("«Fonico per eventi» è già nel catalogo"));
        create(CATEGORIES, "   ").andExpect(status().isBadRequest());
    }

    @Test
    void rinomina_ilCodiceNonCambia_conAudit() throws Exception {
        String id = createdId(CATEGORIES, "Aiuto cuoko");
        mockMvc.perform(put(CATEGORIES + "/" + id).header(USER, "admin").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Aiuto cuoco\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Aiuto cuoco"))
                .andExpect(jsonPath("$.code").value("AIUTO_CUOKO"));
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.JOB_CATEGORY,
                TsidMapper.toInternal(id)))
                .extracting(e -> e.getAction()).containsExactly("CREATE", "UPDATE");
    }

    @Test
    void disattivata_nonSelezionabileMaRestaSuiDati_poiRiattivata() throws Exception {
        String id = createdId(ZONES, "Zona di collaudo");
        Place place = scenarios.place();
        Beneficiary beneficiary = scenarios.beneficiary(actors.tutor1(), place);
        beneficiary.setResidenceZone(em.find(Zone.class, TsidMapper.toInternal(id)));
        em.flush();

        action(ZONES, id, "deactivate").andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false));
        mockMvc.perform(get("/api/v1/reference/zones").header(USER, "tutor1"))
                .andExpect(jsonPath("$[*].name", not(hasItem("Zona di collaudo"))));
        // resta sui dati esistenti: il beneficiario la mantiene
        mockMvc.perform(get("/api/v1/beneficiaries/" + TsidMapper.toExternal(beneficiary.getId())).header(USER, "tutor1"))
                .andExpect(jsonPath("$.residenceZone.name").value("Zona di collaudo"));
        // l'elenco dell'ADMIN la mostra, dopo le attive
        String list = mockMvc.perform(get(ZONES).header(USER, "admin")).andReturn().getResponse().getContentAsString();
        List<Boolean> actives = JsonPath.read(list, "$[*].active");
        assertThat(actives.indexOf(false)).isGreaterThan(actives.lastIndexOf(true));

        // con un'altra zona attiva dello stesso nome non si riattiva
        String twin = createdId(ZONES, "Zona di collaudo");
        action(ZONES, id, "activate")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("«Zona di collaudo» è già attiva: rinomina questa voce prima di riattivarla"));
        action(ZONES, twin, "deactivate").andExpect(status().isOk());
        action(ZONES, id, "activate").andExpect(status().isOk()).andExpect(jsonPath("$.active").value(true));
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.ZONE,
                TsidMapper.toInternal(id)))
                .extracting(e -> e.getAction()).containsExactly("CREATE", "DEACTIVATE", "ACTIVATE");
    }

    @Test
    void soloLAdmin_ecatalogoSconosciuto404() throws Exception {
        mockMvc.perform(get(ZONES).header(USER, "operatore.cc")).andExpect(status().isForbidden());
        mockMvc.perform(post(CATEGORIES).header(USER, "operatore.cc").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Prova\"}")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/catalogs/projects").header(USER, "admin")).andExpect(status().isNotFound());
    }

    @Test
    void tipiDiTirocinio_vociIniziali_gestiteComeGliAltriCataloghi() throws Exception {
        mockMvc.perform(get("/api/v1/catalogs/internship-types").header(USER, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].code", hasItems("CURRICULARE", "EXTRACURRICULARE",
                        "INCLUSIONE", "SOCIO_TERAPEUTICO")));
        String id = JsonPath.read(create("/api/v1/catalogs/internship-types", "Estivo")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("ESTIVO"))
                .andReturn().getResponse().getContentAsString(), "$.id");
        // il Tutor li sceglie nella scheda del tirocinio: solo i tipi attivi
        action("/api/v1/catalogs/internship-types", id, "deactivate").andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/reference/internship-types").header(USER, "tutor1"))
                .andExpect(jsonPath("$[*].name", hasItem("Di inclusione")))
                .andExpect(jsonPath("$[*].name", not(hasItem("Estivo"))));
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.INTERNSHIP_TYPE,
                TsidMapper.toInternal(id)))
                .extracting(e -> e.getAction()).containsExactly("CREATE", "DEACTIVATE");
    }
}
