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

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.security.MockHeaderAuthenticationFilter;
import it.nexus.domain.Project;
import it.nexus.mapper.TsidMapper;
import it.nexus.support.WorkflowScenarios;
import jakarta.persistence.EntityManager;

/** Cataloghi per i documenti reali (US-115): sedi di competenza, tipi per progetto, enti di presa in carico. */
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, WorkflowScenarios.class})
class DocumentCatalogIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;
    private static final String BASE = "/api/v1/document-catalogs";
    /** Voci iniziali della V34. */
    private static final String FIRENZE = TsidMapper.toExternal(893700000000000341L);
    private static final String COMUNE = TsidMapper.toExternal(893700000000000351L);
    private static final String EXTRA = "EXTRACURRICULARE";

    @Autowired MockMvc mockMvc;
    @Autowired WorkflowScenarios scenarios;
    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManager em;

    private Project gol;
    private Project polis;

    @BeforeEach
    void setUp() {
        scenarios.actors();
        gol = scenarios.project("GOLX");
        polis = scenarios.project("POLISX");
    }

    private ResultActions putJson(String user, String path, String json) throws Exception {
        return mockMvc.perform(put(BASE + path).header(USER, user).contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private static String id(Project p) {
        return TsidMapper.toExternal(p.getId());
    }

    private String typeId(String code) {
        return TsidMapper.toExternal(jdbc.queryForObject("SELECT id FROM internship_type WHERE code = ?", Long.class,
                code));
    }

    private int audits(String entityType) {
        return jdbc.queryForObject("SELECT count(*) FROM audit_event WHERE entity_type = ?", Integer.class,
                entityType);
    }

    @Test
    void sediDiCompetenza_vociInizialiEProgetti() throws Exception {
        mockMvc.perform(get(BASE + "/competence-sites").header(USER, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'Firenze')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Borgo San Lorenzo')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Sesto Fiorentino')]").exists());

        int before = audits("COMPETENCE_SITE");
        putJson("admin", "/competence-sites/" + FIRENZE + "/projects",
                "{\"ids\": [\"%s\", \"%s\"]}".formatted(id(gol), id(polis)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projects.length()").value(2));
        assertThat(audits("COMPETENCE_SITE")).isEqualTo(before + 1);

        // un progetto disattivato non si aggiunge, ma resta se già collegato
        polis.setActive(false);
        Project other = scenarios.project("OFFX");
        other.setActive(false);
        em.flush();
        putJson("admin", "/competence-sites/" + FIRENZE + "/projects",
                "{\"ids\": [\"%s\", \"%s\"]}".formatted(id(polis), id(other)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("projectIds"));
    }

    @Test
    void sedeNuovaDalCatalogoGenerico() throws Exception {
        mockMvc.perform(post("/api/v1/catalogs/competence-sites").header(USER, "admin")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Empoli\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("EMPOLI"));
        mockMvc.perform(get(BASE + "/competence-sites").header(USER, "admin"))
                .andExpect(jsonPath("$[?(@.name == 'Empoli')].projects.length()").value(0));
    }

    @Test
    void tipiPerProgetto_impostatiEPoiTolti() throws Exception {
        String inclusione = typeId("INCLUSIONE");
        putJson("admin", "/projects/" + id(gol) + "/internship-types",
                "{\"ids\": [\"%s\", \"%s\"]}".formatted(typeId(EXTRA), inclusione))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.internshipTypes[0].code").value("INCLUSIONE"))
                .andExpect(jsonPath("$.internshipTypes[1].code").value(EXTRA));
        mockMvc.perform(get(BASE + "/project-internship-types").header(USER, "admin"))
                .andExpect(jsonPath("$[?(@.project.id == '%s')].internshipTypes.length()".formatted(id(gol)))
                        .value(2));
        putJson("admin", "/projects/" + id(gol) + "/internship-types", "{\"ids\": []}")
                .andExpect(jsonPath("$.internshipTypes.length()").value(0));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM audit_event WHERE entity_type = 'PROJECT' "
                + "AND entity_id = ? AND action = 'UPDATE'", Integer.class, gol.getId())).isEqualTo(2);
    }

    @Test
    void enti_vociInizialiEDati() throws Exception {
        mockMvc.perform(get(BASE + "/care-agencies").header(USER, "admin"))
                .andExpect(jsonPath("$[?(@.id == '%s')].kind".formatted(COMUNE)).value("COMUNE"))
                .andExpect(jsonPath("$[?(@.name == 'Società della Salute Fiorentina Nord Ovest')].kind")
                        .value("SOCIETA_DELLA_SALUTE"));

        putJson("admin", "/care-agencies/" + COMUNE, """
                {"kind": "COMUNE", "fiscalCode": "01307110484", "legalAddress": "Piazza della Signoria 1, Firenze",
                 "representativeName": "Maria Prova", "representativeRole": "Dirigente",
                 "referentName": "Maria Prova", "referentPhone": "055 1234567",
                 "referentEmail": "maria.prova@esempio.it", "version": 0}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fiscalCode").value("01307110484"))
                .andExpect(jsonPath("$.referentEmail").value("maria.prova@esempio.it"))
                .andExpect(jsonPath("$.version").value(1));
        // nell'audit i dati delle persone non compaiono
        em.flush();
        String changes = jdbc.queryForObject("SELECT changes::text FROM audit_event WHERE entity_type = "
                + "'CARE_AGENCY' AND action = 'UPDATE' ORDER BY created_at DESC LIMIT 1", String.class);
        assertThat(changes).contains("01307110484").doesNotContain("Maria").doesNotContain("esempio.it");

        putJson("admin", "/care-agencies/" + COMUNE, """
                {"kind": "COMUNE", "referentEmail": "non-una-mail", "representativeBirthDate": "2999-01-01",
                 "version": 1}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.length()").value(2));
        putJson("admin", "/care-agencies/" + COMUNE, "{\"kind\": \"COMUNE\", \"version\": 0}")
                .andExpect(status().isConflict());
    }

    @Test
    void soloLAdmin() throws Exception {
        mockMvc.perform(get(BASE + "/care-agencies").header(USER, "tutor1")).andExpect(status().isForbidden());
        putJson("operatore.cc", "/projects/" + id(gol) + "/internship-types", "{\"ids\": []}")
                .andExpect(status().isForbidden());
        putJson("tutor1", "/care-agencies/" + COMUNE, "{\"kind\": \"COMUNE\", \"version\": 0}")
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/catalogs/care-agencies").header(USER, "tutor1")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Ente\"}"))
                .andExpect(status().isForbidden());
    }
}
