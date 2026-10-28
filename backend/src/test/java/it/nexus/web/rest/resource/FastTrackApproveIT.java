package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.security.MockHeaderAuthenticationFilter;
import it.nexus.domain.BoardPost;
import it.nexus.domain.JobSlot;
import it.nexus.domain.Project;
import it.nexus.domain.Ticket;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.BoardPostStatus;
import it.nexus.domain.enumeration.JobSlotStatus;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AuditEventRepository;
import it.nexus.repository.BoardPostRepository;
import it.nexus.repository.CompanyAuditEventRepository;
import it.nexus.repository.JobSlotRepository;
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
/** Approvazione della corsia veloce (US-403): proposta, mansione bloccata e annuncio archiviato in un'operazione. */
class FastTrackApproveIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired WorkflowScenarios scenarios;
    @Autowired TicketRepository tickets;
    @Autowired JobSlotRepository jobSlots;
    @Autowired BoardPostRepository boardPosts;
    @Autowired AuditEventRepository audit;
    @Autowired CompanyAuditEventRepository companyAudit;

    private Actors actors;
    private Project gol;
    private Place place;
    private JobSlot slot;
    private BoardPost post;
    private Ticket special;

    @BeforeEach
    void setUp() {
        actors = scenarios.actors();
        gol = scenarios.project("FGOL", actors.tutor1());
        place = scenarios.place();
        slot = scenarios.freeSlot(place, "Magazziniere");
        post = scenarios.publishedPost(slot, null);
        special = scenarios.specialTicket(actors.tutor1(), gol, post, actors.operator());
        em.flush();
        em.clear();
    }

    private String id() {
        return TsidMapper.toExternal(special.getId());
    }

    private long version() {
        return tickets.findById(special.getId()).orElseThrow().getVersion();
    }

    private ResultActions approve(String user, long version) throws Exception {
        return mockMvc.perform(post("/api/v1/tickets/" + id() + "/fast-track/approve").header(USER, user)
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\": " + version + "}"));
    }

    @Test
    void approvazione_propostaMansioneBloccataAnnuncioArchiviatoTuttoTracciato() throws Exception {
        approve("operatore.cc", version())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROPOSTA_AZIENDA"))
                .andExpect(jsonPath("$.type").value("SPECIAL"))
                .andExpect(jsonPath("$.fastTrack").value(false))
                .andExpect(jsonPath("$.proposal.id").value(TsidMapper.toExternal(slot.getId())))
                .andExpect(jsonPath("$.boardPostNumber").value(post.getNumber()));
        em.flush();
        em.clear();

        JobSlot reloaded = jobSlots.findById(slot.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(JobSlotStatus.BLOCCATA);
        assertThat(reloaded.getBlockedByTicket().getId()).isEqualTo(special.getId());
        assertThat(boardPosts.findById(post.getId()).orElseThrow().getStatus()).isEqualTo(BoardPostStatus.ARCHIVED);
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.BOARD_POST, post.getId()))
                .singleElement()
                .satisfies(e -> assertThat(e.getReason())
                        .isEqualTo("Corsia veloce approvata: segnalazione n. " + special.getNumber()));
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.TICKET, special.getId()))
                .singleElement()
                .satisfies(e -> {
                    assertThat(e.getAction()).isEqualTo("FAST_TRACK_APPROVE");
                    assertThat(e.getChanges()).containsKeys("status", "jobSlotId", "companyId", "fastTrack");
                });
        assertThat(companyAudit.findByCompanyIdOrderByCreatedAtAsc(place.company().getId()))
                .extracting(e -> e.getEventType()).containsExactly("PROPOSTA_INVIATA");
        mockMvc.perform(get("/api/v1/tickets/" + id() + "/history").header(USER, "tutor1"))
                .andExpect(jsonPath("$[-1].action").value("FAST_TRACK_APPROVE"))
                .andExpect(jsonPath("$[-1].detail").value("«Magazziniere» presso " + place.company().getName()));
        mockMvc.perform(get("/api/v1/notifications").header(USER, "tutor1"))
                .andExpect(jsonPath("$.items[0].type").value("PROPOSAL_SENT"));
        // l'annuncio non è più in bacheca; il Tutor prosegue come con una proposta normale
        mockMvc.perform(get("/api/v1/board").header(USER, "tutor1")).andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(post("/api/v1/tickets/" + id() + "/accept").header(USER, "tutor1")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"version\": " + version() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROPOSTA_ACCOLTA"));
    }

    @Test
    void nonPresaInCarico_409() throws Exception {
        Ticket unassigned = scenarios.specialTicket(actors.tutor1(), gol,
                scenarios.publishedPost(scenarios.freeSlot(place, "Cuoco"), null), null);
        mockMvc.perform(post("/api/v1/tickets/" + TsidMapper.toExternal(unassigned.getId()) + "/fast-track/approve")
                        .header(USER, "operatore.cc").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\": " + unassigned.getVersion() + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Prendi in carico la segnalazione n. " + unassigned.getNumber()
                        + " prima di decidere sulla corsia veloce"));
    }

    @Test
    void mansioneNonPiuDisponibileOAnnuncioArchiviato_409() throws Exception {
        JobSlot s = jobSlots.findById(slot.getId()).orElseThrow();
        s.setActive(false);
        jobSlots.saveAndFlush(s);
        approve("operatore.cc", version())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "La mansione «Magazziniere» non è più disponibile: è stata appena bloccata o modificata"));

        s.setActive(true);
        jobSlots.saveAndFlush(s);
        BoardPost p = boardPosts.findById(post.getId()).orElseThrow();
        p.setStatus(BoardPostStatus.ARCHIVED);
        boardPosts.saveAndFlush(p);
        approve("operatore.cc", version())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("L'annuncio #" + post.getNumber() + " non è più in bacheca"));
    }

    @Test
    void unaVoltaSola_versioneSuperata_eSoloIlCallCenter() throws Exception {
        approve("tutor1", version()).andExpect(status().isForbidden());
        approve("operatore.cc", version() + 3).andExpect(status().isConflict());
        approve("admin", version()).andExpect(status().isOk());
        approve("operatore.cc", version())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("La segnalazione n. " + special.getNumber()
                        + " non ha una corsia veloce in attesa"));
    }

    @Test
    void conLaCorsiaVeloceInAttesaNonSiAbbinaAltro() throws Exception {
        JobSlot other = scenarios.freeSlot(place, "Mulettista");
        em.flush();
        mockMvc.perform(post("/api/v1/tickets/" + id() + "/match").header(USER, "operatore.cc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobSlotId\": \"%s\", \"ticketVersion\": %d, \"jobSlotVersion\": %d}".formatted(
                                TsidMapper.toExternal(other.getId()), version(), other.getVersion())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Decidi prima sulla corsia veloce della segnalazione n. "
                        + special.getNumber()));
    }

    @Test
    void presaInCaricoDellaSpeciale_nessunCambioDiStatoVuotoInCronologia() throws Exception {
        Ticket unassigned = scenarios.specialTicket(actors.tutor1(), gol,
                scenarios.publishedPost(scenarios.freeSlot(place, "Barista"), null), null);
        String ticketId = TsidMapper.toExternal(unassigned.getId());
        mockMvc.perform(post("/api/v1/tickets/" + ticketId + "/take-charge").header(USER, "operatore.cc")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"version\": " + unassigned.getVersion() + "}"))
                .andExpect(status().isOk());

        // la speciale è già in lavorazione: nessun "Cambio di stato da In lavorazione a In lavorazione"
        mockMvc.perform(get("/api/v1/tickets/" + ticketId + "/history").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$[*].action", contains("TAKE_CHARGE")));
    }
}
