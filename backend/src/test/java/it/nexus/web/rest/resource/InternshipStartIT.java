package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

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
import it.nexus.domain.CompanyAtecoCode;
import it.nexus.domain.CompanyLegalRepresentative;
import it.nexus.domain.InternshipType;
import it.nexus.domain.JobSlot;
import it.nexus.domain.Project;
import it.nexus.domain.Ticket;
import it.nexus.domain.enumeration.JobSlotStatus;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.CompanyAtecoCodeRepository;
import it.nexus.repository.CompanyAuditEventRepository;
import it.nexus.repository.CompanyLegalRepresentativeRepository;
import it.nexus.repository.InternshipTypeRepository;
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
/** Avvio del tirocinio (US-1003): solo con la scheda completa, solo dal Tutor della segnalazione. */
class InternshipStartIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired WorkflowScenarios scenarios;
    @Autowired InternshipTypeRepository internshipTypes;
    @Autowired CompanyAtecoCodeRepository atecoCodes;
    @Autowired CompanyLegalRepresentativeRepository legalRepresentatives;
    @Autowired CompanyAuditEventRepository companyAudit;
    @Autowired TicketRepository tickets;
    @Autowired JobSlotRepository jobSlots;

    private Place place;
    private JobSlot slot;
    private Ticket ticket;
    private String ticketPath;
    private String typeId;
    private String atecoId;

    @BeforeEach
    void setUp() {
        Actors a = scenarios.actors();
        place = scenarios.place();
        Project project = scenarios.project("GOL", a.tutor1(), a.tutor2());
        slot = scenarios.freeSlot(place, "Aiuto cuoco");
        ticket = scenarios.appointmentTicket(a.tutor1(), project, slot, a.operator());
        ticketPath = "/api/v1/tickets/" + TsidMapper.toExternal(ticket.getId());
        typeId = TsidMapper.toExternal(internshipTypes.save(new InternshipType("T_START_" + ticket.getId(),
                "Tipo di avvio " + ticket.getId())).getId());
        atecoId = TsidMapper.toExternal(atecoCodes.save(new CompanyAtecoCode(place.company(), "56.10.11", null)).getId());
        em.flush();
    }

    private void legalRepresentative(boolean complete) {
        CompanyLegalRepresentative rep = new CompanyLegalRepresentative(place.company());
        rep.setFirstName("Laura");
        rep.setLastName("Bianchi");
        if (complete) {
            rep.setFiscalCode("BNCLRA80A41H501D");
            rep.setBirthDate(LocalDate.of(1980, 1, 1));
            rep.setBirthPlace("Roma");
            rep.setResidence("Via Dante 2, Firenze");
        }
        legalRepresentatives.saveAndFlush(rep);
    }

    /** Scheda completa (senza sospensioni né note, facoltative); incompleta = senza codice fiscale né tutor aziendali. */
    private ResultActions saveInternship(boolean complete) throws Exception {
        String json = """
                {"internshipTypeId": "%s", "startDate": "2026-12-01", "expectedEndDate": "2027-05-31",
                 "site": "Via Roma 1, Firenze", "atecoCodeId": "%s",
                 "companyTutors": [{"fullName": "Anna Verdi"}],
                 "fiscalCode": "RSSMRA85T10A562S", "birthDate": "1985-12-10", "birthPlace": "Firenze",
                 "residence": "Via Dante 2, Firenze", "suspensions": [],
                 "firstSlotStart": "09:00", "firstSlotEnd": "13:00", "secondSlotStart": "14:00", "secondSlotEnd": "18:00",
                 "excludeHolidays": true, "schedule": [{"dayOfWeek": 1, "firstSlot": true, "secondSlot": false}],
                 "version": null}""".formatted(typeId, atecoId);
        if (!complete) {
            json = json.replace("\"fiscalCode\": \"RSSMRA85T10A562S\", ", "")
                    .replace("\"companyTutors\": [{\"fullName\": \"Anna Verdi\"}]", "\"companyTutors\": []");
        }
        return mockMvc.perform(put(ticketPath + "/internship").header(USER, "tutor1")
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private ResultActions start(String user, long version) throws Exception {
        return mockMvc.perform(post(ticketPath + "/start").header(USER, user)
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\": %d}".formatted(version)));
    }

    private long ticketVersion() {
        em.flush();
        em.clear();
        return tickets.findById(ticket.getId()).orElseThrow().getVersion();
    }

    @Test
    void schedaIncompleta_elencoDiCioCheMancaEAvvioRifiutato() throws Exception {
        mockMvc.perform(get(ticketPath + "/internship").header(USER, "tutor1"))
                .andExpect(jsonPath("$.missingForStart", contains("internshipType", "startDate", "expectedEndDate",
                        "site", "atecoCode", "companyTutors", "schedule", "fiscalCode", "birthDate", "birthPlace",
                        "residence", "legalRepresentative")));

        legalRepresentative(true);
        saveInternship(false).andExpect(status().isOk())
                .andExpect(jsonPath("$.missingForStart", contains("companyTutors", "fiscalCode")));

        start("tutor1", ticketVersion())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Per avviare il tirocinio la scheda deve essere completa; "
                        + "mancano: almeno un tutor aziendale, il codice fiscale del beneficiario"));
        assertThat(tickets.findById(ticket.getId()).orElseThrow().getStatus()).isEqualTo(TicketStatus.APPUNTAMENTO);
    }

    @Test
    void legaleRappresentanteIncompleto_loCompletaIlCallCenter() throws Exception {
        legalRepresentative(false);
        saveInternship(true)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missingForStart", contains("legalRepresentativeData")));
        start("tutor1", ticketVersion())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString(
                        "codice fiscale, nascita e residenza del legale rappresentante")));
    }

    @Test
    void avvioConLaSchedaCompleta_storicoNotificaEDopoLaSchedaSiCorregge() throws Exception {
        legalRepresentative(true);
        saveInternship(true).andExpect(jsonPath("$.missingForStart").isEmpty());

        start("tutor1", ticketVersion())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_TIROCINIO"));

        em.flush();
        em.clear();
        assertThat(jobSlots.findById(slot.getId()).orElseThrow().getStatus()).isEqualTo(JobSlotStatus.BLOCCATA);
        assertThat(companyAudit.findByCompanyIdOrderByCreatedAtAsc(place.company().getId()))
                .extracting(e -> e.getEventType())
                .containsExactly("TIROCINIO_AVVIATO");
        mockMvc.perform(get(ticketPath + "/history").header(USER, "tutor1"))
                .andExpect(jsonPath("$[*].action", hasItem("START")))
                .andExpect(jsonPath("$[-1].toStatus").value("IN_TIROCINIO"));
        mockMvc.perform(get("/api/v1/notifications").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$.items[0].type").value("INTERNSHIP_STARTED"));
        // la scheda resta modificabile dopo l'avvio (PO, 29/10)
        mockMvc.perform(get(ticketPath + "/internship").header(USER, "tutor1"))
                .andExpect(status().isOk());
    }

    @Test
    void soloIlTutorDellaSegnalazione_versioneEStato() throws Exception {
        legalRepresentative(true);
        saveInternship(true).andExpect(status().isOk());
        long version = ticketVersion();

        start("tutor2", version).andExpect(status().isForbidden());
        start("operatore.cc", version).andExpect(status().isForbidden());
        start("tutor1", version + 1).andExpect(status().isConflict());

        Ticket current = tickets.findById(ticket.getId()).orElseThrow();
        current.setStatus(TicketStatus.PROPOSTA_ACCOLTA);
        em.flush();
        start("tutor1", ticketVersion())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Operazione non ammessa per un ticket in stato PROPOSTA_ACCOLTA"));
    }
}
