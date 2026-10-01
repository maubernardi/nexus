package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.security.MockHeaderAuthenticationFilter;
import it.nexus.domain.AppUser;
import it.nexus.domain.BoardPost;
import it.nexus.domain.Company;
import it.nexus.domain.JobCategory;
import it.nexus.domain.JobSlot;
import it.nexus.domain.Project;
import it.nexus.domain.TestEntities;
import it.nexus.domain.Ticket;
import it.nexus.domain.Zone;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.Role;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.domain.enumeration.TicketType;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.AuditEventRepository;
import it.nexus.repository.BeneficiaryRepository;
import it.nexus.repository.BoardPostRepository;
import it.nexus.repository.CompanyRepository;
import it.nexus.repository.JobCategoryRepository;
import it.nexus.repository.JobSlotRepository;
import it.nexus.repository.ProjectRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.TicketStatusHistoryRepository;
import it.nexus.repository.ZoneRepository;
import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class TicketTakeChargeIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired AppUserRepository users;
    @Autowired ProjectRepository projects;
    @Autowired ZoneRepository zones;
    @Autowired JobCategoryRepository categories;
    @Autowired BeneficiaryRepository beneficiaries;
    @Autowired CompanyRepository companies;
    @Autowired JobSlotRepository jobSlots;
    @Autowired BoardPostRepository boardPosts;
    @Autowired TicketRepository tickets;
    @Autowired TicketStatusHistoryRepository history;
    @Autowired AuditEventRepository audit;

    private AppUser operator;
    private Ticket nuova;
    private Ticket special;

    @BeforeEach
    void setUp() {
        AppUser tutor = users.save(TestEntities.user("tutor1", Role.TUTOR, "00000000-0000-4000-8000-000000000001"));
        operator = users.save(TestEntities.user("operatore.cc", Role.CALL_CENTER, "00000000-0000-4000-8000-000000000003"));
        users.save(TestEntities.user("admin", Role.ADMIN, "00000000-0000-4000-8000-000000000004"));
        Project project = projects.save(TestEntities.project("PCARICO"));
        Zone zone = zones.save(TestEntities.zone("ZCARICO"));
        JobCategory category = categories.save(TestEntities.jobCategory("CCARICO"));
        nuova = tickets.save(TestEntities.ticket(tutor, project, beneficiaries.save(TestEntities.beneficiary(tutor, zone)), category));

        Company company = companies.save(TestEntities.company("99900000077"));
        JobSlot slot = jobSlots.save(TestEntities.jobSlot(company, category, zone));
        BoardPost post = boardPosts.save(TestEntities.boardPost(slot));
        special = TestEntities.ticket(tutor, project, beneficiaries.save(TestEntities.beneficiary(tutor, zone)), category);
        special.setType(TicketType.SPECIAL);
        special.setFastTrack(true);
        special.setStatus(TicketStatus.IN_LAVORAZIONE);
        special.setBoardPost(post);
        special = tickets.save(special);
        em.flush();
    }

    private ResultActions take(String user, Ticket ticket, long version) throws Exception {
        return mockMvc.perform(post("/api/v1/tickets/" + TsidMapper.toExternal(ticket.getId()) + "/take-charge")
                .header(USER, user).contentType(MediaType.APPLICATION_JSON).content("{\"version\": " + version + "}"));
    }

    @Test
    void presaInCarico_assegnataEInLavorazione_fuoriDallaCoda_tracciata() throws Exception {
        String id = TsidMapper.toExternal(nuova.getId());
        long vista = nuova.getVersion();
        take("operatore.cc", nuova, vista)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_LAVORAZIONE"))
                .andExpect(jsonPath("$.version").value(vista + 1));

        mockMvc.perform(get("/api/v1/tickets/queue").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$[*].id", not(hasItem(id))));
        mockMvc.perform(get("/api/v1/tickets/assigned-to-me").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$[*].id", hasItem(id)));
        mockMvc.perform(get("/api/v1/tickets/assigned-to-me").header(USER, "admin"))
                .andExpect(jsonPath("$[*].id", not(hasItem(id))));

        em.flush();
        em.clear();
        assertThat(tickets.findById(nuova.getId()).orElseThrow().getAssignedCcOperator().getId()).isEqualTo(operator.getId());
        assertThat(history.findByTicketIdOrderByCreatedAtAsc(nuova.getId())).extracting(r -> r.getToStatus())
                .containsExactly(TicketStatus.IN_LAVORAZIONE);
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.TICKET, nuova.getId()))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.getAction()).isEqualTo("TAKE_CHARGE");
                    assertThat(event.getCreatedBy()).isEqualTo("operatore.cc");
                    assertThat(event.getChanges())
                            .containsEntry("status", Map.of("before", "NUOVA", "after", "IN_LAVORAZIONE"))
                            .containsKey("assignedCcOperatorId");
                });
    }

    @Test
    void collegaUnIstantePrima_409() throws Exception {
        long vista = nuova.getVersion();
        take("operatore.cc", nuova, vista).andExpect(status().isOk());
        take("admin", nuova, vista).andExpect(status().isConflict());
    }

    @Test
    void giaAssegnata_anchéConVersioneAggiornata_409() throws Exception {
        take("operatore.cc", special, special.getVersion()).andExpect(status().isOk());
        em.flush();
        em.clear();
        long aggiornata = tickets.findById(special.getId()).orElseThrow().getVersion();

        take("admin", special, aggiornata)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("già stata presa in carico")));
    }

    @Test
    void specialeInLavorazione_presaInCaricoSenzaCambioDiStato() throws Exception {
        take("operatore.cc", special, special.getVersion())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_LAVORAZIONE"))
                .andExpect(jsonPath("$.fastTrack").value(true));
        em.flush();
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.TICKET, special.getId()))
                .singleElement()
                .satisfies(event -> assertThat(event.getChanges()).containsOnlyKeys("assignedCcOperatorId"));
    }

    @Test
    void tutor_403_inesistente_404_versioneMancante_400() throws Exception {
        take("tutor1", nuova, nuova.getVersion()).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/tickets/0000000000000/take-charge").header(USER, "operatore.cc")
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\": 0}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/tickets/" + TsidMapper.toExternal(nuova.getId()) + "/take-charge")
                .header(USER, "operatore.cc").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}
