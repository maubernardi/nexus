package it.nexus.web.rest.resource;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Year;

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
import it.nexus.domain.TestEntities;
import it.nexus.domain.Zone;
import it.nexus.domain.enumeration.Role;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.ZoneRepository;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class CandidateResourceIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;

    @Autowired MockMvc mockMvc;
    @Autowired AppUserRepository users;
    @Autowired ZoneRepository zones;

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
        return mockMvc.perform(post("/api/v1/candidates").header(USER, user).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void registrazioneRiuscita_proprietarioETipiDiPatente() throws Exception {
        String response = register("tutor1", body(""))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/candidates/")))
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.hasDrivingLicense").value(true))
                .andExpect(jsonPath("$.licenseTypes").value(containsInAnyOrder("B", "CQC")))
                .andExpect(jsonPath("$.constraints").doesNotExist())
                .andExpect(jsonPath("$.residenceZone.code").value("ZNORD"))
                .andExpect(jsonPath("$.languages", hasSize(2)))
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(response, "$.id");

        mockMvc.perform(get("/api/v1/candidates/" + id).header(USER, "tutor1")).andExpect(status().isOk());
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
    void elencoSoloDeiPropriCandidati() throws Exception {
        register("tutor1", body("")).andExpect(status().isCreated());
        register("tutor2", body("").replace("Mario", "Luigi")).andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/candidates").header(USER, "tutor1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName").value("Mario"))
                .andExpect(jsonPath("$[0].residenceZoneName").value("Zona ZNORD"));
    }

    @Test
    void dettaglioAltrui_404PerAltroTutor_200PerCallCenter() throws Exception {
        String id = JsonPath.read(register("tutor1", body("")).andReturn().getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/api/v1/candidates/" + id).header(USER, "tutor2")).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/candidates/" + id).header(USER, "operatore.cc")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/candidates/non-un-id").header(USER, "operatore.cc")).andExpect(status().isNotFound());
    }

    @Test
    void zoneDiRiferimento_soloAttive() throws Exception {
        mockMvc.perform(get("/api/v1/reference/zones").header(USER, "tutor1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].code").value(containsInAnyOrder("ZNORD")));
    }
}
