package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
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
import it.nexus.domain.TestEntities;
import it.nexus.domain.Ticket;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.BoardPostStatus;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AuditEventRepository;
import it.nexus.repository.BoardPostRepository;
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
/**
 * Rifiuto della corsia veloce (US-404) e corsia veloce che decade quando l'annuncio esce dalla bacheca mentre la
 * candidatura aspetta (review dello Sprint 6, §8.1).
 */
class FastTrackRejectIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired WorkflowScenarios scenarios;
    @Autowired TicketRepository tickets;
    @Autowired BoardPostRepository boardPosts;
    @Autowired AuditEventRepository audit;

    private Actors actors;
    private Project gol;
    private Place place;
    private JobSlot slot;
    private BoardPost post;
    private Ticket special;

    @BeforeEach
    void setUp() {
        actors = scenarios.actors();
        gol = scenarios.project("RGOL", actors.tutor1(), actors.tutor2());
        place = scenarios.place();
        slot = scenarios.freeSlot(place, "Magazziniere");
        post = scenarios.publishedPost(slot, null);
        special = scenarios.specialTicket(actors.tutor1(), gol, post, actors.operator());
        em.flush();
        em.clear();
    }

    private static String id(Ticket t) {
        return TsidMapper.toExternal(t.getId());
    }

    private long version(Ticket t) {
        return tickets.findById(t.getId()).orElseThrow().getVersion();
    }

    private ResultActions reject(String user, Ticket t, String reason) throws Exception {
        String json = reason == null ? "{\"version\": %d}".formatted(version(t))
                : "{\"version\": %d, \"reason\": \"%s\"}".formatted(version(t), reason);
        return mockMvc.perform(post("/api/v1/tickets/" + id(t) + "/fast-track/reject").header(USER, user)
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private Ticket reloaded(Ticket t) {
        em.flush();
        em.clear();
        return tickets.findById(t.getId()).orElseThrow();
    }

    @Test
    void rifiuto_restaSpecialeInLavorazioneEAnnuncioDiNuovoCandidabile() throws Exception {
        reject("operatore.cc", special, "L'azienda cerca un profilo con patentino del muletto")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_LAVORAZIONE"))
                .andExpect(jsonPath("$.type").value("SPECIAL"))
                .andExpect(jsonPath("$.fastTrack").value(false))
                .andExpect(jsonPath("$.boardPostNumber").value(post.getNumber()));

        assertThat(reloaded(special).isFastTrack()).isFalse();
        assertThat(boardPosts.findById(post.getId()).orElseThrow().getStatus()).isEqualTo(BoardPostStatus.PUBLISHED);
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.TICKET, special.getId()))
                .singleElement()
                .satisfies(e -> {
                    assertThat(e.getAction()).isEqualTo("FAST_TRACK_REJECT");
                    assertThat(e.getChanges()).containsKeys("fastTrack");
                    assertThat(e.getReason()).isEqualTo("L'azienda cerca un profilo con patentino del muletto");
                });
        mockMvc.perform(get("/api/v1/tickets/" + id(special) + "/history").header(USER, "tutor1"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].action").value("FAST_TRACK_REJECT"))
                .andExpect(jsonPath("$[0].note").value("L'azienda cerca un profilo con patentino del muletto"));
        mockMvc.perform(get("/api/v1/notifications").header(USER, "tutor1"))
                .andExpect(jsonPath("$.items[0].type").value("FAST_TRACK_REJECTED"));

        // l'annuncio torna candidabile: un altro Tutor ci candida un suo beneficiario
        mockMvc.perform(get("/api/v1/board/" + TsidMapper.toExternal(post.getId())).header(USER, "tutor2"))
                .andExpect(jsonPath("$.candidatureInProgress").value(false));
        mockMvc.perform(post("/api/v1/tickets/special").header(USER, "tutor2").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"boardPostId\": \"%s\", \"beneficiaryId\": \"%s\", \"projectId\": \"%s\"}".formatted(
                                TsidMapper.toExternal(post.getId()),
                                TsidMapper.toExternal(scenarios.beneficiary(actors.tutor2(), place).getId()),
                                TsidMapper.toExternal(gol.getId()))))
                .andExpect(status().isCreated());

        // e la segnalazione rifiutata si lavora come una normale: si abbina un'altra mansione
        JobSlot other = scenarios.freeSlot(place, "Mulettista");
        em.flush();
        mockMvc.perform(post("/api/v1/tickets/" + id(special) + "/match").header(USER, "operatore.cc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobSlotId\": \"%s\", \"ticketVersion\": %d, \"jobSlotVersion\": %d}".formatted(
                                TsidMapper.toExternal(other.getId()), version(special), other.getVersion())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROPOSTA_AZIENDA"));
    }

    @Test
    void motivoObbligatorio_presaInCarico_ruolo_eUnaVoltaSola() throws Exception {
        reject("operatore.cc", special, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("reason"));
        reject("operatore.cc", special, "  ").andExpect(status().isBadRequest());
        reject("tutor1", special, "Motivo").andExpect(status().isForbidden());

        Ticket unassigned = scenarios.specialTicket(actors.tutor2(), gol,
                scenarios.publishedPost(scenarios.freeSlot(place, "Cuoco"), null), null);
        reject("operatore.cc", unassigned, "Motivo")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Prendi in carico la segnalazione n. " + unassigned.getNumber()
                        + " prima di decidere sulla corsia veloce"));

        reject("admin", special, "Motivo").andExpect(status().isOk());
        reject("operatore.cc", special, "Di nuovo")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("La segnalazione n. " + special.getNumber()
                        + " non ha una corsia veloce in attesa"));
    }

    @Test
    void annuncioArchiviatoDalCallCenter_laCorsiaVeloceDecade() throws Exception {
        mockMvc.perform(post("/api/v1/board-posts/" + TsidMapper.toExternal(post.getId()) + "/archive")
                        .header(USER, "operatore.cc").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\": " + boardPosts.findById(post.getId()).orElseThrow().getVersion() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidatureTicketId").doesNotExist());

        Ticket t = reloaded(special);
        assertThat(t.isFastTrack()).isFalse();
        assertThat(t.getStatus()).isEqualTo(TicketStatus.IN_LAVORAZIONE);
        mockMvc.perform(get("/api/v1/tickets/" + id(special) + "/history").header(USER, "tutor1"))
                .andExpect(jsonPath("$[-1].action").value("FAST_TRACK_DROP"))
                .andExpect(jsonPath("$[-1].note").value("Annuncio #" + post.getNumber() + " archiviato dal Call Center"));
        mockMvc.perform(get("/api/v1/notifications").header(USER, "tutor1"))
                .andExpect(jsonPath("$.items[0].type").value("FAST_TRACK_DROPPED"));
    }

    @Test
    void mansioneAbbinataAUnAltraSegnalazione_laCorsiaVeloceDecadeConIlMotivo() throws Exception {
        Ticket normal = TestEntities.ticket(actors.tutor2(), gol, scenarios.beneficiary(actors.tutor2(), place),
                place.category());
        normal.setStatus(TicketStatus.IN_LAVORAZIONE);
        normal.setAssignedCcOperator(actors.operator());
        normal = tickets.saveAndFlush(normal);

        mockMvc.perform(post("/api/v1/tickets/" + id(normal) + "/match").header(USER, "operatore.cc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobSlotId\": \"%s\", \"ticketVersion\": %d, \"jobSlotVersion\": %d}".formatted(
                                TsidMapper.toExternal(slot.getId()), normal.getVersion(), slot.getVersion())))
                .andExpect(status().isOk());

        assertThat(reloaded(special).isFastTrack()).isFalse();
        mockMvc.perform(get("/api/v1/tickets/" + id(special) + "/history").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$[-1].action").value("FAST_TRACK_DROP"))
                .andExpect(jsonPath("$[-1].note").value("Annuncio #" + post.getNumber()
                        + " archiviato: Mansione abbinata alla segnalazione n. " + normal.getNumber()));
    }
}
