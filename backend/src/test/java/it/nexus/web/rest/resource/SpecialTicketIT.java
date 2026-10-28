package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.security.MockHeaderAuthenticationFilter;
import it.nexus.domain.Beneficiary;
import it.nexus.domain.BoardPost;
import it.nexus.domain.Project;
import it.nexus.domain.Ticket;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.BoardPostStatus;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AuditEventRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.support.WorkflowScenarios;
import it.nexus.support.WorkflowScenarios.Actors;
import it.nexus.support.WorkflowScenarios.Place;
import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, WorkflowScenarios.class})
/** Segnalazione speciale dalla bacheca (US-306): candidatura, corsia veloce, una sola candidatura per annuncio. */
class SpecialTicketIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;
    private static final String SPECIAL = "/api/v1/tickets/special";

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired WorkflowScenarios scenarios;
    @Autowired TicketRepository tickets;
    @Autowired AuditEventRepository audit;

    private Actors actors;
    private Place place;
    private Project gol;
    private Project polis;
    private BoardPost forAll;
    private BoardPost forGol;
    private BoardPost forPolis;

    @BeforeEach
    void setUp() {
        actors = scenarios.actors();
        // tutor1 solo GOL, tutor2 GOL e POLIS (come nei dati demo)
        gol = scenarios.project("SGOL", actors.tutor1(), actors.tutor2());
        polis = scenarios.project("SPOLIS", actors.tutor2());
        place = scenarios.place();
        forAll = scenarios.publishedPost(scenarios.freeSlot(place, "Magazziniere"), null);
        forGol = scenarios.publishedPost(scenarios.freeSlot(place, "Cameriere"), gol);
        forPolis = scenarios.publishedPost(scenarios.freeSlot(place, "Barista"), polis);
        em.flush();
    }

    private static String id(Object entity) {
        return entity instanceof Ticket t ? TsidMapper.toExternal(t.getId())
                : entity instanceof BoardPost p ? TsidMapper.toExternal(p.getId())
                : TsidMapper.toExternal(((Beneficiary) entity).getId());
    }

    private ResultActions candidate(String user, BoardPost post, Beneficiary beneficiary, Project project)
            throws Exception {
        String projectId = project == null ? "null" : "\"" + TsidMapper.toExternal(project.getId()) + "\"";
        return mockMvc.perform(post(SPECIAL).header(USER, user).contentType(MediaType.APPLICATION_JSON)
                .content("{\"boardPostId\": \"%s\", \"beneficiaryId\": \"%s\", \"projectId\": %s}"
                        .formatted(id(post), id(beneficiary), projectId)));
    }

    @Test
    void candidatura_segnalazioneSpecialeInCimaAllaCodaConCronologiaEAudit() throws Exception {
        Beneficiary mario = scenarios.beneficiary(actors.tutor2(), place);
        String body = candidate("tutor2", forAll, mario, polis)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("SPECIAL"))
                .andExpect(jsonPath("$.fastTrack").value(true))
                .andExpect(jsonPath("$.status").value("IN_LAVORAZIONE"))
                .andExpect(jsonPath("$.project.code").value(polis.getCode()))
                .andExpect(jsonPath("$.requestedJobCategory.code").value(place.category().getCode()))
                .andReturn().getResponse().getContentAsString();
        String ticketId = JsonPath.read(body, "$.id");
        Long ticketNumber = ((Number) JsonPath.read(body, "$.number")).longValue();

        // per il Call Center: in cima a "Da prendere in carico", con la corsia veloce e l'annuncio nel dettaglio
        mockMvc.perform(get("/api/v1/tickets").param("view", "TO_TAKE").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$[0].id").value(ticketId))
                .andExpect(jsonPath("$[0].fastTrack").value(true));
        mockMvc.perform(get("/api/v1/tickets/" + ticketId).header(USER, "operatore.cc"))
                .andExpect(jsonPath("$.boardPostId").value(id(forAll)))
                .andExpect(jsonPath("$.boardPostNumber").value(forAll.getNumber()));
        mockMvc.perform(get("/api/v1/tickets/" + ticketId + "/history").header(USER, "tutor2"))
                .andExpect(jsonPath("$[0].action").value("SUBMIT"))
                .andExpect(jsonPath("$[0].toStatus").value("IN_LAVORAZIONE"))
                .andExpect(jsonPath("$[0].detail").value("Candidatura all'annuncio #" + forAll.getNumber()
                        + " della bacheca"));
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.TICKET,
                TsidMapper.toInternal(ticketId)))
                .singleElement()
                .satisfies(e -> assertThat(e.getChanges()).containsKeys("type", "fastTrack", "boardPostId"));

        // l'annuncio è bloccato: il Tutor lo vede "in valutazione", il Call Center vede quale segnalazione lo blocca
        mockMvc.perform(get("/api/v1/board/" + id(forAll)).header(USER, "tutor1"))
                .andExpect(jsonPath("$.candidatureInProgress").value(true));
        mockMvc.perform(get("/api/v1/board").header(USER, "tutor1"))
                .andExpect(jsonPath("$[?(@.number == %d)].candidatureInProgress".formatted(forGol.getNumber()))
                        .value(contains(false)));
        mockMvc.perform(get("/api/v1/board-posts/" + id(forAll)).header(USER, "operatore.cc"))
                .andExpect(jsonPath("$.candidatureTicketId").value(ticketId))
                .andExpect(jsonPath("$.candidatureTicketNumber").value(ticketNumber));
    }

    @Test
    void annuncioRiservato_valeIlSuoProgetto() throws Exception {
        candidate("tutor2", forGol, scenarios.beneficiary(actors.tutor2(), place), null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.project.code").value(gol.getCode()));
    }

    @Test
    void unaSolaCandidaturaPerAnnuncio() throws Exception {
        candidate("tutor1", forAll, scenarios.beneficiary(actors.tutor1(), place), gol)
                .andExpect(status().isCreated());
        candidate("tutor2", forAll, scenarios.beneficiary(actors.tutor2(), place), gol)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("L'annuncio #" + forAll.getNumber()
                        + " ha già una candidatura in valutazione"));
    }

    @Test
    void beneficiarioNonValido_400() throws Exception {
        // con una segnalazione aperta
        Beneficiary busy = scenarios.beneficiary(actors.tutor1(), place);
        candidate("tutor1", forGol, busy, null).andExpect(status().isCreated());
        candidate("tutor1", forAll, busy, gol)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("beneficiaryId"))
                .andExpect(jsonPath("$.fieldErrors[0].message", containsString("ha già una segnalazione aperta")));
        // di un altro Tutor
        candidate("tutor1", forAll, scenarios.beneficiary(actors.tutor2(), place), gol)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("beneficiaryId"));
        // annuncio per tutti: il progetto va scelto tra quelli del Tutor
        candidate("tutor1", forAll, scenarios.beneficiary(actors.tutor1(), place), null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("projectId"));
        candidate("tutor1", forAll, scenarios.beneficiary(actors.tutor1(), place), polis)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("projectId"));
    }

    @Test
    void annuncioNonVisibileOFuoriBacheca() throws Exception {
        Beneficiary mario = scenarios.beneficiary(actors.tutor1(), place);
        // riservato a un progetto del quale il Tutor non fa parte: per lui non esiste
        candidate("tutor1", forPolis, mario, null).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/board/" + id(forPolis)).header(USER, "tutor1")).andExpect(status().isNotFound());
        // bozza: non è in bacheca
        forAll.setStatus(BoardPostStatus.DRAFT);
        forAll.setPublishedAt(null);
        em.flush();
        candidate("tutor1", forAll, mario, gol).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/board/" + id(forAll)).header(USER, "tutor1")).andExpect(status().isNotFound());
        // archiviato nel frattempo
        forGol.setStatus(BoardPostStatus.ARCHIVED);
        em.flush();
        candidate("tutor1", forGol, mario, null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("L'annuncio #" + forGol.getNumber() + " non è più in bacheca"));
    }

    @Test
    void soloIlTutor() throws Exception {
        candidate("operatore.cc", forAll, scenarios.beneficiary(actors.tutor1(), place), gol)
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/board/" + id(forAll)).header(USER, "operatore.cc"))
                .andExpect(status().isForbidden());
    }
}
