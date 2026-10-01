package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
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
import it.nexus.domain.TestEntities;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.Role;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.AuditEventRepository;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class CompanyResourceIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;
    private static final String API = "/api/v1/companies";

    @Autowired MockMvc mockMvc;
    @Autowired AppUserRepository users;
    @Autowired AuditEventRepository audit;

    @BeforeEach
    void setUp() {
        users.save(TestEntities.user("tutor1", Role.TUTOR, "00000000-0000-4000-8000-000000000001"));
        users.save(TestEntities.user("operatore.cc", Role.CALL_CENTER, "00000000-0000-4000-8000-000000000003"));
        users.save(TestEntities.user("admin", Role.ADMIN, "00000000-0000-4000-8000-000000000004"));
    }

    private static String body(String name, String vat, String extra) {
        return """
                {"name": "%s", "vatCode": "%s", "legalAddress": "Via Roma 1, Firenze", "contactPerson": "Paola Neri",
                 "phone": "+39 055 123456", "email": "info@azienda.test" %s}
                """.formatted(name, vat, extra);
    }

    private ResultActions create(String user, String json) throws Exception {
        return mockMvc.perform(post(API).header(USER, user).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private String createdId(String name, String vat) throws Exception {
        return JsonPath.read(create("operatore.cc", body(name, vat, "")).andReturn().getResponse().getContentAsString(), "$.id");
    }

    @Test
    void creazione_201EAudit() throws Exception {
        String id = JsonPath.read(create("operatore.cc", body("Officina Rossi", " 12345678901 ", ""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vatCode").value("12345678901"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.version").value(0))
                .andReturn().getResponse().getContentAsString(), "$.id");

        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.COMPANY, TsidMapper.toInternal(id)))
                .singleElement()
                .satisfies(e -> {
                    assertThat(e.getAction()).isEqualTo("CREATE");
                    assertThat(e.getCreatedBy()).isEqualTo("operatore.cc");
                    assertThat(e.getChanges()).containsKeys("name", "vatCode", "contactPerson");
                });
    }

    @Test
    void partitaIvaDuplicataONonValida_400SulCampo() throws Exception {
        createdId("Prima", "12345678901");
        create("operatore.cc", body("Seconda", "12345678901", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("vatCode"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Esiste già un'azienda con questa partita IVA"));
        create("operatore.cc", body("Terza", "1234ABC", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("vatCode"));
    }

    @Test
    void ricercaPerNomeOPartitaIva_disattivateSoloSeRichieste() throws Exception {
        createdId("Panificio Bianchi", "11111111111");
        String id = createdId("Bianchi Logistica", "22222222222");
        createdId("Officina Verdi", "33333333333");
        mockMvc.perform(post(API + "/" + id + "/deactivate").header(USER, "operatore.cc")
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\": 0}")).andExpect(status().isOk());

        mockMvc.perform(get(API).param("search", "BIANCHI").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$[*].name", contains("Panificio Bianchi")));
        mockMvc.perform(get(API).param("search", "bianchi").param("includeInactive", "true").header(USER, "admin"))
                .andExpect(jsonPath("$[*].name", contains("Bianchi Logistica", "Panificio Bianchi")));
        mockMvc.perform(get(API).param("search", "3333").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$[*].name", contains("Officina Verdi")));
    }

    @Test
    void modifica_soloCampiCambiatiNellAudit_versioneSuperata409() throws Exception {
        String id = createdId("Officina Rossi", "12345678901");
        mockMvc.perform(put(API + "/" + id).header(USER, "operatore.cc").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Officina Rossi S.r.l.", "12345678901", ", \"version\": 0")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Officina Rossi S.r.l."))
                .andExpect(jsonPath("$.version").value(1));

        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.COMPANY, TsidMapper.toInternal(id)))
                .last()
                .satisfies(e -> {
                    assertThat(e.getAction()).isEqualTo("UPDATE");
                    assertThat(e.getChanges()).containsOnlyKeys("name")
                            .containsEntry("name", Map.of("before", "Officina Rossi", "after", "Officina Rossi S.r.l."));
                });

        mockMvc.perform(put(API + "/" + id).header(USER, "admin").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Altro nome", "12345678901", ", \"version\": 0")))
                .andExpect(status().isConflict());
    }

    @Test
    void modificaConPartitaIvaDiUnAltra_400() throws Exception {
        createdId("Prima", "11111111111");
        String id = createdId("Seconda", "22222222222");
        mockMvc.perform(put(API + "/" + id).header(USER, "operatore.cc").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Seconda", "11111111111", ", \"version\": 0")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("vatCode"));
    }

    @Test
    void disattivazioneERiattivazione_tracciate() throws Exception {
        String id = createdId("Officina Rossi", "12345678901");
        mockMvc.perform(post(API + "/" + id + "/deactivate").header(USER, "operatore.cc")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"version\": 0}"))
                .andExpect(jsonPath("$.active").value(false));
        mockMvc.perform(get(API).header(USER, "operatore.cc")).andExpect(jsonPath("$[*].id", not(hasItem(id))));
        mockMvc.perform(post(API + "/" + id + "/activate").header(USER, "operatore.cc")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"version\": 1}"))
                .andExpect(jsonPath("$.active").value(true));

        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.COMPANY, TsidMapper.toInternal(id)))
                .extracting(e -> e.getAction()).containsExactly("CREATE", "DEACTIVATE", "ACTIVATE");
    }

    @Test
    void tutor403_inesistente404() throws Exception {
        create("tutor1", body("X", "12345678901", "")).andExpect(status().isForbidden());
        mockMvc.perform(get(API).header(USER, "tutor1")).andExpect(status().isForbidden());
        mockMvc.perform(get(API + "/0000000000000").header(USER, "operatore.cc")).andExpect(status().isNotFound());
    }
}
