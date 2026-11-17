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

/**
 * Cataloghi per i documenti reali (US-115, US-1011): sedi di competenza, tipi per progetto, dati dei documenti dei
 * progetti, enti di presa in carico.
 */
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
    private static final String HEADER = "Firenze in Rete 2 – Codice 9014358 – CUP B81D24000440006";

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
    void datiDeiDocumentiDelProgetto_avvisoPredefinitoEIntestazione() throws Exception {
        String gol = "$[?(@.project.id == '%s')]".formatted(id(this.gol));
        mockMvc.perform(get(BASE + "/project-documents").header(USER, "admin"))
                .andExpect(jsonPath(gol + ".defaultNotice").value(true))
                .andExpect(jsonPath(gol + ".noticeReference").value(Project.DEFAULT_NOTICE_REFERENCE))
                .andExpect(jsonPath(gol + ".headerLine").isEmpty());

        String path = "/projects/" + id(this.gol) + "/documents";
        putJson("admin", path, "{\"noticeReference\": \" Avviso pubblico n. 4 \", \"headerLine\": \" %s \"}"
                .formatted(HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noticeReference").value("Avviso pubblico n. 4"))
                .andExpect(jsonPath("$.defaultNotice").value(false))
                .andExpect(jsonPath("$.headerLine").value(HEADER));
        // vuoto (o uguale al predefinito) = di nuovo il testo predefinito
        putJson("admin", path, "{\"noticeReference\": \"  \", \"headerLine\": \"%s\"}".formatted(HEADER))
                .andExpect(jsonPath("$.noticeReference").value(Project.DEFAULT_NOTICE_REFERENCE))
                .andExpect(jsonPath("$.defaultNotice").value(true));
        putJson("admin", path, "{\"noticeReference\": \"%s\", \"headerLine\": \"%s\"}"
                .formatted(Project.DEFAULT_NOTICE_REFERENCE, HEADER))
                .andExpect(jsonPath("$.defaultNotice").value(true));
        assertThat(jdbc.queryForObject("SELECT notice_reference FROM project WHERE id = ?", String.class,
                this.gol.getId())).isNull();

        // due modifiche nell'audit del progetto, con i testi; l'ultimo salvataggio non cambiava nulla
        em.flush();
        assertThat(jdbc.queryForList("SELECT changes::text FROM audit_event WHERE entity_type = 'PROJECT' "
                + "AND entity_id = ? AND action = 'UPDATE' ORDER BY created_at, id", String.class, this.gol.getId()))
                .hasSize(2)
                .first().asString().contains("Avviso pubblico n. 4", "CUP B81D24000440006");

        putJson("admin", path, "{\"headerLine\": \"%s\"}".formatted("x".repeat(301)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("headerLine"));
    }

    @Test
    void promotoreDelProgetto_conAuditSenzaDatiPersonali() throws Exception {
        String path = "/projects/" + id(this.gol) + "/documents";
        putJson("admin", path, """
                {"headerLine": "%s", "promoter": {"name": " Cooperativa di prova ", "address": "Via della Prova 1",
                 "fiscalCode": "01234567890", "representative": "Paola Neri", "representativeRole": "Presidente",
                 "representativeBirthPlace": "Prato", "representativeBirthDate": "1970-02-01", "phone": "055 000000",
                 "email": "info@prova.example", "pec": "", "inailPosition": "123456", "rcInsurer": "Assicurazioni",
                 "rcPolicy": "RC-1", "rcExpiry": "2027-07-22"}}""".formatted(HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promoter.name").value("Cooperativa di prova"))
                .andExpect(jsonPath("$.promoter.representativeBirthDate").value("1970-02-01"))
                .andExpect(jsonPath("$.promoter.rcExpiry").value("2027-07-22"))
                .andExpect(jsonPath("$.promoter.pec").doesNotExist());
        assertThat(jdbc.queryForObject("SELECT promoter_rc_policy FROM project WHERE id = ?", String.class,
                this.gol.getId())).isEqualTo("RC-1");
        // nell'audit i dati dell'ente sì, quelli del legale rappresentante solo come «cambiati»
        em.flush();
        String changes = jdbc.queryForObject("SELECT changes::text FROM audit_event WHERE entity_type = 'PROJECT' "
                + "AND entity_id = ? AND action = 'UPDATE' ORDER BY created_at DESC, id DESC LIMIT 1", String.class,
                this.gol.getId());
        assertThat(changes).contains("RC-1", "promoter.representative").doesNotContain("Paola", "Prato", "1970");

        // senza promotore nel modulo i dati restano
        putJson("admin", path, "{\"headerLine\": \"%s\"}".formatted(HEADER))
                .andExpect(jsonPath("$.promoter.rcPolicy").value("RC-1"));
        putJson("admin", path, """
                {"promoter": {"email": "non-una-mail", "fiscalCode": "123", "representativeBirthDate": "2999-01-01"}}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field").value(org.hamcrest.Matchers.containsInAnyOrder(
                        "promoter.email", "promoter.fiscalCode", "promoter.representativeBirthDate")));
    }

    @Test
    void variabiliDelProgetto_aggiunteModificateTolte() throws Exception {
        String path = "/projects/" + id(this.gol) + "/documents";
        putJson("admin", path, """
                {"variables": [{"name": "SEDE_OPERATIVA", "value": " Via Roma 1, Firenze "},
                               {"name": "CODICE_ATS", "value": "ATS-7", "description": "Codice dell'ATS"}]}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.variables[*].name").value(org.hamcrest.Matchers.contains("CODICE_ATS",
                        "SEDE_OPERATIVA")))
                .andExpect(jsonPath("$.variables[1].value").value("Via Roma 1, Firenze"));
        putJson("admin", path, """
                {"variables": [{"name": "SEDE_OPERATIVA", "value": "Via Verdi 2, Firenze", "description": "Sede"}]}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.variables.length()").value(1))
                .andExpect(jsonPath("$.variables[0].value").value("Via Verdi 2, Firenze"))
                .andExpect(jsonPath("$.variables[0].description").value("Sede"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM project_variable WHERE project_id = ?", Long.class,
                this.gol.getId())).isOne();
        em.flush();
        assertThat(jdbc.queryForList("SELECT changes::text FROM audit_event WHERE entity_type = 'PROJECT' "
                + "AND entity_id = ? AND action = 'UPDATE' ORDER BY created_at, id", String.class, this.gol.getId()))
                .hasSize(2)
                .last().asString().contains("variable:CODICE_ATS", "Via Verdi 2, Firenze");

        // nome di NEXUS, ripetuto, non valido
        putJson("admin", path, "{\"variables\": [{\"name\": \"PROMOTORE\", \"value\": \"x\"}]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("variables[0].name"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value(org.hamcrest.Matchers.containsString(
                        "segnaposto di NEXUS")));
        putJson("admin", path, """
                {"variables": [{"name": "UNO", "value": "1"}, {"name": "UNO", "value": "2"}]}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("variables[1].name"));
        putJson("admin", path, "{\"variables\": [{\"name\": \"sede\", \"value\": \"\"}]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field").value(org.hamcrest.Matchers.containsInAnyOrder(
                        "variables[0].name", "variables[0].value")));
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
        mockMvc.perform(get(BASE + "/project-documents").header(USER, "tutor1")).andExpect(status().isForbidden());
        putJson("tutor1", "/projects/" + id(gol) + "/documents", "{\"headerLine\": \"CUP\"}")
                .andExpect(status().isForbidden());
        putJson("tutor1", "/care-agencies/" + COMUNE, "{\"kind\": \"COMUNE\", \"version\": 0}")
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/catalogs/care-agencies").header(USER, "tutor1")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Ente\"}"))
                .andExpect(status().isForbidden());
    }
}
