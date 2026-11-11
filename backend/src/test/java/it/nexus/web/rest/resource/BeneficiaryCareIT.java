package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

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
import it.nexus.domain.Beneficiary;
import it.nexus.domain.Project;
import it.nexus.domain.Ticket;
import it.nexus.mapper.TsidMapper;
import it.nexus.services.BeneficiaryRetentionService;
import it.nexus.support.WorkflowScenarios;
import it.nexus.support.WorkflowScenarios.Actors;
import it.nexus.support.WorkflowScenarios.Place;
import jakarta.persistence.EntityManager;

/** Contatti, ente di presa in carico e case manager del beneficiario; tirocinio di inclusione (US-206). */
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, WorkflowScenarios.class})
class BeneficiaryCareIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;
    /** Voci iniziali della V34. */
    private static final String COMUNE = TsidMapper.toExternal(893700000000000351L);
    private static final String SDS_FIRENZE = TsidMapper.toExternal(893700000000000352L);
    private static final String CASE_MANAGER = """
            , "careAgencyId": "%s", "caseManagerName": "Lucia Prova", "caseManagerOffice": "Via Prova 1, Firenze",
              "caseManagerPhone": "055 1234567", "caseManagerEmail": "lucia.prova@esempio.it\"""".formatted(COMUNE);

    @Autowired MockMvc mockMvc;
    @Autowired WorkflowScenarios scenarios;
    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManager em;
    @Autowired BeneficiaryRetentionService retention;

    private Actors actors;
    private Place place;
    private Ticket ticket;
    private Beneficiary beneficiary;

    @BeforeEach
    void setUp() {
        actors = scenarios.actors();
        place = scenarios.place();
        Project project = scenarios.project("CARE", actors.tutor1());
        ticket = scenarios.appointmentTicket(actors.tutor1(), project, scenarios.freeSlot(place, "Aiuto cuoco"),
                actors.operator());
        beneficiary = ticket.getBeneficiary();
    }

    private String url() {
        return "/api/v1/beneficiaries/" + TsidMapper.toExternal(beneficiary.getId());
    }

    private String body(String extra) {
        return """
                {"firstName": "Mario", "lastName": "Rossi", "birthYear": 1998, "gender": "M",
                 "residenceZoneId": "%s", "licenseTypes": [], "hasVehicle": false, "hasLaw68": false,
                 "languages": [] %s}""".formatted(TsidMapper.toExternal(beneficiary.getResidenceZone().getId()), extra);
    }

    private ResultActions update(String extra) throws Exception {
        return mockMvc.perform(put(url()).header(USER, "tutor1").contentType(MediaType.APPLICATION_JSON)
                .content(body(extra)));
    }

    private String internshipUrl() {
        return "/api/v1/tickets/" + TsidMapper.toExternal(ticket.getId()) + "/internship";
    }

    private ResultActions saveInternship(String typeCode, Long version) throws Exception {
        String typeId = TsidMapper.toExternal(jdbc.queryForObject("SELECT id FROM internship_type WHERE code = ?",
                Long.class, typeCode));
        return mockMvc.perform(put(internshipUrl()).header(USER, "tutor1").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"internshipTypeId": "%s", "companyTutors": [], "suspensions": [], "trainingProjects": [], "schedule": [],
                         "firstSlotStart": "09:00", "firstSlotEnd": "13:00", "secondSlotStart": "14:00",
                         "secondSlotEnd": "18:00", "excludeHolidays": true, "version": %s}"""
                        .formatted(typeId, version)));
    }

    @Test
    void contattiEnteECaseManager_salvatiEAuditSenzaValori() throws Exception {
        update(", \"email\": \"mario.rossi@esempio.it\", \"phone\": \"333 1234567\"" + CASE_MANAGER)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("mario.rossi@esempio.it"))
                .andExpect(jsonPath("$.careAgency.name").value("Comune di Firenze"))
                .andExpect(jsonPath("$.caseManagerEmail").value("lucia.prova@esempio.it"))
                .andExpect(jsonPath("$.careAgencyLocked").value(false));
        em.flush();
        String changes = jdbc.queryForObject("SELECT changes::text FROM audit_event WHERE entity_type = "
                + "'BENEFICIARY' AND entity_id = ? AND action = 'UPDATE'", String.class, beneficiary.getId());
        assertThat(changes).contains("careAgency", "caseManagerEmail").doesNotContain("esempio.it")
                .doesNotContain("Lucia");
        // anche il Call Center, che vede il beneficiario, vede i dati
        mockMvc.perform(get(url()).header(USER, "operatore.cc"))
                .andExpect(jsonPath("$.caseManagerName").value("Lucia Prova"));
    }

    @Test
    void datiNonValidi() throws Exception {
        update(", \"email\": \"non-una-mail\"").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("email"));
        update(", \"caseManagerPhone\": \"chiamami\"").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("caseManagerPhone"));
        jdbc.update("UPDATE care_agency SET active = false WHERE code = 'SOCIETA_DELLA_SALUTE_DI_FIRENZE'");
        update(", \"careAgencyId\": \"" + SDS_FIRENZE + "\"").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("careAgencyId"));
    }

    @Test
    void inclusione_soloConEnteECaseManager_poiEnteBloccato() throws Exception {
        mockMvc.perform(get(internshipUrl()).header(USER, "tutor1"))
                .andExpect(jsonPath("$.inclusionMissing.length()").value(4));
        saveInternship("INCLUSIONE", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].message").value(containsString("l'ente che lo ha in carico")));

        update(CASE_MANAGER).andExpect(status().isOk());
        mockMvc.perform(get(internshipUrl()).header(USER, "tutor1"))
                .andExpect(jsonPath("$.inclusionMissing.length()").value(0));
        saveInternship("INCLUSIONE", null).andExpect(status().isOk());

        // tirocinio aperto: l'ente non cambia, il case manager sì, ma non si svuota
        mockMvc.perform(get(url()).header(USER, "tutor1"))
                .andExpect(jsonPath("$.careAgencyLocked").value(true))
                .andExpect(jsonPath("$.caseManagerRequired").value(true));
        update(CASE_MANAGER.replace(COMUNE, SDS_FIRENZE)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("careAgencyId"));
        update(CASE_MANAGER.replace("Lucia Prova", "Paolo Nuovo")).andExpect(status().isOk())
                .andExpect(jsonPath("$.caseManagerName").value("Paolo Nuovo"));
        update(CASE_MANAGER.replace("lucia.prova@esempio.it", "")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("caseManagerEmail"));

        // segnalazione chiusa: l'ente si può di nuovo cambiare
        jdbc.update("UPDATE ticket SET status = 'FORM_RESTITUZIONE' WHERE id = ?", ticket.getId());
        em.clear();
        update(CASE_MANAGER.replace(COMUNE, SDS_FIRENZE)).andExpect(status().isOk())
                .andExpect(jsonPath("$.careAgencyLocked").value(false));
    }

    @Test
    void altroTipo_nessunVincolo() throws Exception {
        saveInternship("EXTRACURRICULARE", null).andExpect(status().isOk());
        mockMvc.perform(get(url()).header(USER, "tutor1"))
                .andExpect(jsonPath("$.careAgencyLocked").value(true))
                .andExpect(jsonPath("$.caseManagerRequired").value(false));
        update("").andExpect(status().isOk());
    }

    @Test
    void anonimizzazione_cancellaContattiECaseManager() throws Exception {
        update(", \"email\": \"mario.rossi@esempio.it\", \"phone\": \"333 1234567\"" + CASE_MANAGER)
                .andExpect(status().isOk());
        em.flush();
        Instant old = Instant.now().minusSeconds(3L * 365 * 24 * 3600);
        jdbc.update("UPDATE beneficiary SET updated_at = ? WHERE id = ?", java.sql.Timestamp.from(old),
                beneficiary.getId());
        jdbc.update("UPDATE ticket SET status = 'FORM_RESTITUZIONE', updated_at = ? WHERE id = ?",
                java.sql.Timestamp.from(old), ticket.getId());
        em.clear();
        retention.review(Instant.now());
        mockMvc.perform(post(url() + "/anonymize").header(USER, "tutor1")).andExpect(status().isNoContent());
        em.flush();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM beneficiary WHERE id = ? AND email IS NULL AND "
                + "phone IS NULL AND care_agency_id IS NULL AND case_manager_name IS NULL AND case_manager_office "
                + "IS NULL AND case_manager_phone IS NULL AND case_manager_email IS NULL", Integer.class,
                beneficiary.getId())).isEqualTo(1);
    }

    @Test
    void entiPerIModuli() throws Exception {
        mockMvc.perform(get("/api/v1/reference/care-agencies").header(USER, "tutor1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'Comune di Firenze')]").exists());
    }
}
