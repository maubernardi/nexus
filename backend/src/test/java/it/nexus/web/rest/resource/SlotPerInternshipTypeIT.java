package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import it.nexus.domain.InternshipType;
import it.nexus.domain.JobSlot;
import it.nexus.domain.Project;
import it.nexus.domain.TestEntities;
import it.nexus.domain.Ticket;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.JobSlotStatus;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AuditEventRepository;
import it.nexus.repository.InternshipTypeRepository;
import it.nexus.repository.JobSlotRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.support.WorkflowScenarios;
import it.nexus.support.WorkflowScenarios.Actors;
import it.nexus.support.WorkflowScenarios.Place;
import jakarta.persistence.EntityManager;

/** US-605: una mansione per più segnalazioni, una per tipo di tirocinio. */
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, WorkflowScenarios.class})
class SlotPerInternshipTypeIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;
    private static final String API = "/api/v1/tickets/";

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired WorkflowScenarios scenarios;
    @Autowired TicketRepository tickets;
    @Autowired JobSlotRepository jobSlots;
    @Autowired AuditEventRepository audit;
    @Autowired InternshipTypeRepository internshipTypes;

    private Actors actors;
    private Place place;
    private Project project;
    private JobSlot slot;
    private InternshipType extracurricular;
    private InternshipType inclusion;

    @BeforeEach
    void setUp() {
        actors = scenarios.actors();
        place = scenarios.place();
        project = scenarios.project("GOL", actors.tutor1(), actors.tutor2());
        slot = scenarios.freeSlot(place, "Cameriere");
        extracurricular = scenarios.extracurricular();
        inclusion = scenarios.inclusion();
        em.flush();
    }

    /** Segnalazione presa in carico, pronta per l'abbinamento, del tipo indicato (null = tipo non indicato). */
    private Ticket working(InternshipType type) {
        Ticket t = TestEntities.ticket(actors.tutor1(), project, scenarios.beneficiary(actors.tutor1(), place),
                place.category());
        t.setInternshipType(type);
        t.setStatus(TicketStatus.IN_LAVORAZIONE);
        t.setAssignedCcOperator(actors.operator());
        return tickets.saveAndFlush(t);
    }

    private static String id(Object entity) {
        return TsidMapper.toExternal(entity instanceof Ticket t ? t.getId() : ((JobSlot) entity).getId());
    }

    private ResultActions match(Ticket t) throws Exception {
        em.flush();
        em.refresh(slot);
        em.refresh(t);
        return mockMvc.perform(post(API + id(t) + "/match").header(USER, "operatore.cc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"jobSlotId\": \"%s\", \"ticketVersion\": %d, \"jobSlotVersion\": %d}"
                        .formatted(id(slot), t.getVersion(), slot.getVersion())));
    }

    private ResultActions compatible(Ticket t) throws Exception {
        return mockMvc.perform(get(API + id(t) + "/compatible-job-slots").header(USER, "operatore.cc"));
    }

    private ResultActions changeType(Ticket t, InternshipType type) throws Exception {
        return mockMvc.perform(put(API + id(t) + "/internship-type").header(USER, "tutor1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"internshipTypeId\": \"%s\"}".formatted(TsidMapper.toExternal(type.getId()))));
    }

    private JobSlot reloaded() {
        em.flush();
        em.clear();
        return jobSlots.findById(slot.getId()).orElseThrow();
    }

    @Test
    void stessaMansione_dueTipi_conLAvvisoNellAbbinamento() throws Exception {
        Ticket first = working(extracurricular);
        Ticket second = working(inclusion);
        match(first).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PROPOSTA_AZIENDA"));

        // per l'altro tipo la mansione resta proponibile, con l'avviso dell'assegnazione
        compatible(second)
                .andExpect(jsonPath("$[?(@.title == 'Cameriere')].assignments[0].ticketNumber")
                        .value(first.getNumber().intValue()))
                .andExpect(jsonPath("$[?(@.title == 'Cameriere')].assignments[0].internshipType.name")
                        .value("Extracurriculare"));
        match(second).andExpect(status().isOk());

        JobSlot both = reloaded();
        assertThat(both.getStatus()).isEqualTo(JobSlotStatus.BLOCCATA);
        assertThat(both.getBlockedByTicket().getId()).isEqualTo(first.getId());
        assertThat(tickets.findHolders(both.getId())).extracting(Ticket::getId)
                .containsExactly(first.getId(), second.getId());
        // le mansioni dell'azienda elencano tutte le assegnazioni
        mockMvc.perform(get("/api/v1/companies/" + TsidMapper.toExternal(place.company().getId()) + "/job-slots")
                        .header(USER, "operatore.cc"))
                .andExpect(jsonPath("$[0].assignments[*].ticketNumber",
                        contains(first.getNumber().intValue(), second.getNumber().intValue())));
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.JOB_SLOT, slot.getId()))
                .extracting(e -> e.getAction()).containsExactly("BLOCK", "BLOCK");
    }

    @Test
    void stessoTipo_409ConIlNumero_eNonCompareTraLeCompatibili() throws Exception {
        Ticket first = working(extracurricular);
        Ticket second = working(extracurricular);
        match(first).andExpect(status().isOk());

        compatible(second).andExpect(jsonPath("$[*].title", not(hasItem("Cameriere"))));
        match(second)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("La mansione «Cameriere» è già assegnata alla segnalazione n. %d "
                        .formatted(first.getNumber()) + "(Extracurriculare)"));
        assertThat(tickets.findById(second.getId()).orElseThrow().getStatus()).isEqualTo(TicketStatus.IN_LAVORAZIONE);
    }

    @Test
    void tipoNonIndicato_nonSiPropone_eVedeSoloLeMansioniLibere() throws Exception {
        Ticket untyped = working(null);
        compatible(untyped).andExpect(jsonPath("$[*].title", hasItem("Cameriere")));
        match(untyped)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("non ha il tipo di tirocinio: lo indica il Tutor")));

        // una mansione assegnata non compare per chi non ha il tipo
        match(working(extracurricular)).andExpect(status().isOk());
        compatible(untyped).andExpect(jsonPath("$[*].title", not(hasItem("Cameriere"))));
    }

    @Test
    void rifiuto_liberaSoloQuellaSegnalazione() throws Exception {
        Ticket first = working(extracurricular);
        Ticket second = working(inclusion);
        match(first).andExpect(status().isOk());
        match(second).andExpect(status().isOk());

        em.refresh(second);
        mockMvc.perform(post(API + id(second) + "/reject").header(USER, "tutor1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\": %d, \"reason\": \"Orari non compatibili\"}".formatted(second.getVersion())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RIAPERTO"));

        JobSlot afterReject = reloaded();
        assertThat(afterReject.getStatus()).isEqualTo(JobSlotStatus.BLOCCATA);
        assertThat(tickets.findHolders(afterReject.getId())).extracting(Ticket::getId).containsExactly(first.getId());
        // di nuovo proponibile per una segnalazione Di inclusione
        slot = afterReject;
        compatible(working(inclusion)).andExpect(jsonPath("$[*].title", hasItem("Cameriere")));

        Ticket firstNow = tickets.findById(first.getId()).orElseThrow();
        mockMvc.perform(post(API + id(firstNow) + "/reject").header(USER, "tutor1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\": %d, \"reason\": \"Non interessato\"}".formatted(firstNow.getVersion())))
                .andExpect(status().isOk());
        JobSlot free = reloaded();
        assertThat(free.getStatus()).isEqualTo(JobSlotStatus.LIBERA);
        assertThat(free.getBlockedByTicket()).isNull();
    }

    @Test
    void correzioneDelTipo_controlliSullaMansione_audit_eFermaDopoLAvvio() throws Exception {
        InternshipType curricular = scenarios.curricular();
        InternshipType other = internshipTypes.save(new InternshipType("T_605_" + slot.getId(), "Tipo di prova"));
        Ticket first = working(extracurricular);
        Ticket second = working(curricular);
        match(first).andExpect(status().isOk());
        match(second).andExpect(status().isOk());

        // verso il tipo dell'altra segnalazione sulla stessa mansione: no
        em.refresh(first);
        changeType(first, curricular)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("internshipTypeId"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value(containsString("n. " + second.getNumber())));
        // verso un tipo libero sulla mansione: sì, con l'audit e la cronologia
        changeType(first, other)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.internshipType.name").value("Tipo di prova"))
                .andExpect(jsonPath("$.canChangeInternshipType").value(true));
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.TICKET, first.getId()))
                .last()
                .satisfies(e -> {
                    assertThat(e.getAction()).isEqualTo("INTERNSHIP_TYPE_CHANGED");
                    assertThat(e.getChanges().get("internshipType")).extracting("before", "after")
                            .containsExactly("Extracurriculare", "Tipo di prova");
                });
        mockMvc.perform(get(API + id(first) + "/history").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$[?(@.action == 'INTERNSHIP_TYPE_CHANGED')].detail")
                        .value("Extracurriculare → Tipo di prova"));
        // la mansione è di nuovo proponibile per un'Extracurriculare
        slot = reloaded();
        compatible(working(extracurricular)).andExpect(jsonPath("$[*].title", hasItem("Cameriere")));

        // solo il Tutor della segnalazione
        mockMvc.perform(put(API + id(first) + "/internship-type").header(USER, "operatore.cc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"internshipTypeId\": \"%s\"}".formatted(TsidMapper.toExternal(extracurricular.getId()))))
                .andExpect(status().isForbidden());

        // dopo l'avvio il tipo non cambia
        Ticket started = tickets.findById(first.getId()).orElseThrow();
        started.setStatus(TicketStatus.IN_TIROCINIO);
        em.flush();
        changeType(started, extracurricular).andExpect(status().isConflict());
        mockMvc.perform(get(API + id(started)).header(USER, "tutor1"))
                .andExpect(jsonPath("$.canChangeInternshipType").value(false));
    }

    @Test
    void creazione_tipoObbligatorioAmmessoDalProgetto_inclusioneConIlCaseManager() throws Exception {
        String beneficiary = TsidMapper.toExternal(scenarios.beneficiary(actors.tutor1(), place).getId());
        String json = """
                {"beneficiaryId": "%s", "projectId": "%s", "jobCategoryId": "%s", "internshipTypeId": %s}""";
        String category = TsidMapper.toExternal(place.category().getId());
        mockMvc.perform(post("/api/v1/tickets").header(USER, "tutor1").contentType(MediaType.APPLICATION_JSON)
                        .content(json.formatted(beneficiary, id(project), category, "null")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("internshipTypeId"));
        // «Di inclusione» senza ente e case manager del beneficiario
        mockMvc.perform(post("/api/v1/tickets").header(USER, "tutor1").contentType(MediaType.APPLICATION_JSON)
                        .content(json.formatted(beneficiary, id(project), category,
                                "\"" + TsidMapper.toExternal(inclusion.getId()) + "\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("internshipTypeId"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value(containsString("case manager")));
        // il progetto ammette solo «Extracurriculare»
        project.getAllowedInternshipTypes().add(extracurricular);
        em.flush();
        mockMvc.perform(get("/api/v1/reference/internship-types").param("projectId", id(project)).header(USER, "tutor1"))
                .andExpect(jsonPath("$[*].name", contains("Extracurriculare")));
        mockMvc.perform(post("/api/v1/tickets").header(USER, "tutor1").contentType(MediaType.APPLICATION_JSON)
                        .content(json.formatted(beneficiary, id(project), category,
                                "\"" + TsidMapper.toExternal(scenarios.curricular().getId()) + "\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].message").value(containsString("non è ammesso dal progetto")));
        String created = com.jayway.jsonpath.JsonPath.read(mockMvc.perform(post("/api/v1/tickets").header(USER, "tutor1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.formatted(beneficiary, id(project), category, "\"" + TestEntities.EXTRACURRICULAR + "\"")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id");
        mockMvc.perform(get(API + created).header(USER, "operatore.cc"))
                .andExpect(jsonPath("$.internshipType.name").value("Extracurriculare"))
                .andExpect(jsonPath("$.canChangeInternshipType").value(false));
    }

    private static String id(Project project) {
        return TsidMapper.toExternal(project.getId());
    }
}
