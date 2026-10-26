package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
import it.nexus.config.TimeConfig;
import it.nexus.config.security.MockHeaderAuthenticationFilter;
import it.nexus.domain.AppUser;
import it.nexus.domain.Company;
import it.nexus.domain.JobCategory;
import it.nexus.domain.JobSlot;
import it.nexus.domain.Project;
import it.nexus.domain.TestEntities;
import it.nexus.domain.Ticket;
import it.nexus.domain.UserProject;
import it.nexus.domain.Zone;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.JobSlotStatus;
import it.nexus.domain.enumeration.Role;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.AuditEventRepository;
import it.nexus.repository.BeneficiaryRepository;
import it.nexus.repository.CompanyAuditEventRepository;
import it.nexus.repository.CompanyRepository;
import it.nexus.repository.JobCategoryRepository;
import it.nexus.repository.JobSlotRepository;
import it.nexus.repository.ProjectRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.UserProjectRepository;
import it.nexus.repository.ZoneRepository;
import it.nexus.services.WorkingDayService;
import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class TicketAcceptIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired AppUserRepository users;
    @Autowired ProjectRepository projects;
    @Autowired UserProjectRepository userProjects;
    @Autowired ZoneRepository zones;
    @Autowired JobCategoryRepository categories;
    @Autowired BeneficiaryRepository beneficiaries;
    @Autowired CompanyRepository companies;
    @Autowired JobSlotRepository jobSlots;
    @Autowired TicketRepository tickets;
    @Autowired AuditEventRepository audit;
    @Autowired CompanyAuditEventRepository companyAudit;
    @Autowired WorkingDayService workingDays;

    private AppUser tutor;
    private AppUser colleague;
    private Project project;
    private Ticket ticket;
    private JobSlot slot;
    private Company company;

    @BeforeEach
    void setUp() throws Exception {
        tutor = users.save(TestEntities.user("tutor1", Role.TUTOR, "00000000-0000-4000-8000-000000000001"));
        colleague = users.save(TestEntities.user("tutor2", Role.TUTOR, "00000000-0000-4000-8000-000000000002"));
        AppUser operator = users.save(TestEntities.user("operatore.cc", Role.CALL_CENTER,
                "00000000-0000-4000-8000-000000000003"));
        project = projects.save(TestEntities.project("PACCEPT"));
        userProjects.save(new UserProject(tutor, project));
        Zone zone = zones.save(TestEntities.zone("ZACCEPT"));
        JobCategory category = categories.save(TestEntities.jobCategory("CACCEPT"));
        company = TestEntities.company("99900000201");
        company.setName("Alfa Logistica");
        company = companies.save(company);
        JobSlot s = TestEntities.jobSlot(company, category, zone);
        s.setTitle("Magazziniere");
        slot = jobSlots.save(s);
        Ticket t = TestEntities.ticket(tutor, project, beneficiaries.save(TestEntities.beneficiary(tutor, zone)), category);
        t.setStatus(TicketStatus.IN_LAVORAZIONE);
        t.setAssignedCcOperator(operator);
        ticket = tickets.saveAndFlush(t);

        // proposta all'azienda con il flusso reale (US-602)
        mockMvc.perform(post("/api/v1/tickets/" + id() + "/match").header(USER, "operatore.cc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobSlotId\": \"%s\", \"ticketVersion\": %d, \"jobSlotVersion\": %d}"
                                .formatted(TsidMapper.toExternal(slot.getId()), ticket.getVersion(), slot.getVersion())))
                .andExpect(status().isOk());
        em.flush();
        em.clear();
    }

    private String id() {
        return TsidMapper.toExternal(ticket.getId());
    }

    private long version() {
        return tickets.findById(ticket.getId()).orElseThrow().getVersion();
    }

    private ResultActions accept(String user, long version) throws Exception {
        return mockMvc.perform(post("/api/v1/tickets/" + id() + "/accept").header(USER, user)
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\": %d}".formatted(version)));
    }

    @Test
    void accettazione_statoTimerStoricoECronologia() throws Exception {
        mockMvc.perform(get("/api/v1/tickets/" + id()).header(USER, "tutor1"))
                .andExpect(jsonPath("$.myTicket").value(true))
                .andExpect(jsonPath("$.timerDeadline").doesNotExist());
        LocalDate expected = workingDays.plusWorkingDays(LocalDate.now(TimeConfig.ZONE), 7);

        accept("tutor1", version())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROPOSTA_ACCOLTA"))
                .andExpect(jsonPath("$.timerDeadline").value(expected.toString()))
                .andExpect(jsonPath("$.proposal.title").value("Magazziniere"));

        em.flush();
        em.clear();
        Ticket saved = tickets.findById(ticket.getId()).orElseThrow();
        assertThat(saved.getTimerStartedAt()).isNotNull();
        // il timer scade alla fine del 7° giorno lavorativo, ora di Roma
        assertThat(saved.getTimerDeadlineAt()).isEqualTo(expected.plusDays(1).atStartOfDay(TimeConfig.ZONE).toInstant());
        assertThat(jobSlots.findById(slot.getId()).orElseThrow().getStatus()).isEqualTo(JobSlotStatus.BLOCCATA);
        assertThat(companyAudit.findByCompanyIdOrderByCreatedAtAsc(company.getId()))
                .extracting(e -> e.getEventType())
                .containsExactly("PROPOSTA_INVIATA", "PROPOSTA_ACCOLTA");
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.TICKET, ticket.getId()))
                .last()
                .satisfies(e -> {
                    assertThat(e.getAction()).isEqualTo("ACCEPT");
                    assertThat(e.getCreatedBy()).isEqualTo("tutor1");
                });

        String history = mockMvc.perform(get("/api/v1/tickets/" + id() + "/history").header(USER, "operatore.cc"))
                .andReturn().getResponse().getContentAsString();
        assertThat((String) JsonPath.read(history, "$[-1].detail"))
                .isEqualTo("Scadenza " + expected.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        // anche il Call Center vede la scadenza, ma la segnalazione non è sua
        mockMvc.perform(get("/api/v1/tickets/" + id()).header(USER, "operatore.cc"))
                .andExpect(jsonPath("$.myTicket").value(false))
                .andExpect(jsonPath("$.timerDeadline").value(expected.toString()));
    }

    @Test
    void soloIlTutorDellaSegnalazione() throws Exception {
        // tutor di un altro progetto: la segnalazione non esiste per lui
        accept("tutor2", version()).andExpect(status().isNotFound());
        // collega dello stesso progetto: la vede ma non può accettare
        userProjects.save(new UserProject(colleague, project));
        accept("tutor2", version()).andExpect(status().isForbidden());
        accept("operatore.cc", version()).andExpect(status().isForbidden());
    }

    @Test
    void secondaAccettazioneOVersioneSuperata_409() throws Exception {
        long v = version();
        accept("tutor1", v + 1).andExpect(status().isConflict());
        accept("tutor1", v).andExpect(status().isOk());
        accept("tutor1", version()).andExpect(status().isConflict());
    }
}
