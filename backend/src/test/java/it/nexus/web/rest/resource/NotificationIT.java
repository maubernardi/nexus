package it.nexus.web.rest.resource;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.security.MockHeaderAuthenticationFilter;
import it.nexus.domain.AppUser;
import it.nexus.domain.Notification;
import it.nexus.domain.TestEntities;
import it.nexus.domain.enumeration.NotificationType;
import it.nexus.domain.enumeration.Role;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.NotificationRepository;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class NotificationIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;

    @Autowired MockMvc mockMvc;
    @Autowired AppUserRepository users;
    @Autowired NotificationRepository notifications;

    private String first;
    private String others;

    @BeforeEach
    void setUp() {
        AppUser tutor = users.save(TestEntities.user("tutor1", Role.TUTOR, "00000000-0000-4000-8000-000000000001"));
        AppUser operator = users.save(TestEntities.user("operatore.cc", Role.CALL_CENTER,
                "00000000-0000-4000-8000-000000000003"));
        first = TsidMapper.toExternal(notifications.save(new Notification(tutor, NotificationType.PROPOSAL_SENT, null))
                .getId());
        notifications.save(new Notification(tutor, NotificationType.CATEGORY_CHANGED, null));
        others = TsidMapper.toExternal(notifications.saveAndFlush(
                new Notification(operator, NotificationType.PROPOSAL_ACCEPTED, null)).getId());
    }

    @Test
    void soloLeProprie_dallaPiuRecente_conContatore() throws Exception {
        mockMvc.perform(get("/api/v1/notifications").header(USER, "tutor1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unread").value(2))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].type").value("CATEGORY_CHANGED"))
                .andExpect(jsonPath("$.items[1].read").value(false));
        mockMvc.perform(get("/api/v1/notifications/unread-count").header(USER, "tutor1"))
                .andExpect(content().string("2"));
    }

    @Test
    void segnaComeLetta_unaETutte() throws Exception {
        mockMvc.perform(post("/api/v1/notifications/" + first + "/read").header(USER, "tutor1"))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/notifications").header(USER, "tutor1"))
                .andExpect(jsonPath("$.unread").value(1))
                .andExpect(jsonPath("$.items[1].read").value(true));

        mockMvc.perform(post("/api/v1/notifications/read-all").header(USER, "tutor1"))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/notifications/unread-count").header(USER, "tutor1"))
                .andExpect(content().string("0"));
        // quelle degli altri restano come sono
        mockMvc.perform(get("/api/v1/notifications/unread-count").header(USER, "operatore.cc"))
                .andExpect(content().string("1"));
    }

    @Test
    void notificaAltrui_404() throws Exception {
        mockMvc.perform(post("/api/v1/notifications/" + others + "/read").header(USER, "tutor1"))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/notifications/non-un-id/read").header(USER, "tutor1"))
                .andExpect(status().isNotFound());
    }
}
