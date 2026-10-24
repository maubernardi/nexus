package it.nexus.web.rest.resource;

import static org.hamcrest.Matchers.contains;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.security.MockHeaderAuthenticationFilter;
import it.nexus.domain.AppUser;
import it.nexus.domain.Company;
import it.nexus.domain.JobCategory;
import it.nexus.domain.JobSlot;
import it.nexus.domain.Project;
import it.nexus.domain.TestEntities;
import it.nexus.domain.Ticket;
import it.nexus.domain.TicketStatusHistory;
import it.nexus.domain.UserProject;
import it.nexus.domain.Zone;
import it.nexus.domain.enumeration.Role;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.BeneficiaryRepository;
import it.nexus.repository.CompanyRepository;
import it.nexus.repository.JobCategoryRepository;
import it.nexus.repository.JobSlotRepository;
import it.nexus.repository.ProjectRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.TicketStatusHistoryRepository;
import it.nexus.repository.UserProjectRepository;
import it.nexus.repository.ZoneRepository;
import jakarta.persistence.EntityManager;

/** Dettaglio e cronologia della segnalazione (US-305): visibilità per ruolo ed eventi leggibili. */
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class TicketDetailIT {

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
    @Autowired TicketStatusHistoryRepository history;

    private AppUser tutor1;
    private AppUser tutor2;
    private Project gol;
    private Project polis;
    private Zone zone;
    private JobCategory category;

    @BeforeEach
    void setUp() {
        tutor1 = TestEntities.user("tutor1", Role.TUTOR, "00000000-0000-4000-8000-000000000001");
        tutor1.setFirstName("Anna");
        tutor1.setLastName("Tutor");
        tutor1 = users.save(tutor1);
        tutor2 = users.save(TestEntities.user("tutor2", Role.TUTOR, "00000000-0000-4000-8000-000000000002"));
        AppUser operator = TestEntities.user("operatore.cc", Role.CALL_CENTER, "00000000-0000-4000-8000-000000000003");
        operator.setFirstName("Olga");
        operator.setLastName("Operatrice");
        users.save(operator);
        gol = projects.save(TestEntities.project("DGOL"));
        polis = projects.save(TestEntities.project("DPOLIS"));
        userProjects.save(new UserProject(tutor1, gol));
        userProjects.save(new UserProject(tutor2, gol));
        userProjects.save(new UserProject(tutor2, polis));
        zone = zones.save(TestEntities.zone("DZONA"));
        category = categories.save(TestEntities.jobCategory("DMAG"));
        em.flush();
    }

    private Ticket ticketOf(AppUser tutor, Project project) {
        return tickets.saveAndFlush(TestEntities.ticket(tutor, project, beneficiaries.save(TestEntities.beneficiary(tutor, zone)),
                category));
    }

    @Test
    void visibilita_proprio_delProgetto_maiAltriProgetti_callCenterTutte() throws Exception {
        String own = TsidMapper.toExternal(ticketOf(tutor1, gol).getId());
        String colleagueSameProject = TsidMapper.toExternal(ticketOf(tutor2, gol).getId());
        String otherProject = TsidMapper.toExternal(ticketOf(tutor2, polis).getId());

        mockMvc.perform(get("/api/v1/tickets/" + own).header(USER, "tutor1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/tickets/" + colleagueSameProject).header(USER, "tutor1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.beneficiary.lastName").value("Rossi"));
        mockMvc.perform(get("/api/v1/tickets/" + otherProject).header(USER, "tutor1")).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/tickets/" + otherProject + "/history").header(USER, "tutor1"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/tickets/" + otherProject).header(USER, "operatore.cc")).andExpect(status().isOk());
    }

    @Test
    void cronologia_dallInvioAllaProposta_conAutoriEDettagli() throws Exception {
        String beneficiaryId = TsidMapper.toExternal(beneficiaries.save(TestEntities.beneficiary(tutor1, zone)).getId());
        String id = JsonPath.read(mockMvc.perform(post("/api/v1/tickets").header(USER, "tutor1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"beneficiaryId\": \"%s\", \"projectId\": \"%s\", \"jobCategoryId\": \"%s\"}".formatted(
                                beneficiaryId, TsidMapper.toExternal(gol.getId()), TsidMapper.toExternal(category.getId()))))
                .andReturn().getResponse().getContentAsString(), "$.id");
        mockMvc.perform(post("/api/v1/tickets/" + id + "/take-charge").header(USER, "operatore.cc")
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\": 0}")).andExpect(status().isOk());
        Company company = TestEntities.company("99900000555");
        company.setName("Alfa Logistica");
        company = companies.save(company);
        JobSlot slot = TestEntities.jobSlot(company, category, zone);
        slot.setTitle("Magazziniere");
        slot = jobSlots.saveAndFlush(slot);
        mockMvc.perform(post("/api/v1/tickets/" + id + "/match").header(USER, "operatore.cc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"jobSlotId\": \"%s\", \"ticketVersion\": 1, \"jobSlotVersion\": %d}".formatted(
                        TsidMapper.toExternal(slot.getId()), slot.getVersion()))).andExpect(status().isOk());
        em.flush();

        mockMvc.perform(get("/api/v1/tickets/" + id + "/history").header(USER, "tutor1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].action", contains("SUBMIT", "TAKE_CHARGE", "MATCH")))
                .andExpect(jsonPath("$[0].authorName").value("Anna Tutor"))
                .andExpect(jsonPath("$[0].fromStatus").doesNotExist())
                .andExpect(jsonPath("$[0].toStatus").value("NUOVA"))
                .andExpect(jsonPath("$[1].authorName").value("Olga Operatrice"))
                .andExpect(jsonPath("$[1].detail").value("Olga Operatrice"))
                .andExpect(jsonPath("$[1].toStatus").value("IN_LAVORAZIONE"))
                .andExpect(jsonPath("$[2].fromStatus").value("IN_LAVORAZIONE"))
                .andExpect(jsonPath("$[2].toStatus").value("PROPOSTA_AZIENDA"))
                .andExpect(jsonPath("$[2].detail").value("«Magazziniere» presso Alfa Logistica"));
    }

    @Test
    void segnalazioneSenzaAudit_cronologiaDalloStoricoDegliStati() throws Exception {
        Ticket legacy = ticketOf(tutor1, gol);
        history.saveAndFlush(new TicketStatusHistory(legacy, null, TicketStatus.NUOVA, "Creata dai dati demo"));

        mockMvc.perform(get("/api/v1/tickets/" + TsidMapper.toExternal(legacy.getId()) + "/history").header(USER, "tutor1"))
                .andExpect(jsonPath("$[*].action", contains("STATUS")))
                .andExpect(jsonPath("$[0].toStatus").value("NUOVA"))
                .andExpect(jsonPath("$[0].note").value("Creata dai dati demo"))
                .andExpect(jsonPath("$[0].authorName").value("Sistema"));
    }

    @Test
    void segnalazioneNataPrimaDellAudit_invioDalloStorico_poiGliEventiDiAudit() throws Exception {
        Ticket legacy = ticketOf(tutor1, gol);
        history.saveAndFlush(new TicketStatusHistory(legacy, null, TicketStatus.NUOVA, null));
        // l'audit inizia dopo (un'ora dopo l'invio)
        em.createNativeQuery("UPDATE ticket_status_history SET created_at = now() - interval '1 hour' WHERE ticket_id = :id")
                .setParameter("id", legacy.getId()).executeUpdate();
        em.clear();
        mockMvc.perform(post("/api/v1/tickets/" + TsidMapper.toExternal(legacy.getId()) + "/take-charge")
                .header(USER, "operatore.cc").contentType(MediaType.APPLICATION_JSON).content("{\"version\": 0}"))
                .andExpect(status().isOk());
        em.flush();

        mockMvc.perform(get("/api/v1/tickets/" + TsidMapper.toExternal(legacy.getId()) + "/history").header(USER, "tutor1"))
                .andExpect(jsonPath("$[*].action", contains("STATUS", "TAKE_CHARGE")))
                .andExpect(jsonPath("$[0].toStatus").value("NUOVA"));
    }
}
