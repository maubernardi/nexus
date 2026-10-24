package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Year;
import java.util.Map;

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
import it.nexus.domain.AppUser;
import it.nexus.domain.Beneficiary;
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

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class BeneficiaryResourceIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;

    @Autowired MockMvc mockMvc;
    @Autowired AppUserRepository users;
    @Autowired ZoneRepository zones;
    @Autowired AuditEventRepository audit;
    @Autowired JdbcTemplate jdbc;
    @Autowired ProjectRepository projects;
    @Autowired UserProjectRepository userProjects;
    @Autowired JobCategoryRepository categories;
    @Autowired BeneficiaryRepository beneficiaries;
    @Autowired TicketRepository tickets;

    private String zoneId;
    private String inactiveZoneId;

    @BeforeEach
    void setUp() {
        users.save(TestEntities.user("tutor1", Role.TUTOR, "00000000-0000-4000-8000-000000000001"));
        users.save(TestEntities.user("tutor2", Role.TUTOR, "00000000-0000-4000-8000-000000000002"));
        users.save(TestEntities.user("operatore.cc", Role.CALL_CENTER, "00000000-0000-4000-8000-000000000003"));
        zoneId = TsidMapper.toExternal(zones.save(TestEntities.zone("ZNORD")).getId());
        Zone inactive = TestEntities.zone("ZCHIUSA");
        inactive.setActive(false);
        inactiveZoneId = TsidMapper.toExternal(zones.saveAndFlush(inactive).getId());
    }

    private String body(String overrides) {
        return """
                {"firstName": "Mario", "lastName": "Rossi", "birthYear": 1998, "gender": "M",
                 "nationality": "IT", "citizenship": "IT", "residenceZoneId": "%s",
                 "licenseTypes": ["B", "CQC"], "hasVehicle": true, "transportMode": "AUTO_PROPRIA",
                 "hasLaw68": false, "educationLevel": "DIPLOMA", "constraints": "  ",
                 "languages": [{"language": "it", "level": "MADRELINGUA"}, {"language": "en", "level": "B1"}] %s}
                """.formatted(zoneId, overrides);
    }

    private ResultActions register(String user, String json) throws Exception {
        return mockMvc.perform(post("/api/v1/beneficiaries").header(USER, user).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void registrazioneRiuscita_proprietarioETipiDiPatente() throws Exception {
        String response = register("tutor1", body(""))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/beneficiaries/")))
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.hasDrivingLicense").value(true))
                .andExpect(jsonPath("$.licenseTypes").value(containsInAnyOrder("B", "CQC")))
                .andExpect(jsonPath("$.constraints").doesNotExist())
                .andExpect(jsonPath("$.residenceZone.code").value("ZNORD"))
                .andExpect(jsonPath("$.languages", hasSize(2)))
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(response, "$.id");

        mockMvc.perform(get("/api/v1/beneficiaries/" + id).header(USER, "tutor1")).andExpect(status().isOk());
    }

    @Test
    void cognomeMancante_400ConErroreSulCampo() throws Exception {
        register("tutor1", body("").replace("\"lastName\": \"Rossi\",", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("lastName"));
    }

    @Test
    void annoDiNascitaFuturo_400() throws Exception {
        register("tutor1", body("").replace("1998", String.valueOf(Year.now().getValue() + 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("birthYear"));
    }

    @Test
    void nazionalitaNonIso_400() throws Exception {
        register("tutor1", body("").replace("\"nationality\": \"IT\"", "\"nationality\": \"Italia\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("nationality"));
    }

    @Test
    void linguaRipetuta_400() throws Exception {
        register("tutor1", body("").replace("\"language\": \"en\"", "\"language\": \"it\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("languages"));
    }

    @Test
    void zonaDisattivata_400() throws Exception {
        register("tutor1", body("").replace(zoneId, inactiveZoneId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("residenceZoneId"));
    }

    @Test
    void callCenterNonPuoRegistrare_403() throws Exception {
        register("operatore.cc", body("")).andExpect(status().isForbidden());
    }

    @Test
    void elencoSoloDeiPropriBeneficiari() throws Exception {
        register("tutor1", body("")).andExpect(status().isCreated());
        register("tutor2", body("").replace("Mario", "Luigi")).andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/beneficiaries").header(USER, "tutor1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName").value("Mario"))
                .andExpect(jsonPath("$[0].residenceZoneName").value("Zona ZNORD"));
    }

    @Test
    void dettaglioAltrui_404PerAltroTutor_200PerCallCenter() throws Exception {
        String id = JsonPath.read(register("tutor1", body("")).andReturn().getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/api/v1/beneficiaries/" + id).header(USER, "tutor2")).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/beneficiaries/" + id).header(USER, "operatore.cc")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/beneficiaries/non-un-id").header(USER, "operatore.cc")).andExpect(status().isNotFound());
    }

    @Test
    void zoneDiRiferimento_soloAttive() throws Exception {
        mockMvc.perform(get("/api/v1/reference/zones").header(USER, "tutor1"))
                .andExpect(status().isOk())
                // le zone iniziali (V11) più quella attiva del test; mai quella disattivata
                .andExpect(jsonPath("$[*].code").value(hasItems("ZNORD", "NOVOLI", "FIESOLE")))
                .andExpect(jsonPath("$[*].code").value(not(hasItem("ZCHIUSA"))));
    }

    @Test
    void registrazione_auditSenzaDatiPersonali() throws Exception {
        String id = JsonPath.read(register("tutor1", body("")).andReturn().getResponse().getContentAsString(), "$.id");

        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.BENEFICIARY,
                TsidMapper.toInternal(id)))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.getAction()).isEqualTo("CREATE");
                    assertThat(event.getCreatedBy()).isEqualTo("tutor1");
                    assertThat(event.getChanges()).isEmpty();
                    assertThat(event.getReason()).isNull();
                });
        // nessuna traccia dei valori personali nel registro
        assertThat(jdbc.queryForObject("SELECT count(*) FROM audit_event WHERE changes::text LIKE '%Rossi%'", Long.class))
                .isZero();
    }

    private ResultActions update(String user, String id, String json) throws Exception {
        return mockMvc.perform(put("/api/v1/beneficiaries/" + id).header(USER, user).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private String registered(String user) throws Exception {
        return JsonPath.read(register(user, body("")).andReturn().getResponse().getContentAsString(), "$.id");
    }

    @Test
    void modificaDelProprietario_campiELingueAggiornati() throws Exception {
        String id = registered("tutor1");
        String changed = body("").replace("Mario", "Luigi").replace("[\"B\", \"CQC\"]", "[]")
                .replace("{\"language\": \"en\", \"level\": \"B1\"}", "{\"language\": \"fr\", \"level\": \"A2\"}")
                .replace("\"MADRELINGUA\"", "\"C2\"");

        update("tutor1", id, changed)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Luigi"))
                .andExpect(jsonPath("$.hasDrivingLicense").value(false))
                .andExpect(jsonPath("$.languages[*].language").value(containsInAnyOrder("it", "fr")))
                .andExpect(jsonPath("$.languages[?(@.language == 'it')].level").value(hasItem("C2")))
                .andExpect(jsonPath("$.editable").value(true));
    }

    @Test
    void modifica_auditConISoliNomiDeiCampi() throws Exception {
        String id = registered("tutor1");
        update("tutor1", id, body("").replace("Mario", "Luigi").replace("\"hasLaw68\": false", "\"hasLaw68\": true"))
                .andExpect(status().isOk());
        // nessuna modifica: nessun nuovo evento
        update("tutor1", id, body("").replace("Mario", "Luigi").replace("\"hasLaw68\": false", "\"hasLaw68\": true"))
                .andExpect(status().isOk());

        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.BENEFICIARY,
                TsidMapper.toInternal(id)))
                .extracting(e -> e.getAction())
                .containsExactly("CREATE", "UPDATE");
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.BENEFICIARY,
                TsidMapper.toInternal(id)).get(1).getChanges())
                .containsOnlyKeys("firstName", "hasLaw68")
                .containsEntry("firstName", Map.of("redacted", true));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM audit_event WHERE changes::text LIKE '%Luigi%'", Long.class))
                .isZero();
    }

    @Test
    void modifica_soloIlProprietario() throws Exception {
        String id = registered("tutor1");
        update("tutor2", id, body("")).andExpect(status().isNotFound());
        shareProject("tutor1", "tutor2", id);

        update("tutor2", id, body("")).andExpect(status().isForbidden());
        update("operatore.cc", id, body("")).andExpect(status().isForbidden());
    }

    @Test
    void modifica_zonaGiaAssegnataAncheSeDisattivata() throws Exception {
        String id = registered("tutor1");
        zones.findById(TsidMapper.toInternal(zoneId)).orElseThrow().setActive(false);

        update("tutor1", id, body("").replace("Mario", "Luigi")).andExpect(status().isOk());
        update("tutor1", id, body("").replace(zoneId, inactiveZoneId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("residenceZoneId"));
    }

    @Test
    void visibilita_tutorDelProgettoVedeIDatiCompletiInSolaLettura() throws Exception {
        String id = registered("tutor1");
        // un Tutor di un altro progetto non lo vede
        Project other = projects.save(TestEntities.project("QALTRO"));
        userProjects.save(new UserProject(users.findByUsername("tutor2").orElseThrow(), other));
        mockMvc.perform(get("/api/v1/beneficiaries/" + id).header(USER, "tutor2")).andExpect(status().isNotFound());

        shareProject("tutor1", "tutor2", id);
        mockMvc.perform(get("/api/v1/beneficiaries/" + id).header(USER, "tutor2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Mario"))
                .andExpect(jsonPath("$.lastName").value("Rossi"))
                .andExpect(jsonPath("$.editable").value(false));
        mockMvc.perform(get("/api/v1/beneficiaries/" + id).header(USER, "tutor1"))
                .andExpect(jsonPath("$.editable").value(true))
                .andExpect(jsonPath("$.ownerTutorName").isString());
        // "I miei beneficiari" resta solo del proprietario
        mockMvc.perform(get("/api/v1/beneficiaries").header(USER, "tutor2")).andExpect(jsonPath("$", hasSize(0)));
    }

    /** Segnalazione del beneficiario nel progetto QSHARE, a cui sono assegnati il proprietario e il collega. */
    private void shareProject(String owner, String colleague, String beneficiaryId) {
        AppUser ownerUser = users.findByUsername(owner).orElseThrow();
        Project project = projects.save(TestEntities.project("QSHARE"));
        userProjects.save(new UserProject(ownerUser, project));
        userProjects.save(new UserProject(users.findByUsername(colleague).orElseThrow(), project));
        JobCategory category = categories.save(TestEntities.jobCategory("QCAT"));
        Beneficiary beneficiary = beneficiaries.findById(TsidMapper.toInternal(beneficiaryId)).orElseThrow();
        tickets.saveAndFlush(TestEntities.ticket(ownerUser, project, beneficiary, category));
    }
}
