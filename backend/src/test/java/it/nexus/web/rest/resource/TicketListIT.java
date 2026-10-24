package it.nexus.web.rest.resource;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
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
import it.nexus.domain.UserProject;
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
import it.nexus.repository.UserProjectRepository;
import it.nexus.repository.ZoneRepository;
import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
/** Elenco delle segnalazioni della Home (US-303): viste per ruolo, ordinamenti, filtri, visibilità. */
class TicketListIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;
    private static final String LIST = "/api/v1/tickets";

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired AppUserRepository users;
    @Autowired ProjectRepository projects;
    @Autowired ZoneRepository zones;
    @Autowired UserProjectRepository userProjects;
    @Autowired JobCategoryRepository categories;
    @Autowired BeneficiaryRepository beneficiaries;
    @Autowired CompanyRepository companies;
    @Autowired JobSlotRepository jobSlots;
    @Autowired BoardPostRepository boardPosts;
    @Autowired TicketRepository tickets;

    private String golId;
    private long normaleNord;
    private long normaleSud;
    private long fastTrack;
    private long assignedNum;
    private long advancedNum;
    private long colleagueNum;

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
        assignedNum = arrivedHoursAgo(tickets.save(assigned), 5);
        Ticket advanced = TestEntities.ticket(tutor, gol, inNorth3, category);
        advanced.setStatus(TicketStatus.PROPOSTA_AZIENDA);
        advancedNum = arrivedHoursAgo(tickets.save(advanced), 6);

        // un collega dello stesso progetto GOL: tutor1 vede le sue segnalazioni nella vista "Del mio progetto"
        AppUser colleague = users.save(TestEntities.user("tutor2", Role.TUTOR, "00000000-0000-4000-8000-000000000002"));
        userProjects.save(new UserProject(tutor, gol));
        userProjects.save(new UserProject(colleague, gol));
        colleagueNum = arrivedHoursAgo(tickets.save(TestEntities.ticket(colleague, gol,
                beneficiaries.save(TestEntities.beneficiary(colleague, north)), category)), 4);

        golId = TsidMapper.toExternal(gol.getId());
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
    void callCenter_predefinitaDaPrendereInCarico_fastTrackPrimaPoiPerArrivo() throws Exception {
        mockMvc.perform(get(LIST).header(USER, "operatore.cc"))
                .andExpect(status().isOk())
                // la segnalazione del collega (4 ore fa) è nuova e non presa in carico: è in coda, prima delle più recenti
                .andExpect(jsonPath("$[*].number", contains((int) fastTrack, (int) colleagueNum, (int) normaleNord,
                        (int) normaleSud)))
                .andExpect(jsonPath("$[0].fastTrack").value(true))
                .andExpect(jsonPath("$[0].type").value("SPECIAL"))
                .andExpect(jsonPath("$[2].beneficiary.residenceZoneName").value("Zona QNORD"))
                .andExpect(jsonPath("$[2].project.code").value("QGOL"))
                .andExpect(jsonPath("$[2].requestedJobCategory.code").value("QMAG"))
                .andExpect(jsonPath("$[2].tutorName").value("Nome Cognome"))
                .andExpect(jsonPath("$[2].assignedOperatorName").doesNotExist())
                .andExpect(jsonPath("$[2].version").isNumber());
    }

    @Test
    void daPrendereInCarico_filtroPerProgetto() throws Exception {
        mockMvc.perform(get(LIST).param("view", "TO_TAKE").param("projectId", golId).header(USER, "operatore.cc"))
                .andExpect(jsonPath("$[*].number", contains((int) fastTrack, (int) colleagueNum, (int) normaleNord)));
    }

    @Test
    void inCaricoAMe_conOperatore() throws Exception {
        mockMvc.perform(get(LIST).param("view", "ASSIGNED_TO_ME").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$[*].number", contains((int) assignedNum)))
                .andExpect(jsonPath("$[0].assignedOperatorName").value("Nome Cognome"));
        mockMvc.perform(get(LIST).param("view", "ASSIGNED_TO_ME").header(USER, "admin"))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void tutte_dallaPiuRecente_filtroPerStato() throws Exception {
        mockMvc.perform(get(LIST).param("view", "ALL").header(USER, "admin"))
                .andExpect(jsonPath("$[*].number", contains((int) fastTrack, (int) normaleSud, (int) normaleNord,
                        (int) colleagueNum, (int) assignedNum, (int) advancedNum)));
        mockMvc.perform(get(LIST).param("view", "ALL").param("status", "PROPOSTA_AZIENDA").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$[*].number", contains((int) advancedNum)));
    }

    @Test
    void tutor_predefinitaLeMie_senzaQuelleDelCollega() throws Exception {
        mockMvc.perform(get(LIST).header(USER, "tutor1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].number", contains((int) fastTrack, (int) normaleSud, (int) normaleNord,
                        (int) assignedNum, (int) advancedNum)));
    }

    @Test
    void tutor_delMioProgetto_ancheIlCollega_maiAltriProgetti() throws Exception {
        // tutor1 è assegnato solo a GOL: vede le segnalazioni GOL (anche del collega), non quella POLIS
        mockMvc.perform(get(LIST).param("view", "PROJECT").header(USER, "tutor1"))
                .andExpect(jsonPath("$[*].number", contains((int) fastTrack, (int) normaleNord, (int) colleagueNum,
                        (int) assignedNum, (int) advancedNum)))
                .andExpect(jsonPath("$[2].beneficiary.lastName").value("Rossi"));
    }

    @Test
    void vistaNonDelRuoloOFiltroNonValido_400() throws Exception {
        mockMvc.perform(get(LIST).param("view", "TO_TAKE").header(USER, "tutor1")).andExpect(status().isBadRequest());
        mockMvc.perform(get(LIST).param("view", "MINE").header(USER, "operatore.cc")).andExpect(status().isBadRequest());
        mockMvc.perform(get(LIST).param("projectId", "non-un-id").header(USER, "operatore.cc"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get(LIST).param("view", "SCONOSCIUTA").header(USER, "operatore.cc"))
                .andExpect(status().isBadRequest());
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
