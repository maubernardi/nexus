package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.assertj.core.api.InstanceOfAssertFactories;

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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.security.MockHeaderAuthenticationFilter;
import it.nexus.domain.AuditEvent;
import it.nexus.domain.TestEntities;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.Role;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.AuditEventRepository;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class WorkCalendarIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;
    private static final String API = "/api/v1/calendar";

    @Autowired MockMvc mockMvc;
    @Autowired AppUserRepository users;
    @Autowired AuditEventRepository audit;

    @BeforeEach
    void setUp() {
        users.save(TestEntities.user("tutor1", Role.TUTOR, "00000000-0000-4000-8000-000000000001"));
        users.save(TestEntities.user("operatore.cc", Role.CALL_CENTER, "00000000-0000-4000-8000-000000000003"));
        users.save(TestEntities.user("admin", Role.ADMIN, "00000000-0000-4000-8000-000000000004"));
    }

    private ResultActions json(MockHttpServletRequestBuilder request, String body) throws Exception {
        return mockMvc.perform(request.header(USER, "admin").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private long version() throws Exception {
        Number v = JsonPath.read(mockMvc.perform(get(API).header(USER, "admin")).andReturn().getResponse()
                .getContentAsString(), "$.version");
        return v.longValue();
    }

    private List<AuditEvent> calendarAudit() {
        return audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.CALENDAR, 1L);
    }

    @Test
    void patronoIniziale24Giugno() throws Exception {
        mockMvc.perform(get(API).header(USER, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patronDay").value(24))
                .andExpect(jsonPath("$.patronMonth").value(6));
    }

    @Test
    void cambioPatrono_conAuditPrimaEDopo() throws Exception {
        json(put(API + "/patron"), "{\"day\": 29, \"month\": 6, \"version\": %d}".formatted(version()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patronDay").value(29));

        assertThat(calendarAudit()).singleElement().satisfies(e -> {
            assertThat(e.getAction()).isEqualTo("PATRON_UPDATE");
            assertThat(e.getChanges()).containsEntry("patron", Map.of("before", "--06-24", "after", "--06-29"));
            assertThat(e.getCreatedBy()).isEqualTo("admin");
        });
    }

    @Test
    void patronoInesistenteOVersioneSuperata() throws Exception {
        long v = version();
        json(put(API + "/patron"), "{\"day\": 30, \"month\": 2, \"version\": %d}".formatted(v))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("day"));
        json(put(API + "/patron"), "{\"day\": 1, \"month\": 13, \"version\": %d}".formatted(v))
                .andExpect(status().isBadRequest());
        json(put(API + "/patron"), "{\"day\": 1, \"month\": 7, \"version\": %d}".formatted(v + 5))
                .andExpect(status().isConflict());
    }

    @Test
    void chiusure_giornoEPeriodo_ordinateERimovibili() throws Exception {
        json(post(API + "/closures"), "{\"startDate\": \"2026-12-24\", \"description\": \"Vigilia di Natale\"}")
                .andExpect(status().isOk());
        String body = json(post(API + "/closures"),
                "{\"startDate\": \"2026-08-10\", \"endDate\": \"2026-08-21\", \"description\": \" Ferie estive \"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.closures", hasSize(2)))
                .andExpect(jsonPath("$.closures[0].startDate").value("2026-08-10"))
                .andExpect(jsonPath("$.closures[0].endDate").value("2026-08-21"))
                .andExpect(jsonPath("$.closures[0].description").value("Ferie estive"))
                .andExpect(jsonPath("$.closures[1].endDate").value("2026-12-24"))
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(body, "$.closures[0].id");

        mockMvc.perform(delete(API + "/closures/" + id).header(USER, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.closures", hasSize(1)));
        mockMvc.perform(delete(API + "/closures/" + id).header(USER, "admin")).andExpect(status().isNotFound());

        assertThat(calendarAudit()).extracting(e -> e.getAction())
                .containsExactly("CLOSURE_ADD", "CLOSURE_ADD", "CLOSURE_REMOVE");
        // la rimozione registra i valori della chiusura come "prima"
        assertThat(calendarAudit().get(2).getChanges().get("description"))
                .asInstanceOf(InstanceOfAssertFactories.MAP)
                .containsEntry("before", "Ferie estive")
                .containsEntry("after", null);
    }

    @Test
    void chiusuraConFinePrimaDellInizio_400() throws Exception {
        json(post(API + "/closures"),
                "{\"startDate\": \"2026-08-21\", \"endDate\": \"2026-08-10\", \"description\": \"Ferie\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("endDate"));
        json(post(API + "/closures"), "{\"startDate\": \"2026-08-21\", \"description\": \"  \"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("description"));
    }

    @Test
    void soloAdmin() throws Exception {
        mockMvc.perform(get(API).header(USER, "operatore.cc")).andExpect(status().isForbidden());
        mockMvc.perform(get(API).header(USER, "tutor1")).andExpect(status().isForbidden());
        mockMvc.perform(post(API + "/closures").header(USER, "operatore.cc").contentType(MediaType.APPLICATION_JSON)
                .content("{\"startDate\": \"2026-12-24\", \"description\": \"Vigilia\"}")).andExpect(status().isForbidden());
    }
}
