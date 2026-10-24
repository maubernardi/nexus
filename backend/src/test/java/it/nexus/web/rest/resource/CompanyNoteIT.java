package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.security.MockHeaderAuthenticationFilter;
import it.nexus.domain.AppUser;
import it.nexus.domain.Company;
import it.nexus.domain.TestEntities;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.Role;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.AuditEventRepository;
import it.nexus.repository.CompanyRepository;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class CompanyNoteIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;

    @Autowired MockMvc mockMvc;
    @Autowired AppUserRepository users;
    @Autowired CompanyRepository companies;
    @Autowired AuditEventRepository audit;
    @Autowired JdbcTemplate jdbc;

    private Company company;
    private String notesApi;

    @BeforeEach
    void setUp() {
        users.save(TestEntities.user("tutor1", Role.TUTOR, "00000000-0000-4000-8000-000000000001"));
        AppUser operator = TestEntities.user("operatore.cc", Role.CALL_CENTER, "00000000-0000-4000-8000-000000000003");
        operator.setFirstName("Paolo");
        operator.setLastName("Verdi");
        users.save(operator);
        users.save(TestEntities.user("admin", Role.ADMIN, "00000000-0000-4000-8000-000000000004"));
        company = companies.saveAndFlush(TestEntities.company("98700000001"));
        notesApi = "/api/v1/companies/" + TsidMapper.toExternal(company.getId()) + "/notes";
    }

    private ResultActions add(String user, String text) throws Exception {
        return mockMvc.perform(post(notesApi).header(USER, user).contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\": \"%s\"}".formatted(text)));
    }

    @Test
    void aggiuntaEDiarioDallaPiuRecente() throws Exception {
        add("operatore.cc", "  Referente disponibile solo al mattino  ")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.text").value("Referente disponibile solo al mattino"))
                .andExpect(jsonPath("$.authorName").value("Paolo Verdi"))
                .andExpect(jsonPath("$.createdAt").isString());
        add("admin", "Chiusura per ferie ad agosto").andExpect(status().isCreated());

        mockMvc.perform(get(notesApi).header(USER, "operatore.cc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].text").value("Chiusura per ferie ad agosto"))
                .andExpect(jsonPath("$[0].authorName").value("Nome Cognome"))
                .andExpect(jsonPath("$[1].authorName").value("Paolo Verdi"));
    }

    @Test
    void aggiunta_auditSenzaTesto() throws Exception {
        add("operatore.cc", "Contatto riservato del titolare").andExpect(status().isCreated());

        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.COMPANY, company.getId()))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.getAction()).isEqualTo("NOTE_ADD");
                    assertThat(event.getCreatedBy()).isEqualTo("operatore.cc");
                    assertThat(event.getChanges()).isEmpty();
                });
        assertThat(jdbc.queryForObject("SELECT count(*) FROM audit_event WHERE changes::text LIKE '%titolare%'"
                + " OR reason LIKE '%titolare%'", Long.class)).isZero();
    }

    @Test
    void testoVuotoOTroppoLungo_400() throws Exception {
        add("operatore.cc", "   ").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("text"));
        add("operatore.cc", "x".repeat(2001)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("text"));
    }

    @Test
    void soloCallCenterEAdmin() throws Exception {
        add("tutor1", "Nota").andExpect(status().isForbidden());
        mockMvc.perform(get(notesApi).header(USER, "tutor1")).andExpect(status().isForbidden());
    }

    @Test
    void aziendaInesistente_404() throws Exception {
        mockMvc.perform(get("/api/v1/companies/non-un-id/notes").header(USER, "operatore.cc"))
                .andExpect(status().isNotFound());
    }
}
