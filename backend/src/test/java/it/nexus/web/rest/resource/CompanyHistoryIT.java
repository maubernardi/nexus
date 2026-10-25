package it.nexus.web.rest.resource;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

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
import it.nexus.domain.Company;
import it.nexus.domain.CompanyAuditEvent;
import it.nexus.domain.JobCategory;
import it.nexus.domain.Project;
import it.nexus.domain.TestEntities;
import it.nexus.domain.Ticket;
import it.nexus.domain.TicketCompanyBlacklist;
import it.nexus.domain.Zone;
import it.nexus.domain.enumeration.Role;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.BeneficiaryRepository;
import it.nexus.repository.CompanyAuditEventRepository;
import it.nexus.repository.CompanyRepository;
import it.nexus.repository.JobCategoryRepository;
import it.nexus.repository.ProjectRepository;
import it.nexus.repository.TicketCompanyBlacklistRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.ZoneRepository;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class CompanyHistoryIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;

    @Autowired MockMvc mockMvc;
    @Autowired AppUserRepository users;
    @Autowired CompanyRepository companies;
    @Autowired ZoneRepository zones;
    @Autowired ProjectRepository projects;
    @Autowired JobCategoryRepository categories;
    @Autowired BeneficiaryRepository beneficiaries;
    @Autowired TicketRepository tickets;
    @Autowired CompanyAuditEventRepository events;
    @Autowired TicketCompanyBlacklistRepository blacklist;

    private AppUser tutor;
    private Company company;
    private Project project;
    private Zone zone;
    private JobCategory category;

    @BeforeEach
    void setUp() {
        tutor = users.save(TestEntities.user("tutor1", Role.TUTOR, "00000000-0000-4000-8000-000000000001"));
        users.save(TestEntities.user("operatore.cc", Role.CALL_CENTER, "00000000-0000-4000-8000-000000000003"));
        company = companies.save(TestEntities.company("98700000002"));
        project = projects.save(TestEntities.project("QSTORICO"));
        zone = zones.save(TestEntities.zone("QZSTORICO"));
        category = categories.save(TestEntities.jobCategory("QCSTORICO"));
    }

    private Ticket ticket() {
        return tickets.save(TestEntities.ticket(tutor, project, beneficiaries.save(TestEntities.beneficiary(tutor, zone)),
                category));
    }

    private String history() {
        return "/api/v1/companies/" + TsidMapper.toExternal(company.getId()) + "/history";
    }

    @Test
    void senzaEventi_riepilogoCompletoAZero() throws Exception {
        mockMvc.perform(get(history()).header(USER, "operatore.cc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.proposals").value(0))
                .andExpect(jsonPath("$.summary.accepted").value(0))
                .andExpect(jsonPath("$.summary.failedAppointments").value(0))
                .andExpect(jsonPath("$.summary.internships").value(0))
                .andExpect(jsonPath("$.summary.hirings").value(0))
                .andExpect(jsonPath("$.summary.negativeOutcomes").value(0))
                .andExpect(jsonPath("$.events", hasSize(0)));
    }

    @Test
    void riepilogoEdElencoDallaPiuRecente_conEsclusioni() throws Exception {
        Ticket first = ticket();
        Ticket second = ticket();
        Ticket third = ticket();
        events.save(new CompanyAuditEvent(company, first, "PROPOSTA_INVIATA", Map.of("jobSlotTitle", "Magazziniere")));
        events.save(new CompanyAuditEvent(company, first, "PROPOSTA_ACCOLTA", Map.of()));
        events.save(new CompanyAuditEvent(company, first, "TIROCINIO_AVVIATO", Map.of()));
        events.save(new CompanyAuditEvent(company, first, "TIROCINIO_CONCLUSO", Map.of("assunto", true)));
        events.save(new CompanyAuditEvent(company, second, "PROPOSTA_INVIATA", Map.of("jobSlotTitle", "Cameriere")));
        events.save(new CompanyAuditEvent(company, second, "APPUNTAMENTO_FALLITO", Map.of("rilancioInBacheca", true)));
        events.save(new CompanyAuditEvent(company, third, "TIROCINIO_CONCLUSO", Map.of("assunto", false)));
        blacklist.saveAndFlush(new TicketCompanyBlacklist(second, company, "Orari non compatibili"));

        mockMvc.perform(get(history()).header(USER, "operatore.cc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.proposals").value(2))
                .andExpect(jsonPath("$.summary.accepted").value(1))
                .andExpect(jsonPath("$.summary.failedAppointments").value(1))
                .andExpect(jsonPath("$.summary.internships").value(1))
                .andExpect(jsonPath("$.summary.hirings").value(1))
                .andExpect(jsonPath("$.summary.negativeOutcomes").value(1))
                .andExpect(jsonPath("$.events", hasSize(8)))
                // la più recente in cima: l'esclusione, con il motivo e la segnalazione collegata
                .andExpect(jsonPath("$.events[0].type").value("ESCLUSIONE"))
                .andExpect(jsonPath("$.events[0].detail").value("Orari non compatibili"))
                .andExpect(jsonPath("$.events[0].ticketNumber").value(second.getNumber()))
                .andExpect(jsonPath("$.events[0].ticketId").value(TsidMapper.toExternal(second.getId())))
                .andExpect(jsonPath("$.events[0].authorName").isString())
                .andExpect(jsonPath("$.events[1].detail").value("Senza assunzione"))
                .andExpect(jsonPath("$.events[2].detail").value("Con rilancio in bacheca"))
                .andExpect(jsonPath("$.events[3].detail").value("«Cameriere»"))
                .andExpect(jsonPath("$.events[7].type").value("PROPOSTA_INVIATA"))
                .andExpect(jsonPath("$.events[7].detail").value("«Magazziniere»"));
    }

    @Test
    void soloCallCenterEAdmin() throws Exception {
        mockMvc.perform(get(history()).header(USER, "tutor1")).andExpect(status().isForbidden());
    }

    @Test
    void aziendaInesistente_404() throws Exception {
        mockMvc.perform(get("/api/v1/companies/non-un-id/history").header(USER, "operatore.cc"))
                .andExpect(status().isNotFound());
    }
}
