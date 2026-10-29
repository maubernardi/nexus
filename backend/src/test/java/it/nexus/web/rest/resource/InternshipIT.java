package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

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
import it.nexus.domain.AuditEvent;
import it.nexus.domain.Company;
import it.nexus.domain.CompanyAtecoCode;
import it.nexus.domain.Internship;
import it.nexus.domain.InternshipType;
import it.nexus.domain.JobSlot;
import it.nexus.domain.Project;
import it.nexus.domain.Ticket;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AuditEventRepository;
import it.nexus.repository.CompanyAtecoCodeRepository;
import it.nexus.repository.InternshipRepository;
import it.nexus.repository.InternshipTypeRepository;
import it.nexus.support.WorkflowScenarios;
import it.nexus.support.WorkflowScenarios.Actors;
import it.nexus.support.WorkflowScenarios.Place;
import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, WorkflowScenarios.class})
/** Scheda amministrativa del tirocinio (US-1001): solo il Tutor della segnalazione, dall'appuntamento fissato. */
class InternshipIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired WorkflowScenarios scenarios;
    @Autowired AuditEventRepository audit;
    @Autowired InternshipRepository internships;
    @Autowired InternshipTypeRepository internshipTypes;
    @Autowired CompanyAtecoCodeRepository atecoCodes;

    private Place place;
    private Ticket ticket;
    private String base;
    private String typeId;

    @BeforeEach
    void setUp() {
        Actors a = scenarios.actors();
        place = scenarios.place();
        Project project = scenarios.project("GOL", a.tutor1(), a.tutor2());
        JobSlot slot = scenarios.freeSlot(place, "Aiuto cuoco");
        ticket = scenarios.appointmentTicket(a.tutor1(), project, slot, a.operator());
        base = "/api/v1/tickets/" + TsidMapper.toExternal(ticket.getId()) + "/internship";
        typeId = TsidMapper.toExternal(internshipTypes.save(new InternshipType("T_IT_" + ticket.getId(),
                "Tipo di prova " + ticket.getId())).getId());
        em.flush();
    }

    private ResultActions save(String user, String json) throws Exception {
        return mockMvc.perform(put(base).header(USER, user).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    /** Modulo con i campi indicati (JSON senza graffe); liste vuote se non indicate. */
    private static String form(String fields) {
        String json = "{" + fields + "}";
        if (!json.contains("\"companyTutors\"")) {
            json = json.replaceFirst("\\{", "{\"companyTutors\": [], ");
        }
        if (!json.contains("\"suspensions\"")) {
            json = json.replaceFirst("\\{", "{\"suspensions\": [], ");
        }
        return json.replace(", }", "}");
    }

    private String full(Long version) {
        return form("""
                "internshipTypeId": "%s", "startDate": "2026-12-01", "expectedEndDate": "2027-05-31",
                "weeklyHours": 30, "site": "Via Roma 1, Firenze",
                "companyTutors": [{"fullName": "Anna Verdi", "contact": "055 123456"}, {"fullName": "Luca Neri"}],
                "fiscalCode": "rssmra85t10a562s", "birthDate": "1985-12-10", "birthPlace": "Firenze",
                "residence": "Via Dante 2, Firenze",
                "suspensions": [{"from": "2026-12-22", "to": "2027-01-06"}, {"from": "2026-12-08", "to": "2026-12-08"}],
                "notes": "Turni concordati", "version": %s
                """.formatted(typeId, version));
    }

    private long version() {
        return internships.findByTicketId(ticket.getId()).orElseThrow().getVersion();
    }

    private List<AuditEvent> ticketAudit() {
        return audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.TICKET, ticket.getId());
    }

    @Test
    void schedaVuota_salvataggioParziale_rilettura_auditECronologia() throws Exception {
        mockMvc.perform(get(base).header(USER, "tutor1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").doesNotExist())
                .andExpect(jsonPath("$.companyName").value(place.company().getName()))
                .andExpect(jsonPath("$.atecoCodes").isEmpty())
                .andExpect(jsonPath("$.companyTutors").isEmpty());

        save("tutor1", form("\"internshipTypeId\": \"%s\", \"startDate\": \"2026-12-01\", \"version\": null"
                .formatted(typeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(0))
                .andExpect(jsonPath("$.internshipType.id").value(typeId))
                .andExpect(jsonPath("$.startDate").value("2026-12-01"))
                .andExpect(jsonPath("$.expectedEndDate").doesNotExist());
        mockMvc.perform(get(base).header(USER, "tutor1"))
                .andExpect(jsonPath("$.internshipType.name").value("Tipo di prova " + ticket.getId()));

        AuditEvent event = ticketAudit().getLast();
        assertThat(event.getAction()).isEqualTo("INTERNSHIP_SAVE");
        assertThat(event.getChanges()).containsOnlyKeys("internshipType", "startDate");
        assertThat(event.getChanges().get("startDate")).extracting("after").isEqualTo("2026-12-01");
        mockMvc.perform(get("/api/v1/tickets/" + TsidMapper.toExternal(ticket.getId()) + "/history")
                        .header(USER, "operatore.cc"))
                .andExpect(jsonPath("$[*].action", hasItem("INTERNSHIP_SAVE")));
    }

    @Test
    void salvataggioCompleto_righeFiglie_versione_auditSenzaDatiPersonali() throws Exception {
        save("tutor1", full(null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fiscalCode").value("RSSMRA85T10A562S"))
                .andExpect(jsonPath("$.companyTutors[1].fullName").value("Luca Neri"))
                .andExpect(jsonPath("$.companyTutors[1].contact").doesNotExist())
                // sospensioni in ordine di data
                .andExpect(jsonPath("$.suspensions[0].from").value("2026-12-08"))
                .andExpect(jsonPath("$.suspensions[1].to").value("2027-01-06"));
        // dati del tirocinio con i valori, dati personali solo come "cambiato"
        Map<String, Object> changes = ticketAudit().getLast().getChanges();
        assertThat(changes)
                .containsEntry("fiscalCode", Map.of("redacted", true))
                .containsEntry("residence", Map.of("redacted", true))
                .containsEntry("companyTutors", Map.of("redacted", true))
                .containsEntry("notes", Map.of("redacted", true));
        assertThat(changes.get("weeklyHours")).extracting("after").isEqualTo(30);
        assertThat(changes.get("suspensions")).extracting("after")
                .isEqualTo(List.of("2026-12-08/2026-12-08", "2026-12-22/2027-01-06"));

        // si sposta un periodo sopra la vecchia posizione dell'altro: le righe vecchie se ne vanno prima
        long v0 = version();
        String moved = full(v0).replace("{\"from\": \"2026-12-08\", \"to\": \"2026-12-08\"}",
                "{\"from\": \"2026-12-07\", \"to\": \"2026-12-08\"}")
                .replace("\"2026-12-22\", \"to\": \"2027-01-06\"", "\"2026-12-23\", \"to\": \"2027-01-07\"");
        String body = save("tutor1", moved)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suspensions[1].from").value("2026-12-23"))
                .andReturn().getResponse().getContentAsString();
        long v1 = ((Number) JsonPath.read(body, "$.version")).longValue();
        assertThat(v1).isGreaterThan(v0);
        assertThat(ticketAudit().getLast().getChanges()).containsOnlyKeys("suspensions");

        // versione superata o assente con la scheda già salvata
        save("tutor1", full(v0)).andExpect(status().isConflict());
        save("tutor1", full(null)).andExpect(status().isConflict());
        // stessa scheda, nessuna modifica: niente audit
        int events = ticketAudit().size();
        save("tutor1", moved.replace("\"version\": " + v0, "\"version\": " + v1)).andExpect(status().isOk());
        assertThat(ticketAudit()).hasSize(events);
    }

    @Test
    void controlliSuiDati() throws Exception {
        save("tutor1", form("\"startDate\": \"2027-01-10\", \"expectedEndDate\": \"2027-01-09\", \"version\": null"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("expectedEndDate"));
        save("tutor1", form("\"weeklyHours\": 41, \"version\": null"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("weeklyHours"));
        save("tutor1", form("\"companyTutors\": [{\"fullName\": \" \"}], \"version\": null"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("companyTutors[0].fullName"));
        save("tutor1", form("""
                "suspensions": [{"from": "2026-12-22", "to": "2027-01-06"}, {"from": "2027-01-06", "to": "2027-01-10"}],
                "version": null"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("suspensions[1].from"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Si sovrappone al periodo dal 22/12/2026 al 6/1/2027"));
        save("tutor1", form("""
                "startDate": "2027-01-01", "suspensions": [{"from": "2026-12-22", "to": "2027-01-06"}], "version": null"""))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("suspensions[0].from"));
        save("tutor1", form("\"suspensions\": [{\"from\": \"2027-01-06\", \"to\": \"2027-01-05\"}], \"version\": null"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("suspensions[0].to"));
        save("tutor1", form("\"fiscalCode\": \"RSSMRA85T10A562S\", \"birthDate\": \"1985-12-11\", \"version\": null"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("fiscalCode"))
                .andExpect(jsonPath("$.fieldErrors[0].message")
                        .value("Il codice fiscale non corrisponde alla data di nascita"));
        save("tutor1", form("\"fiscalCode\": \"RSSMRA85T10A562T\", \"version\": null"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("fiscalCode"));

        // ATECO di un'altra azienda e tipo disattivato
        Company other = scenarios.place().company();
        CompanyAtecoCode foreign = atecoCodes.save(new CompanyAtecoCode(other, "10.71", null));
        InternshipType inactive = internshipTypes.save(new InternshipType("T_OFF_" + ticket.getId(), "Spento"));
        inactive.setActive(false);
        em.flush();
        save("tutor1", form("\"atecoCodeId\": \"%s\", \"version\": null".formatted(TsidMapper.toExternal(foreign.getId()))))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("atecoCodeId"));
        save("tutor1", form("\"internshipTypeId\": \"%s\", \"version\": null"
                .formatted(TsidMapper.toExternal(inactive.getId()))))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("internshipTypeId"));

        assertThat(internships.findByTicketId(ticket.getId())).isEmpty();
    }

    @Test
    void soloIlTutorDellaSegnalazione_eDallAppuntamentoInPoi() throws Exception {
        mockMvc.perform(get(base).header(USER, "tutor2")).andExpect(status().isForbidden());
        mockMvc.perform(get(base).header(USER, "operatore.cc")).andExpect(status().isForbidden());
        mockMvc.perform(get(base).header(USER, "admin")).andExpect(status().isForbidden());
        save("tutor2", form("\"version\": null")).andExpect(status().isForbidden());

        ticket.setStatus(TicketStatus.PROPOSTA_ACCOLTA);
        em.flush();
        mockMvc.perform(get(base).header(USER, "tutor1")).andExpect(status().isConflict());
        save("tutor1", form("\"version\": null")).andExpect(status().isConflict());
    }

    @Test
    void ilTutorLaModificaInQualunqueMomento_ancheATirocinioAvviatoOConcluso() throws Exception {
        save("tutor1", form("\"weeklyHours\": 20, \"version\": null")).andExpect(status().isOk());

        ticket.setStatus(TicketStatus.IN_TIROCINIO);
        em.flush();
        save("tutor1", form("\"weeklyHours\": 25, \"version\": %d".formatted(version())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weeklyHours").value(25));
        mockMvc.perform(post(base + "/ateco-codes").header(USER, "tutor1")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"code\": \"56.10\"}"))
                .andExpect(status().isCreated());

        ticket.setStatus(TicketStatus.FORM_RESTITUZIONE);
        em.flush();
        save("tutor1", form("\"weeklyHours\": 30, \"version\": %d".formatted(version())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weeklyHours").value(30));
    }

    @Test
    void datiDellAzienda_legaleRappresentanteSoloSeMancaEAteco() throws Exception {
        String rep = """
                {"firstName": "Laura", "lastName": "Bianchi", "fiscalCode": "BNCLRA80A41H501D", "version": 7}""";
        mockMvc.perform(put(base + "/legal-representative").header(USER, "tutor1")
                        .contentType(MediaType.APPLICATION_JSON).content(rep))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(0));
        mockMvc.perform(put(base + "/legal-representative").header(USER, "tutor1")
                        .contentType(MediaType.APPLICATION_JSON).content(rep))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Il legale rappresentante di " + place.company().getName()
                        + " è già indicato: per correggerlo rivolgiti al Call Center"));
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.COMPANY,
                place.company().getId()).getFirst().getCreatedBy()).isEqualTo("tutor1");

        String atecoId = JsonPath.read(mockMvc.perform(post(base + "/ateco-codes").header(USER, "tutor1")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"code\": \"56.10.11\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id");
        mockMvc.perform(get(base).header(USER, "tutor1"))
                .andExpect(jsonPath("$.legalRepresentative.lastName").value("Bianchi"))
                .andExpect(jsonPath("$.atecoCodes[0].code").value("56.10.11"));

        save("tutor1", form("\"atecoCodeId\": \"%s\", \"version\": null".formatted(atecoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.atecoCodeId").value(atecoId));
        // il codice scelto non si toglie dall'azienda
        mockMvc.perform(delete("/api/v1/companies/" + TsidMapper.toExternal(place.company().getId())
                        + "/ateco-codes/" + atecoId).header(USER, "operatore.cc"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Il codice 56.10.11 è scelto nella scheda del tirocinio della "
                        + "segnalazione n. " + ticket.getNumber()));
    }

    @Test
    void appuntamentoFallito_restanoSoloIDatiDelBeneficiario() throws Exception {
        save("tutor1", full(null)).andExpect(status().isOk());
        em.flush();
        em.clear();

        mockMvc.perform(post("/api/v1/tickets/" + TsidMapper.toExternal(ticket.getId()) + "/appointment-failed")
                        .header(USER, "tutor1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\": %d, \"cause\": \"BENEFICIARIO_RINUNCIA\"}".formatted(
                                em.find(Ticket.class, ticket.getId()).getVersion())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RIAPERTO"));
        em.flush();
        em.clear();

        Internship left = internships.findByTicketId(ticket.getId()).orElseThrow();
        assertThat(left.getFiscalCode()).isEqualTo("RSSMRA85T10A562S");
        assertThat(left.getBirthPlace()).isEqualTo("Firenze");
        assertThat(left.getResidence()).isEqualTo("Via Dante 2, Firenze");
        assertThat(left.getInternshipType()).isNull();
        assertThat(left.getStartDate()).isNull();
        assertThat(left.getSite()).isNull();
        assertThat(left.getNotes()).isNull();
        assertThat(left.getCompanyTutors()).isEmpty();
        assertThat(left.getSuspensions()).isEmpty();
        assertThat(ticketAudit().getLast().getChanges().get("internshipReset")).extracting("after").isEqualTo(true);
    }
}
