package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.security.MockHeaderAuthenticationFilter;
import it.nexus.domain.AuditEvent;
import it.nexus.domain.Beneficiary;
import it.nexus.domain.Internship;
import it.nexus.domain.Project;
import it.nexus.domain.TestEntities;
import it.nexus.domain.Ticket;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.RetentionStatus;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AuditEventRepository;
import it.nexus.repository.BeneficiaryRepository;
import it.nexus.repository.InternshipRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.services.BeneficiaryRetentionService;
import it.nexus.support.WorkflowScenarios;
import it.nexus.support.WorkflowScenarios.Actors;
import it.nexus.support.WorkflowScenarios.Place;

/** Conservazione dei dati dei beneficiari (US-1303): segno notturno, anonimizzazione confermata o dati tenuti. */
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, WorkflowScenarios.class})
class BeneficiaryRetentionIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;

    @Autowired MockMvc mockMvc;
    @Autowired WorkflowScenarios scenarios;
    @Autowired BeneficiaryRetentionService retention;
    @Autowired BeneficiaryRepository beneficiaries;
    @Autowired TicketRepository tickets;
    @Autowired InternshipRepository internships;
    @Autowired AuditEventRepository audit;
    @Autowired JdbcTemplate jdbc;

    private Actors actors;
    private Place place;
    private Project gol;

    @BeforeEach
    void setUp() {
        actors = scenarios.actors();
        place = scenarios.place();
        gol = scenarios.project("GOL", actors.tutor1(), actors.tutor2());
    }

    /** Beneficiario di tutor1 con una segnalazione chiusa e la scheda del tirocinio, fermo da 25 mesi. */
    private Beneficiary inactive() {
        Beneficiary beneficiary = scenarios.beneficiary(actors.tutor1(), place);
        beneficiary.setConstraints("Solo mattina");
        beneficiaries.saveAndFlush(beneficiary);
        Ticket ticket = TestEntities.ticket(actors.tutor1(), gol, beneficiary, place.category());
        ticket.setStatus(TicketStatus.CLOSED);
        tickets.saveAndFlush(ticket);
        Internship internship = new Internship(ticket);
        internship.setFiscalCode("RSSMRA95A01D612X");
        internship.setBirthDate(LocalDate.of(1995, 1, 1));
        internship.setBirthPlace("Firenze");
        internship.setResidence("Via Roma 1, Firenze");
        internship.setNotes("Note del Tutor");
        internships.saveAndFlush(internship);
        Instant old = Instant.now().minus(25 * 31L, ChronoUnit.DAYS);
        jdbc.update("UPDATE beneficiary SET updated_at = ? WHERE id = ?", java.sql.Timestamp.from(old), beneficiary.getId());
        jdbc.update("UPDATE ticket SET updated_at = ? WHERE id = ?", java.sql.Timestamp.from(old), ticket.getId());
        jdbc.update("UPDATE internship SET updated_at = ? WHERE id = ?", java.sql.Timestamp.from(old),
                internship.getId());
        return beneficiary;
    }

    private static String id(Beneficiary b) {
        return TsidMapper.toExternal(b.getId());
    }

    private Beneficiary reload(Beneficiary b) {
        return beneficiaries.findById(b.getId()).orElseThrow();
    }

    private List<String> actions(Beneficiary b) {
        return audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.BENEFICIARY, b.getId()).stream()
                .map(AuditEvent::getAction).toList();
    }

    /** Scheda valida del beneficiario, per provare la modifica. */
    private String form() {
        return """
                {"firstName": "Mario", "lastName": "Rossi", "birthYear": 1995, "gender": "M",
                 "nationality": "IT", "citizenship": "IT", "residenceZoneId": "%s",
                 "licenseTypes": [], "hasVehicle": false, "hasLaw68": false, "languages": []}
                """.formatted(TsidMapper.toExternal(place.zone().getId()));
    }

    @Test
    void modificaDelTutor_ilSegnoSiToglieSubito() throws Exception {
        Beneficiary old = inactive();
        retention.review(Instant.now());

        mockMvc.perform(put("/api/v1/beneficiaries/" + id(old)).header(USER, "tutor1")
                        .contentType(MediaType.APPLICATION_JSON).content(form()))
                .andExpect(status().isOk());

        assertThat(reload(old).getRetention()).isNull();
    }

    private ResultActions keep(String user, Beneficiary b, String reason) throws Exception {
        return mockMvc.perform(post("/api/v1/beneficiaries/" + id(b) + "/keep").header(USER, user)
                .contentType(MediaType.APPLICATION_JSON).content("{\"reason\": \"" + reason + "\"}"));
    }

    @Test
    void ilJobSegnaSoloChiNonHaAttivitaDa2Anni_eNonTocca_updatedAt() {
        Beneficiary old = inactive();
        Beneficiary recent = scenarios.beneficiary(actors.tutor1(), place);
        Instant before = jdbc.queryForObject("SELECT updated_at FROM beneficiary WHERE id = ?", java.sql.Timestamp.class,
                old.getId()).toInstant();

        assertThat(retention.review(Instant.now())).isEqualTo(1);

        assertThat(reload(old).getRetention()).isEqualTo(RetentionStatus.TO_ANONYMIZE);
        assertThat(reload(recent).getRetention()).isNull();
        // il segno non è un'attività: la notte dopo il beneficiario resta da anonimizzare
        assertThat(jdbc.queryForObject("SELECT updated_at FROM beneficiary WHERE id = ?", java.sql.Timestamp.class,
                old.getId()).toInstant()).isEqualTo(before);
        assertThat(retention.review(Instant.now())).isZero();
        assertThat(reload(old).getRetention()).isEqualTo(RetentionStatus.TO_ANONYMIZE);
        assertThat(actions(old)).containsExactly("ANONYMIZATION_DUE");
    }

    @Test
    void nuovaAttivita_ilSegnoSiToglie() throws Exception {
        Beneficiary old = inactive();
        retention.review(Instant.now());

        // la sua segnalazione si muove: alla notte dopo il segno si toglie
        // clock_timestamp(): nel test now() è l'inizio della transazione, prima del segno
        jdbc.update("UPDATE ticket SET updated_at = clock_timestamp() WHERE beneficiary_id = ?", old.getId());
        retention.review(Instant.now());

        assertThat(reload(old).getRetention()).isNull();
        assertThat(actions(old)).containsExactly("ANONYMIZATION_DUE", "ANONYMIZATION_CANCELLED");
    }

    @Test
    void ilTutorAnonimizza_restanoLeIniziali_eLaSchedaDelTirocinioSiSvuota() throws Exception {
        Beneficiary old = inactive();
        retention.review(Instant.now());

        mockMvc.perform(get("/api/v1/beneficiaries").header(USER, "tutor1"))
                .andExpect(jsonPath("$[?(@.id == '" + id(old) + "')].retention", contains("TO_ANONYMIZE")));
        mockMvc.perform(post("/api/v1/beneficiaries/" + id(old) + "/anonymize").header(USER, "tutor1"))
                .andExpect(status().isNoContent());

        Beneficiary anonymized = reload(old);
        assertThat(anonymized.getFirstName()).isEqualTo("M");
        assertThat(anonymized.getLastName()).isEqualTo("R");
        assertThat(anonymized.getConstraints()).isNull();
        assertThat(anonymized.getBirthYear()).isEqualTo(1995);
        assertThat(anonymized.getRetention()).isEqualTo(RetentionStatus.ANONYMIZED);
        Internship internship = internships.findByBeneficiaryId(old.getId()).getFirst();
        assertThat(internship.getFiscalCode()).isNull();
        assertThat(internship.getBirthDate()).isNull();
        assertThat(internship.getBirthPlace()).isNull();
        assertThat(internship.getResidence()).isNull();
        assertThat(internship.getNotes()).isNull();
        assertThat(actions(old)).containsExactly("ANONYMIZATION_DUE", "ANONYMIZE");
        // nell'audit nessun valore personale
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.BENEFICIARY, old.getId())
                .getLast().getChanges().toString()).doesNotContain("Mario", "Rossi", "mattina");

        // anonimizzato: non si modifica, non si segnala, non si anonimizza due volte
        mockMvc.perform(put("/api/v1/beneficiaries/" + id(old)).header(USER, "tutor1")
                        .contentType(MediaType.APPLICATION_JSON).content(form()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Il beneficiario è anonimizzato: i suoi dati non si modificano più"));
        mockMvc.perform(post("/api/v1/beneficiaries/" + id(old) + "/anonymize").header(USER, "admin"))
                .andExpect(status().isBadRequest());
        assertThat(retention.review(Instant.now())).isZero();
    }

    @Test
    void lAdminTieneIDati_conIlMotivo_eNonVieneSegnatoPiu() throws Exception {
        Beneficiary old = inactive();
        retention.review(Instant.now());

        mockMvc.perform(get("/api/v1/beneficiaries/all").param("toAnonymize", "true").header(USER, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", contains(id(old))))
                .andExpect(jsonPath("$[0].ownerTutorName").value("Nome Cognome"));
        keep("admin", old, " ").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("reason"));
        keep("admin", old, "Contenzioso in corso").andExpect(status().isNoContent());

        assertThat(reload(old).getRetention()).isEqualTo(RetentionStatus.KEPT);
        assertThat(reload(old).getKeptReason()).isEqualTo("Contenzioso in corso");
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.BENEFICIARY, old.getId())
                .getLast().getReason()).isEqualTo("Contenzioso in corso");
        mockMvc.perform(get("/api/v1/beneficiaries/all").header(USER, "admin"))
                .andExpect(jsonPath("$[*].retention", hasItem("KEPT")));
        // tenuto per sempre: la notte non lo segna più
        jdbc.update("UPDATE beneficiary SET updated_at = now() - interval '3 years' WHERE id = ?", old.getId());
        assertThat(retention.review(Instant.now())).isZero();
    }

    @Test
    void decideSoloLAdminOIlSuoTutor_eSoloSeEDaAnonimizzare() throws Exception {
        Beneficiary old = inactive();
        Beneficiary recent = scenarios.beneficiary(actors.tutor1(), place);
        retention.review(Instant.now());

        mockMvc.perform(post("/api/v1/beneficiaries/" + id(old) + "/anonymize").header(USER, "tutor2"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/beneficiaries/" + id(old) + "/anonymize").header(USER, "operatore.cc"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/beneficiaries/all").header(USER, "tutor1")).andExpect(status().isForbidden());
        keep("tutor1", recent, "Motivo").andExpect(status().isBadRequest());
        assertThat(reload(old).getRetention()).isEqualTo(RetentionStatus.TO_ANONYMIZE);
    }
}
