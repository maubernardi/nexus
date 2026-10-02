package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
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
import it.nexus.domain.Company;
import it.nexus.domain.JobCategory;
import it.nexus.domain.JobSlot;
import it.nexus.domain.Project;
import it.nexus.domain.TestEntities;
import it.nexus.domain.Ticket;
import it.nexus.domain.TicketCompanyBlacklist;
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
import it.nexus.repository.TicketCompanyBlacklistRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.TicketStatusHistoryRepository;
import it.nexus.repository.ZoneRepository;
import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class TicketMatchingIT {

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
    @Autowired TicketRepository tickets;
    @Autowired TicketCompanyBlacklistRepository blacklist;
    @Autowired TicketStatusHistoryRepository history;
    @Autowired AuditEventRepository audit;
    @Autowired CompanyAuditEventRepository companyAudit;

    private AppUser tutor;
    private AppUser operator;
    private AppUser colleague;
    private Project project;
    private Zone near;
    private Zone far;
    private JobCategory wanted;
    private JobCategory other;
    private Ticket ticket;
    private JobSlot match;
    private JobSlot blocked;

    @BeforeEach
    void setUp() {
        tutor = users.save(TestEntities.user("tutor1", Role.TUTOR, "00000000-0000-4000-8000-000000000001"));
        operator = users.save(TestEntities.user("operatore.cc", Role.CALL_CENTER, "00000000-0000-4000-8000-000000000003"));
        users.save(TestEntities.user("admin", Role.ADMIN, "00000000-0000-4000-8000-000000000004"));
        colleague = users.save(TestEntities.user("collega.cc", Role.CALL_CENTER));
        project = projects.save(TestEntities.project("PMATCH"));
        near = zones.save(TestEntities.zone("ZVICINA"));
        far = zones.save(TestEntities.zone("ZLONTANA"));
        wanted = categories.save(TestEntities.jobCategory("CRICHIESTA"));
        other = categories.save(TestEntities.jobCategory("CALTRA"));

        ticket = workingTicket(operator);

        Company alfa = companies.save(named("Alfa Logistica", "99900000101"));
        Company beta = companies.save(named("Beta Servizi", "99900000102"));
        Company chiusa = named("Chiusa S.r.l.", "99900000103");
        chiusa.setActive(false);
        chiusa = companies.save(chiusa);
        Company esclusa = companies.save(named("Esclusa S.p.A.", "99900000104"));

        match = slot(alfa, wanted, near, "Magazziniere");
        slot(beta, wanted, far, "Magazziniere notturno");
        slot(beta, other, near, "Cuoco");
        blocked = slot(alfa, wanted, near, "Carrellista");
        blocked.blockFor(workingTicket(operator));
        jobSlots.save(blocked);
        JobSlot withdrawn = slot(alfa, wanted, near, "Ritirata");
        withdrawn.setActive(false);
        jobSlots.save(withdrawn);
        slot(chiusa, wanted, near, "In azienda chiusa");
        slot(esclusa, wanted, near, "In azienda esclusa");
        em.flush();
        blacklist.save(new TicketCompanyBlacklist(ticket, esclusa, "Esito negativo precedente"));
        em.flush();
    }

    private static Company named(String name, String vat) {
        Company c = TestEntities.company(vat);
        c.setName(name);
        return c;
    }

    private JobSlot slot(Company company, JobCategory category, Zone zone, String title) {
        JobSlot s = TestEntities.jobSlot(company, category, zone);
        s.setTitle(title);
        return jobSlots.save(s);
    }

    private Ticket workingTicket(AppUser assignee) {
        Ticket t = TestEntities.ticket(tutor, project, beneficiaries.save(TestEntities.beneficiary(tutor, near)), wanted);
        t.setStatus(TicketStatus.IN_LAVORAZIONE);
        t.setAssignedCcOperator(assignee);
        return tickets.saveAndFlush(t);
    }

    private String id(Object entity) {
        return TsidMapper.toExternal(entity instanceof Ticket t ? t.getId() : ((JobSlot) entity).getId());
    }

    private ResultActions doMatch(String user, Ticket t, JobSlot s, long slotVersion) throws Exception {
        return mockMvc.perform(post("/api/v1/tickets/" + id(t) + "/match").header(USER, user)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"jobSlotId\": \"%s\", \"ticketVersion\": %d, \"jobSlotVersion\": %d}"
                        .formatted(id(s), t.getVersion(), slotVersion)));
    }

    @Test
    void compatibili_perZonaETipologia_soloLibereAttiveNonEscluse() throws Exception {
        String url = "/api/v1/tickets/" + id(ticket) + "/compatible-job-slots";
        mockMvc.perform(get(url).param("zoneId", TsidMapper.toExternal(near.getId()))
                        .param("jobCategoryId", TsidMapper.toExternal(wanted.getId())).header(USER, "operatore.cc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", contains("Magazziniere")))
                .andExpect(jsonPath("$[0].companyName").value("Alfa Logistica"));
        // allargando a tutte le zone e tipologie: per azienda, poi per titolo
        mockMvc.perform(get(url).header(USER, "operatore.cc"))
                .andExpect(jsonPath("$[*].title", contains("Magazziniere", "Cuoco", "Magazziniere notturno")));
    }

    @Test
    void dettaglioPerLaLavorazione() throws Exception {
        mockMvc.perform(get("/api/v1/tickets/" + id(ticket) + "/work").header(USER, "operatore.cc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_LAVORAZIONE"))
                .andExpect(jsonPath("$.assignedToMe").value(true))
                .andExpect(jsonPath("$.beneficiary.residenceZoneName").value("Zona ZVICINA"))
                .andExpect(jsonPath("$.beneficiaryZone.code").value("ZVICINA"))
                .andExpect(jsonPath("$.requestedJobCategory.code").value("CRICHIESTA"))
                .andExpect(jsonPath("$.proposal").doesNotExist());
    }

    @Test
    void abbinamento_propostaAllAziendaMansioneBloccataTuttoTracciato() throws Exception {
        doMatch("operatore.cc", ticket, match, match.getVersion())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROPOSTA_AZIENDA"))
                .andExpect(jsonPath("$.proposal.id").value(id(match)))
                .andExpect(jsonPath("$.proposal.companyName").value("Alfa Logistica"));
        em.flush();
        em.clear();

        JobSlot reloaded = jobSlots.findById(match.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(JobSlotStatus.BLOCCATA);
        assertThat(reloaded.getBlockedByTicket().getId()).isEqualTo(ticket.getId());
        assertThat(history.findByTicketIdOrderByCreatedAtAsc(ticket.getId())).extracting(r -> r.getToStatus())
                .containsExactly(TicketStatus.PROPOSTA_AZIENDA);
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.TICKET, ticket.getId()))
                .singleElement()
                .satisfies(e -> {
                    assertThat(e.getAction()).isEqualTo("MATCH");
                    assertThat(e.getChanges())
                            .containsEntry("status", Map.of("before", "IN_LAVORAZIONE", "after", "PROPOSTA_AZIENDA"))
                            .containsKeys("jobSlotId", "companyId");
                });
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.JOB_SLOT, match.getId()))
                .extracting(e -> e.getAction()).containsExactly("BLOCK");
        assertThat(companyAudit.findByCompanyIdOrderByCreatedAtAsc(match.getCompany().getId()))
                .extracting(e -> e.getEventType()).containsExactly("PROPOSTA_INVIATA");
    }

    @Test
    void mansioneAppenaBloccataOVersioneSuperata_409() throws Exception {
        doMatch("operatore.cc", ticket, blocked, blocked.getVersion())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("non è più disponibile")));
        doMatch("operatore.cc", ticket, match, match.getVersion() + 1).andExpect(status().isConflict());
        em.flush();
        em.clear();
        assertThat(tickets.findById(ticket.getId()).orElseThrow().getStatus()).isEqualTo(TicketStatus.IN_LAVORAZIONE);
    }

    @Test
    void segnalazioneDiUnCollegaONonPresaInCarico_409_adminPuo() throws Exception {
        Ticket ofColleague = workingTicket(colleague);
        doMatch("operatore.cc", ofColleague, match, match.getVersion())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("in carico a un collega")));
        doMatch("admin", ofColleague, match, match.getVersion()).andExpect(status().isOk());
    }

    @Test
    void tutor_403() throws Exception {
        doMatch("tutor1", ticket, match, match.getVersion()).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/tickets/" + id(ticket) + "/compatible-job-slots").header(USER, "tutor1"))
                .andExpect(status().isForbidden());
    }
}
