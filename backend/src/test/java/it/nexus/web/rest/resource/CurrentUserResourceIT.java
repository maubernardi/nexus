package it.nexus.web.rest.resource;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.security.MockHeaderAuthenticationFilter;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, CurrentUserResourceIT.TestEndpoints.class})
class CurrentUserResourceIT {

    private static final String USER_HEADER = MockHeaderAuthenticationFilter.HEADER;

    @Autowired
    MockMvc mockMvc;

    @Test
    void me_returnsCurrentUser_whenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/me").header(USER_HEADER, "operatore.cc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("operatore.cc"))
                .andExpect(jsonPath("$.firstName").value("Operatore"))
                .andExpect(jsonPath("$.roles").value(containsInAnyOrder("CALL_CENTER")));
    }

    @Test
    void me_returns401WithUniformBody_whenAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.path").value("/api/v1/me"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void me_returns401_whenUserUnknown() throws Exception {
        mockMvc.perform(get("/api/v1/me").header(USER_HEADER, "sconosciuto"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpoint_returns403_forTutor() throws Exception {
        mockMvc.perform(get("/api/v1/test/admin-only").header(USER_HEADER, "tutor1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.path").value("/api/v1/test/admin-only"));
    }

    @Test
    void adminEndpoint_returns200_forAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/test/admin-only").header(USER_HEADER, "admin"))
                .andExpect(status().isOk());
    }

    @Test
    void unknownResource_returns404WithUniformBody() throws Exception {
        mockMvc.perform(get("/api/v1/non-esiste").header(USER_HEADER, "tutor1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void unexpectedError_returns500WithoutInternals() throws Exception {
        mockMvc.perform(get("/api/v1/test/boom").header(USER_HEADER, "admin"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Si è verificato un errore imprevisto"))
                .andExpect(content().string(not(org.hamcrest.Matchers.containsString("dettaglio-interno"))));
    }

    @Test
    void health_isPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void openApi_isPublicAndListsMeEndpoint() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/me']").exists());
    }

    /** Endpoint presenti solo nei test per verificare 403 e 500. */
    @TestConfiguration
    static class TestEndpoints {

        @RestController
        static class TestController {

            @GetMapping("/api/v1/test/admin-only")
            @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
            String adminOnly() {
                return "ok";
            }

            @GetMapping("/api/v1/test/boom")
            @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
            String boom() {
                throw new IllegalStateException("dettaglio-interno");
            }
        }
    }
}
