package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import it.nexus.domain.Company;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AuditEventRepository;
import it.nexus.support.WorkflowScenarios;
import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, WorkflowScenarios.class})
/** Legale rappresentante e codici ATECO dell'azienda (US-506). */
class CompanyLegalDataIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired WorkflowScenarios scenarios;
    @Autowired AuditEventRepository audit;

    private Company company;
    private String base;

    @BeforeEach
    void setUp() {
        scenarios.actors();
        company = scenarios.place().company();
        base = "/api/v1/companies/" + TsidMapper.toExternal(company.getId());
        em.flush();
    }

    private ResultActions saveRep(String user, String json) throws Exception {
        return mockMvc.perform(put(base + "/legal-representative").header(USER, user)
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private static String rep(String fiscalCode, String birthDate, Long version) {
        return """
                {"firstName": "Laura", "lastName": "Bianchi", "fiscalCode": %s, "birthDate": %s,
                 "birthPlace": "Roma", "residence": "Via Roma 1, Firenze", "version": %s}
                """.formatted(fiscalCode == null ? "null" : "\"" + fiscalCode + "\"",
                birthDate == null ? "null" : "\"" + birthDate + "\"", version == null ? "null" : version);
    }

    @Test
    void legaleRappresentante_primaRegistrazioneECorrezione_auditSenzaValori() throws Exception {
        mockMvc.perform(get(base + "/legal-representative").header(USER, "operatore.cc"))
                .andExpect(status().isNoContent());

        saveRep("operatore.cc", rep("bnclra80a41h501d", "1980-01-01", null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fiscalCode").value("BNCLRA80A41H501D"))
                .andExpect(jsonPath("$.version").value(0));
        saveRep("admin", rep("BNCLRA80A41H501D", "1980-01-01", 0L).replace("Via Roma 1", "Via Dante 2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.residence").value("Via Dante 2, Firenze"))
                .andExpect(jsonPath("$.version").value(1));
        mockMvc.perform(get(base + "/legal-representative").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$.lastName").value("Bianchi"));

        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.COMPANY, company.getId()))
                .extracting(e -> e.getAction()).containsExactly("LEGAL_REPRESENTATIVE", "LEGAL_REPRESENTATIVE");
        // dati personali: solo che il campo è cambiato, mai il valore
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.COMPANY, company.getId())
                .get(1).getChanges())
                .containsOnlyKeys("residence")
                .containsEntry("residence", Map.of("redacted", true));
    }

    @Test
    void legaleRappresentante_versioneEDatiNonValidi() throws Exception {
        saveRep("operatore.cc", rep(null, null, 3L)).andExpect(status().isConflict());
        saveRep("operatore.cc", rep(null, null, null)).andExpect(status().isOk());
        // esiste già: senza versione o con una superata è un conflitto
        saveRep("operatore.cc", rep(null, null, null)).andExpect(status().isConflict());
        saveRep("operatore.cc", rep(null, null, 5L)).andExpect(status().isConflict());

        saveRep("operatore.cc", rep("RSSMRA85T10A562T", null, 0L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("fiscalCode"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Codice fiscale non valido"));
        saveRep("operatore.cc", rep(null, "2999-01-01", 0L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("birthDate"));
        saveRep("operatore.cc", rep(null, null, 0L).replace("\"Laura\"", "\" \""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("firstName"));
    }

    private ResultActions addAteco(String code, String description) throws Exception {
        return mockMvc.perform(post(base + "/ateco-codes").header(USER, "operatore.cc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\": \"%s\", \"description\": %s}".formatted(code,
                        description == null ? "null" : "\"" + description + "\"")));
    }

    @Test
    void codiciAteco_aggiuntaElencoRimozioneConAudit() throws Exception {
        String id = JsonPath.read(addAteco("56.10.11", "Ristorazione con somministrazione")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("56.10.11"))
                .andReturn().getResponse().getContentAsString(), "$.id");
        addAteco(" 47.11 ", null).andExpect(status().isCreated());
        mockMvc.perform(get(base + "/ateco-codes").header(USER, "admin"))
                .andExpect(jsonPath("$[*].code", contains("47.11", "56.10.11")));

        addAteco("56.10.11", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("code"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("L'azienda ha già il codice 56.10.11"));
        addAteco("5610", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("code"));

        mockMvc.perform(delete(base + "/ateco-codes/" + id).header(USER, "operatore.cc"))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(base + "/ateco-codes").header(USER, "admin"))
                .andExpect(jsonPath("$[*].code", contains("47.11")));
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.COMPANY, company.getId()))
                .extracting(e -> e.getAction()).containsExactly("ATECO_ADD", "ATECO_ADD", "ATECO_REMOVE");

        // un codice di un'altra azienda non si tocca da qui
        Company other = scenarios.place().company();
        em.flush();
        String otherId = JsonPath.read(mockMvc.perform(post("/api/v1/companies/" + TsidMapper.toExternal(other.getId())
                        + "/ateco-codes").header(USER, "operatore.cc").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\": \"10.71\"}"))
                .andReturn().getResponse().getContentAsString(), "$.id");
        mockMvc.perform(delete(base + "/ateco-codes/" + otherId).header(USER, "operatore.cc"))
                .andExpect(status().isNotFound());
    }

    @Test
    void soloCallCenterEAdmin() throws Exception {
        mockMvc.perform(get(base + "/legal-representative").header(USER, "tutor1")).andExpect(status().isForbidden());
        mockMvc.perform(get(base + "/ateco-codes").header(USER, "tutor1")).andExpect(status().isForbidden());
        saveRep("tutor1", rep(null, null, null)).andExpect(status().isForbidden());
    }
}
