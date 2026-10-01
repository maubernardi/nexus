package it.nexus.web.rest.resource;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import it.nexus.domain.BoardPost;
import it.nexus.domain.Beneficiary;
import it.nexus.domain.Company;
import it.nexus.domain.JobCategory;
import it.nexus.domain.JobSlot;
import it.nexus.domain.Project;
import it.nexus.domain.TestEntities;
import it.nexus.domain.Ticket;
import it.nexus.domain.Zone;
import it.nexus.domain.enumeration.Role;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.domain.enumeration.TicketType;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.BoardPostRepository;
import it.nexus.repository.BeneficiaryRepository;
import it.nexus.repository.CompanyRepository;
import it.nexus.repository.JobCategoryRepository;
import it.nexus.repository.JobSlotRepository;
import it.nexus.repository.ProjectRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.ZoneRepository;
import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class TicketQueueIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;
    private static final String QUEUE = "/api/v1/tickets/queue";

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

    private String golId;
    private String southId;
    private long normaleNord;
    private long normaleSud;
    private long fastTrack;

    @BeforeEach
    void setUp() {
        AppUser tutor = users.save(TestEntities.user("tutor1", Role.TUTOR, "00000000-0000-4000-8000-000000000001"));
        AppUser operator = users.save(TestEntities.user("operatore.cc", Role.CALL_CENTER, "00000000-0000-4000-8000-000000000003"));
        users.save(TestEntities.user("admin", Role.ADMIN, "00000000-0000-4000-8000-000000000004"));
        Project gol = projects.save(TestEntities.project("QGOL"));
        Project polis = projects.save(TestEntities.project("QPOLIS"));
        Project closed = TestEntities.project("QCHIUSO");
        closed.setActive(false);
        projects.save(closed);
        Zone north = zones.save(TestEntities.zone("QNORD"));
        Zone south = zones.save(TestEntities.zone("QSUD"));
        JobCategory category = categories.save(TestEntities.jobCategory("QMAG"));
        Beneficiary inNorth = beneficiaries.save(TestEntities.beneficiary(tutor, north));
        Beneficiary inSouth = beneficiaries.save(TestEntities.beneficiary(tutor, south));
        // un beneficiario per ticket: ognuno può averne al più uno aperto
        Beneficiary inSouth2 = beneficiaries.save(TestEntities.beneficiary(tutor, south));
        Beneficiary inNorth2 = beneficiaries.save(TestEntities.beneficiary(tutor, north));
        Beneficiary inNorth3 = beneficiaries.save(TestEntities.beneficiary(tutor, north));
        Company company = companies.save(TestEntities.company("99900000001"));
        JobSlot slot = jobSlots.save(TestEntities.jobSlot(company, category, north));
        BoardPost post = boardPosts.save(TestEntities.boardPost(slot));

        normaleNord = arrivedHoursAgo(tickets.save(TestEntities.ticket(tutor, gol, inNorth, category)), 3);
        normaleSud = arrivedHoursAgo(tickets.save(TestEntities.ticket(tutor, polis, inSouth2, category)), 2);
        Ticket special = TestEntities.ticket(tutor, gol, inSouth, category);
        special.setType(TicketType.SPECIAL);
        special.setFastTrack(true);
        special.setStatus(TicketStatus.IN_LAVORAZIONE);
        special.setBoardPost(post);
        fastTrack = arrivedHoursAgo(tickets.save(special), 1);

        // fuori coda: già assegnato a un operatore, oppure in una fase successiva
        Ticket assigned = TestEntities.ticket(tutor, gol, inNorth2, category);
        assigned.setAssignedCcOperator(operator);
        arrivedHoursAgo(tickets.save(assigned), 5);
        Ticket advanced = TestEntities.ticket(tutor, gol, inNorth3, category);
        advanced.setStatus(TicketStatus.PROPOSTA_AZIENDA);
        arrivedHoursAgo(tickets.save(advanced), 6);

        golId = TsidMapper.toExternal(gol.getId());
        southId = TsidMapper.toExternal(south.getId());
        em.flush();
        em.clear();
    }

    /** Fissa l'istante di arrivo, per un ordine deterministico; restituisce il numero del ticket. */
    private long arrivedHoursAgo(Ticket ticket, int hours) {
        em.flush();
        em.createNativeQuery("UPDATE ticket SET created_at = now() - make_interval(hours => :h) WHERE id = :id")
                .setParameter("h", hours)
                .setParameter("id", ticket.getId())
                .executeUpdate();
        em.refresh(ticket);
        return ticket.getNumber();
    }

    @Test
    void fastTrackPrimaPoiPerArrivo_esclusiAssegnatiEFasiSuccessive() throws Exception {
        mockMvc.perform(get(QUEUE).header(USER, "operatore.cc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].number", contains((int) fastTrack, (int) normaleNord, (int) normaleSud)))
                .andExpect(jsonPath("$[0].fastTrack").value(true))
                .andExpect(jsonPath("$[0].type").value("SPECIAL"))
                .andExpect(jsonPath("$[1].beneficiary.residenceZoneName").value("Zona QNORD"))
                .andExpect(jsonPath("$[1].project.code").value("QGOL"))
                .andExpect(jsonPath("$[1].requestedJobCategory.code").value("QMAG"))
                .andExpect(jsonPath("$[1].tutorName").value("Nome Cognome"))
                .andExpect(jsonPath("$[1].version").isNumber());
    }

    @Test
    void filtroPerProgetto() throws Exception {
        mockMvc.perform(get(QUEUE).param("projectId", golId).header(USER, "operatore.cc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].number", contains((int) fastTrack, (int) normaleNord)));
    }

    @Test
    void filtroPerZonaEProgettoInsieme() throws Exception {
        mockMvc.perform(get(QUEUE).param("zoneId", southId).header(USER, "operatore.cc"))
                .andExpect(jsonPath("$[*].number", contains((int) fastTrack, (int) normaleSud)));
        mockMvc.perform(get(QUEUE).param("zoneId", southId).param("projectId", golId).header(USER, "operatore.cc"))
                .andExpect(jsonPath("$[*].number", contains((int) fastTrack)));
    }

    @Test
    void filtroNonValido_400() throws Exception {
        mockMvc.perform(get(QUEUE).param("projectId", "non-un-id").header(USER, "operatore.cc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminVedeLaCoda_tutorNo() throws Exception {
        mockMvc.perform(get(QUEUE).header(USER, "admin")).andExpect(status().isOk());
        // un 403 di ruolo non è "utente non abilitato": niente codice dedicato
        mockMvc.perform(get(QUEUE).header(USER, "tutor1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void progettiAttiviPerIFiltri() throws Exception {
        mockMvc.perform(get("/api/v1/reference/projects").header(USER, "operatore.cc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'QCHIUSO')]").isEmpty())
                .andExpect(jsonPath("$[?(@.code == 'QGOL')]").isNotEmpty());
        mockMvc.perform(get("/api/v1/reference/projects").header(USER, "tutor1")).andExpect(status().isForbidden());
    }
}
