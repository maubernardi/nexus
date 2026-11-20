package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

import com.jayway.jsonpath.JsonPath;

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.security.MockHeaderAuthenticationFilter;
import it.nexus.domain.AuditEvent;
import it.nexus.domain.Company;
import it.nexus.domain.CompanyAtecoCode;
import it.nexus.domain.Internship;
import it.nexus.domain.InternshipExtension;
import it.nexus.domain.InternshipType;
import it.nexus.domain.JobSlot;
import it.nexus.domain.Municipality;
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
    @Autowired JdbcTemplate jdbc;

    private Place place;
    private Ticket ticket;
    private String base;
    private String typeId;
    private Municipality firenze;

    @BeforeEach
    void setUp() {
        Actors a = scenarios.actors();
        place = scenarios.place();
        Project project = scenarios.project("GOL", a.tutor1(), a.tutor2());
        JobSlot slot = scenarios.freeSlot(place, "Aiuto cuoco");
        ticket = scenarios.appointmentTicket(a.tutor1(), project, slot, a.operator());
        base = "/api/v1/tickets/" + TsidMapper.toExternal(ticket.getId()) + "/internship";
        InternshipType type = internshipTypes.save(new InternshipType("T_IT_" + ticket.getId(),
                "Tipo di prova " + ticket.getId()));
        typeId = TsidMapper.toExternal(type.getId());
        // il tipo è della segnalazione (US-605): la scheda parte da quello
        ticket.setInternshipType(type);
        firenze = scenarios.municipality("Firenze", "FI");
        em.flush();
    }

    private ResultActions save(String user, String json) throws Exception {
        return mockMvc.perform(put(base).header(USER, user).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    /** Modulo con i campi indicati (JSON senza graffe); liste vuote, fasce precompilate e festività escluse se mancano. */
    private static String form(String fields) {
        String json = "{" + fields + "}";
        Map<String, String> defaults = Map.of("companyTutors", "[]", "suspensions", "[]", "trainingProjects", "[]",
                "extensions", "[]", "schedule", "[]",
                "firstSlotStart", "\"09:00\"", "firstSlotEnd", "\"13:00\"", "secondSlotStart", "\"14:00\"",
                "secondSlotEnd", "\"18:00\"", "excludeHolidays", "true");
        for (Map.Entry<String, String> d : defaults.entrySet()) {
            if (!json.contains("\"" + d.getKey() + "\"")) {
                json = json.replaceFirst("\\{", "{\"" + d.getKey() + "\": " + d.getValue() + ", ");
            }
        }
        return json.replace(", }", "}");
    }

    private String full(Long version) {
        return form("""
                "internshipTypeId": "%s", "startDate": "2026-12-01", "expectedEndDate": "2027-05-31",
                "site": "Via Roma 1", "siteMunicipalityId": "%s",
                "schedule": [{"dayOfWeek": 1, "firstSlot": true, "secondSlot": false}],
                "companyTutors": [{"fullName": "Anna Verdi", "contact": "055 123456"}, {"fullName": "Luca Neri"}],
                "fiscalCode": "rssmra85t10a562s", "birthDate": "1985-12-10", "birthPlace": "Firenze",
                "residenceMunicipalityId": "%s", "residenceStreet": "Via Dante", "residenceStreetNumber": "2",
                "suspensions": [{"from": "2026-12-22", "to": "2027-01-06"}, {"from": "2026-12-08", "to": "2026-12-08"}],
                "notes": "Turni concordati", "version": %s
                """.formatted(typeId, id(firenze), id(firenze), version));
    }

    private static String id(Municipality m) {
        return TsidMapper.toExternal(m.getId());
    }

    private long version() {
        return internships.findByTicketId(ticket.getId()).orElseThrow().getVersion();
    }

    private List<AuditEvent> ticketAudit() {
        return audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.TICKET, ticket.getId());
    }

    @Test
    void tipiAmmessiDalProgetto_soloQuelliOQuelloGiaScelto() throws Exception {
        InternshipType other = internshipTypes.save(new InternshipType("T_OK_" + ticket.getId(),
                "Tipo ammesso " + ticket.getId()));
        String otherId = TsidMapper.toExternal(other.getId());
        // progetto senza limitazioni: tutti i tipi attivi
        mockMvc.perform(get(base).header(USER, "tutor1"))
                .andExpect(jsonPath("$.internshipTypes[?(@.id == '%s')]".formatted(typeId)).exists());

        save("tutor1", form("\"internshipTypeId\": \"%s\", \"version\": null".formatted(typeId)))
                .andExpect(status().isOk());
        ticket.getProject().getAllowedInternshipTypes().add(other);
        em.flush();

        // il tipo già scelto resta valido e proposto, anche se il progetto ora non lo ammette
        mockMvc.perform(get(base).header(USER, "tutor1"))
                .andExpect(jsonPath("$.internshipTypes.length()").value(2))
                .andExpect(jsonPath("$.internshipTypes[?(@.id == '%s')]".formatted(typeId)).exists())
                .andExpect(jsonPath("$.internshipTypes[?(@.id == '%s')]".formatted(otherId)).exists());
        save("tutor1", form("\"internshipTypeId\": \"%s\", \"version\": %d".formatted(typeId, version())))
                .andExpect(status().isOk());
        save("tutor1", form("\"internshipTypeId\": \"%s\", \"version\": %d".formatted(otherId, version())))
                .andExpect(status().isOk());

        // cambiato tipo, quello non ammesso non si può più scegliere
        save("tutor1", form("\"internshipTypeId\": \"%s\", \"version\": %d".formatted(typeId, version())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("internshipTypeId"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value(org.hamcrest.Matchers.containsString(
                        "non è ammesso dal progetto")));
        mockMvc.perform(get(base).header(USER, "tutor1"))
                .andExpect(jsonPath("$.internshipTypes.length()").value(1));
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
        // la scheda parte già con il tipo della segnalazione (US-605): cambia solo la data
        assertThat(event.getChanges()).containsOnlyKeys("startDate");
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
        assertThat(changes.get("schedule")).extracting("after").isEqualTo(List.of("1 F1"));
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

    /** Due progetti formativi dentro il tirocinio di {@link #full}: 1 mese e 15 giorni, poi 1 mese e 13 giorni. */
    private static final String TWO_PROJECTS = """
            [{"from": "2026-12-01", "to": "2027-01-15", "jobTitle": " Aiuto cuoco ",
              "jobDetails": "Preparazione dei piatti freddi"},
             {"from": "2027-01-16", "to": "2027-02-28", "jobTitle": "Cuoco", "jobDetails": "  "}]""";

    /** La scheda con la convenzione e i progetti formativi indicati (US-1008). */
    private static String withAgreementAndProjects(String json, String projects) {
        return json.replace("\"trainingProjects\": [], ", "").replace("\"notes\": \"Turni concordati\",",
                "\"notes\": \"Turni concordati\", \"agreementNumber\": \" 2026/145 \", \"agreementDate\": \"2026-11-20\", "
                        + "\"agreementSignedDate\": \"2026-11-25\", \"trainingProjects\": " + projects + ",");
    }

    @Test
    void convenzioneEProgettiFormativi_numeriMesiMonteOreEAudit() throws Exception {
        save("tutor1", withAgreementAndProjects(full(null), TWO_PROJECTS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agreementNumber").value("2026/145"))
                .andExpect(jsonPath("$.agreementDate").value("2026-11-20"))
                .andExpect(jsonPath("$.agreementSignedDate").value("2026-11-25"))
                .andExpect(jsonPath("$.jobSlotTitle").value("Aiuto cuoco"))
                .andExpect(jsonPath("$.trainingProjects[0].number").value(1))
                .andExpect(jsonPath("$.trainingProjects[0].jobTitle").value("Aiuto cuoco"))
                .andExpect(jsonPath("$.trainingProjects[0].jobDetails").value("Preparazione dei piatti freddi"))
                .andExpect(jsonPath("$.trainingProjects[0].months").value(2))
                // lunedì 7, 14 e 21 dicembre e 11 gennaio: 28 dicembre e 4 gennaio sono nella sospensione
                .andExpect(jsonPath("$.trainingProjects[0].totalMinutes").value(4 * 4 * 60))
                .andExpect(jsonPath("$.trainingProjects[1].number").value(2))
                .andExpect(jsonPath("$.trainingProjects[1].jobDetails").doesNotExist())
                .andExpect(jsonPath("$.trainingProjects[1].months").value(1))
                // sei lunedì dal 18 gennaio al 22 febbraio
                .andExpect(jsonPath("$.trainingProjects[1].totalMinutes").value(6 * 4 * 60));
        Map<String, Object> changes = ticketAudit().getLast().getChanges();
        assertThat(changes.get("agreementNumber")).extracting("after").isEqualTo("2026/145");
        assertThat(changes.get("agreementSignedDate")).extracting("after").isEqualTo("2026-11-25");
        assertThat(changes.get("trainingProjects")).extracting("after")
                .isEqualTo(List.of("1 2026-12-01/2027-01-15 Aiuto cuoco", "2 2027-01-16/2027-02-28 Cuoco"));
        assertThat(changes).containsEntry("trainingProjectDetails", Map.of("redacted", true));

        // si toglie l'ultimo progetto: cambiano solo i progetti
        String one = """
                [{"from": "2026-12-01", "to": "2027-01-15", "jobTitle": "Aiuto cuoco",
                  "jobDetails": "Preparazione dei piatti freddi"}]""";
        save("tutor1", withAgreementAndProjects(full(version()), one))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainingProjects.length()").value(1));
        assertThat(ticketAudit().getLast().getChanges())
                .containsOnlyKeys("trainingProjects", "trainingProjectDetails");

        // stessa scheda: niente audit
        int events = ticketAudit().size();
        save("tutor1", withAgreementAndProjects(full(version()), one)).andExpect(status().isOk());
        assertThat(ticketAudit()).hasSize(events);
    }

    @Test
    void progettiFormativi_dentroIlTirocinioEInOrdine_senzaSovrapposizioni() throws Exception {
        String overlap = """
                [{"from": "2026-12-01", "to": "2027-02-28", "jobTitle": "Aiuto cuoco"},
                 {"from": "2027-02-28", "to": "2027-05-31", "jobTitle": "Cuoco"}]""";
        save("tutor1", withAgreementAndProjects(full(null), overlap))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("trainingProjects[1].from"))
                .andExpect(jsonPath("$.fieldErrors[0].message")
                        .value("Deve iniziare dopo la fine del progetto formativo 1 (28/02/2027)"));
        String reversed = """
                [{"from": "2027-03-01", "to": "2027-05-31", "jobTitle": "Cuoco"},
                 {"from": "2026-12-01", "to": "2027-02-28", "jobTitle": "Aiuto cuoco"}]""";
        save("tutor1", withAgreementAndProjects(full(null), reversed))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("trainingProjects[1].from"));
        save("tutor1", withAgreementAndProjects(full(null),
                        "[{\"from\": \"2026-11-30\", \"to\": \"2027-02-28\", \"jobTitle\": \"Aiuto cuoco\"}]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("trainingProjects[0].from"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Il progetto inizia prima del tirocinio (01/12/2026)"));
        save("tutor1", withAgreementAndProjects(full(null),
                        "[{\"from\": \"2027-02-01\", \"to\": \"2027-01-31\", \"jobTitle\": \"Aiuto cuoco\"}]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("trainingProjects[0].to"));
        save("tutor1", withAgreementAndProjects(full(null),
                        "[{\"from\": \"2026-12-01\", \"to\": \"2027-02-28\", \"jobTitle\": \"  \"}]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("trainingProjects[0].jobTitle"));
        // senza le date del tirocinio i progetti non stanno da nessuna parte
        save("tutor1", form("""
                "trainingProjects": [{"from": "2026-12-01", "to": "2027-02-28", "jobTitle": "Aiuto cuoco"}],
                "version": null"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("startDate"));
        assertThat(internships.findByTicketId(ticket.getId())).isEmpty();

        // tirocinio accorciato con il progetto ancora in corso
        String whole = "[{\"from\": \"2026-12-01\", \"to\": \"2027-05-31\", \"jobTitle\": \"Aiuto cuoco\"}]";
        save("tutor1", withAgreementAndProjects(full(null), whole)).andExpect(status().isOk());
        save("tutor1", withAgreementAndProjects(full(version()), whole)
                        .replace("\"expectedEndDate\": \"2027-05-31\"", "\"expectedEndDate\": \"2027-04-30\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("trainingProjects[0].to"))
                .andExpect(jsonPath("$.fieldErrors[0].message")
                        .value("Il progetto finisce dopo la fine prevista del tirocinio (30/04/2027)"));
    }

    /** Un progetto formativo per tutta la convenzione iniziale di {@link #full}. */
    private static final String WHOLE = "[" + project("2026-12-01", "2027-05-31", "Aiuto cuoco") + "]";

    /** Proroga 1 dal 15 giugno al 30 settembre 2027, con un progetto formativo per tutta la sua durata. */
    private static final String EXTENSION = "[" + extension(" 2027/200 ", "2027-06-15", "2027-09-30", """
            [{"from": "2027-06-15", "to": "2027-09-30", "jobTitle": " Cuoco ", "jobDetails": "Cucina calda"}]""")
            + "]";

    private static String project(String from, String to, String jobTitle) {
        return "{\"from\": \"%s\", \"to\": \"%s\", \"jobTitle\": \"%s\"}".formatted(from, to, jobTitle);
    }

    private static String extension(String number, String from, String to, String projects) {
        return """
                {"agreementNumber": "%s", "agreementDate": "2027-06-01", "agreementSignedDate": "2027-06-10",
                 "from": "%s", "to": "%s", "trainingProjects": %s}""".formatted(number, from, to, projects);
    }

    /** La scheda con le proroghe indicate al posto dell'elenco vuoto (US-1012). */
    private static String withExtensions(String json, String extensions) {
        return json.replace("\"extensions\": [], ", "\"extensions\": " + extensions + ", ");
    }

    /** La scheda di {@link #full} con convenzione, un progetto e le proroghe indicate. */
    private String extended(Long version, String extensions) {
        return withExtensions(withAgreementAndProjects(full(version), WHOLE), extensions);
    }

    private void started() {
        ticket.setStatus(TicketStatus.IN_TIROCINIO);
        em.flush();
    }

    @Test
    void proroghe_soloATirocinioAvviato_conFinePrevistaMonteOreEAudit() throws Exception {
        // prima dell'avvio le proroghe non si registrano
        save("tutor1", extended(null, EXTENSION))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("extensions"))
                .andExpect(jsonPath("$.fieldErrors[0].message")
                        .value("Le proroghe si registrano a tirocinio avviato e non ancora chiuso"));
        String initial = save("tutor1", extended(null, "[]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.extensions.length()").value(0))
                .andExpect(jsonPath("$.currentEndDate").value("2027-05-31"))
                .andExpect(jsonPath("$.extensible").value(false))
                .andReturn().getResponse().getContentAsString();
        int initialMinutes = JsonPath.read(initial, "$.totalMinutes");

        started();
        mockMvc.perform(get(base).header(USER, "tutor1")).andExpect(jsonPath("$.extensible").value(true));
        save("tutor1", extended(version(), EXTENSION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.extensions[0].number").value(1))
                .andExpect(jsonPath("$.extensions[0].agreementNumber").value("2027/200"))
                .andExpect(jsonPath("$.extensions[0].agreementDate").value("2027-06-01"))
                .andExpect(jsonPath("$.extensions[0].agreementSignedDate").value("2027-06-10"))
                .andExpect(jsonPath("$.extensions[0].from").value("2027-06-15"))
                .andExpect(jsonPath("$.extensions[0].to").value("2027-09-30"))
                .andExpect(jsonPath("$.extensions[0].trainingProjects[0].number").value(1))
                .andExpect(jsonPath("$.extensions[0].trainingProjects[0].jobTitle").value("Cuoco"))
                .andExpect(jsonPath("$.extensions[0].trainingProjects[0].jobDetails").value("Cucina calda"))
                // quindici lunedì dal 21 giugno al 27 settembre
                .andExpect(jsonPath("$.extensions[0].trainingProjects[0].totalMinutes").value(15 * 240))
                .andExpect(jsonPath("$.trainingProjects.length()").value(1))
                .andExpect(jsonPath("$.expectedEndDate").value("2027-05-31"))
                .andExpect(jsonPath("$.currentEndDate").value("2027-09-30"))
                // i lunedì 7 e 14 giugno, tra le due convenzioni, non contano
                .andExpect(jsonPath("$.totalMinutes").value(initialMinutes + 15 * 240));
        Map<String, Object> changes = ticketAudit().getLast().getChanges();
        assertThat(changes).containsOnlyKeys("extensions", "extensionTrainingProjects", "trainingProjectDetails");
        assertThat(changes.get("extensions")).extracting("after")
                .isEqualTo(List.of("1 2027/200 2027-06-15/2027-09-30 (data 2027-06-01, stipula 2027-06-10)"));
        assertThat(changes.get("extensionTrainingProjects")).extracting("after")
                .isEqualTo(List.of("1.1 2027-06-15/2027-09-30 Cuoco"));
        assertThat(changes).containsEntry("trainingProjectDetails", Map.of("redacted", true));
        Long internshipId = internships.findByTicketId(ticket.getId()).orElseThrow().getId();
        assertThat(jdbc.queryForObject("""
                SELECT count(*) FROM internship_training_project p
                JOIN internship_extension e ON e.id = p.extension_id WHERE p.internship_id = ?""", Long.class,
                internshipId)).isEqualTo(1);

        // stessa scheda: niente audit
        int events = ticketAudit().size();
        save("tutor1", extended(version(), EXTENSION)).andExpect(status().isOk());
        assertThat(ticketAudit()).hasSize(events);

        // una sospensione dentro la proroga: un lunedì in meno
        save("tutor1", extended(version(), EXTENSION).replace("{\"from\": \"2026-12-08\", \"to\": \"2026-12-08\"}",
                        "{\"from\": \"2026-12-08\", \"to\": \"2026-12-08\"}, {\"from\": \"2027-07-05\", \"to\": \"2027-07-11\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMinutes").value(initialMinutes + 14 * 240));

        // la proroga si toglie: la fine prevista torna quella della convenzione
        save("tutor1", extended(version(), "[]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.extensions.length()").value(0))
                .andExpect(jsonPath("$.currentEndDate").value("2027-05-31"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM internship_extension WHERE internship_id = ?", Long.class,
                internshipId)).isZero();
        assertThat(ticketAudit().getLast().getChanges().get("extensions")).extracting("after").isEqualTo(List.of());
    }

    @Test
    void proroghe_dopoLaConvenzionePrecedente_conIProgettiDentro_eFermeDopoLaChiusura() throws Exception {
        started();
        String noProjects = "[]";
        // il giorno di fine della convenzione è ancora suo
        save("tutor1", extended(null, "[" + extension("2027/200", "2027-05-31", "2027-09-30", noProjects) + "]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("extensions[0].from"))
                .andExpect(jsonPath("$.fieldErrors[0].message")
                        .value("Deve iniziare dopo la fine della convenzione (31/05/2027)"));
        save("tutor1", extended(null, "[" + extension("2027/200", "2027-09-30", "2027-06-15", noProjects) + "]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("extensions[0].to"));
        save("tutor1", extended(null, "[" + extension("  ", "2027-06-15", "2027-09-30", noProjects) + "]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("extensions[0].agreementNumber"));
        save("tutor1", extended(null, "[" + extension("2027/200", "2027-06-15", "2027-09-30", noProjects) + ", "
                        + extension("2027/300", "2027-09-30", "2027-12-31", noProjects) + "]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("extensions[1].from"))
                .andExpect(jsonPath("$.fieldErrors[0].message")
                        .value("Deve iniziare dopo la fine della proroga 1 (30/09/2027)"));
        // progetti della proroga: dentro le sue date e in ordine
        save("tutor1", extended(null, "[" + extension("2027/200", "2027-06-15", "2027-09-30",
                        "[" + project("2027-06-14", "2027-09-30", "Cuoco") + "]") + "]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("extensions[0].trainingProjects[0].from"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Il progetto inizia prima della proroga (15/06/2027)"));
        save("tutor1", extended(null, "[" + extension("2027/200", "2027-06-15", "2027-09-30",
                        "[" + project("2027-06-15", "2027-10-01", "Cuoco") + "]") + "]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("extensions[0].trainingProjects[0].to"))
                .andExpect(jsonPath("$.fieldErrors[0].message")
                        .value("Il progetto finisce dopo la fine della proroga (30/09/2027)"));
        save("tutor1", extended(null, "[" + extension("2027/200", "2027-06-15", "2027-09-30",
                        "[" + project("2027-06-15", "2027-07-31", "Cuoco") + ", "
                                + project("2027-07-31", "2027-09-30", "Capo partita") + "]") + "]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("extensions[0].trainingProjects[1].from"))
                .andExpect(jsonPath("$.fieldErrors[0].message")
                        .value("Deve iniziare dopo la fine del progetto formativo 1 (31/07/2027)"));
        // senza la fine prevista le proroghe non hanno un punto di partenza
        save("tutor1", form("\"trainingProjects\": [], \"extensions\": " + EXTENSION + ", \"version\": null"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("expectedEndDate"));
        assertThat(internships.findByTicketId(ticket.getId())).isEmpty();

        // due proroghe, la seconda non il giorno dopo la prima: vanno bene
        String two = "[" + extension("2027/200", "2027-06-15", "2027-09-30", "[" + project("2027-06-15", "2027-07-31",
                "Cuoco") + ", " + project("2027-08-01", "2027-09-30", "Capo partita") + "]") + ", "
                + extension("2027/300", "2027-10-11", "2027-12-31", "[" + project("2027-10-11", "2027-12-31",
                "Chef") + "]") + "]";
        save("tutor1", extended(null, two))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.extensions[0].trainingProjects[1].number").value(2))
                .andExpect(jsonPath("$.extensions[1].number").value(2))
                .andExpect(jsonPath("$.extensions[1].agreementNumber").value("2027/300"))
                .andExpect(jsonPath("$.extensions[1].trainingProjects[0].number").value(1))
                .andExpect(jsonPath("$.currentEndDate").value("2027-12-31"));
        assertThat(ticketAudit().getLast().getChanges().get("extensionTrainingProjects")).extracting("after")
                .isEqualTo(List.of("1.1 2027-06-15/2027-07-31 Cuoco", "1.2 2027-08-01/2027-09-30 Capo partita",
                        "2.1 2027-10-11/2027-12-31 Chef"));

        // la convenzione iniziale allungata sopra la proroga: l'errore è sulla fine prevista
        save("tutor1", extended(version(), two).replace("\"expectedEndDate\": \"2027-05-31\"",
                        "\"expectedEndDate\": \"2027-06-20\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("expectedEndDate"))
                .andExpect(jsonPath("$.fieldErrors[0].message")
                        .value("Deve finire prima dell'inizio della proroga 1 (15/06/2027)"));

        // dopo la chiusura la scheda si corregge ancora, ma le proroghe restano quelle
        ticket.setStatus(TicketStatus.FORM_RESTITUZIONE);
        em.flush();
        mockMvc.perform(get(base).header(USER, "tutor1")).andExpect(jsonPath("$.extensible").value(false));
        save("tutor1", extended(version(), two).replace("Turni concordati", "Turni rivisti"))
                .andExpect(status().isOk());
        save("tutor1", extended(version(), "[]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("extensions"));
    }

    /** Lunedì 7 – domenica 13 dicembre 2026; martedì 8 è festivo. */
    private static String week(String schedule, boolean excludeHolidays, String suspensions, Long version) {
        return form("""
                "startDate": "2026-12-07", "expectedEndDate": "2026-12-13", "schedule": %s,
                "excludeHolidays": %s, "suspensions": %s, "version": %s"""
                .formatted(schedule, excludeHolidays, suspensions, version));
    }

    @Test
    void grigliaOraria_monteOreConFestivitaESospensioni_ricalcolato() throws Exception {
        // lunedì prima fascia (4 ore), martedì entrambe ma 3 ore (esempio del PO)
        String schedule = """
                [{"dayOfWeek": 2, "firstSlot": true, "secondSlot": true, "minutes": 180},
                 {"dayOfWeek": 1, "firstSlot": true, "secondSlot": false}]""";
        mockMvc.perform(get(base).header(USER, "tutor1"))
                .andExpect(jsonPath("$.firstSlotStart").value("09:00:00"))
                .andExpect(jsonPath("$.secondSlotEnd").value("18:00:00"))
                .andExpect(jsonPath("$.excludeHolidays").value(true))
                .andExpect(jsonPath("$.weeklyMinutes").doesNotExist())
                .andExpect(jsonPath("$.totalMinutes").doesNotExist());

        save("tutor1", week(schedule, true, "[]", null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schedule[0].dayOfWeek").value(1))
                .andExpect(jsonPath("$.schedule[1].minutes").value(180))
                .andExpect(jsonPath("$.weeklyMinutes").value(420))
                // martedì 8 dicembre festivo: solo il lunedì
                .andExpect(jsonPath("$.totalMinutes").value(240));
        assertThat(ticketAudit().getLast().getChanges().get("schedule")).extracting("after")
                .isEqualTo(List.of("1 F1", "2 F1+F2 180'"));

        save("tutor1", week(schedule, false, "[]", version()))
                .andExpect(jsonPath("$.totalMinutes").value(420));
        // ricalcolo: una sospensione il lunedì, poi la prima fascia spostata a 8:30–12:00
        save("tutor1", week(schedule, false, "[{\"from\": \"2026-12-07\", \"to\": \"2026-12-07\"}]", version()))
                .andExpect(jsonPath("$.totalMinutes").value(180));
        save("tutor1", week(schedule, false, "[]", version()).replace("\"09:00\"", "\"08:30\"")
                        .replace("\"13:00\"", "\"12:00\""))
                .andExpect(jsonPath("$.firstSlotStart").value("08:30:00"))
                .andExpect(jsonPath("$.weeklyMinutes").value(390));

        // riletto dal database l'orario resta quello scritto, senza conversioni di fuso
        em.flush();
        em.clear();
        mockMvc.perform(get(base).header(USER, "tutor1"))
                .andExpect(jsonPath("$.firstSlotStart").value("08:30:00"))
                .andExpect(jsonPath("$.secondSlotEnd").value("18:00:00"));
        assertThat(jdbc.queryForObject("SELECT first_slot_start FROM internship WHERE ticket_id = ?", Integer.class,
                ticket.getId())).isEqualTo(510);
    }

    @Test
    void grigliaOraria_controlli() throws Exception {
        save("tutor1", form("\"schedule\": [{\"dayOfWeek\": 1, \"firstSlot\": true, \"secondSlot\": false, "
                + "\"minutes\": 300}], \"version\": null"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("schedule[0].minutes"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Al massimo 4 ore nelle fasce scelte"));
        save("tutor1", form("\"schedule\": [{\"dayOfWeek\": 3, \"firstSlot\": false, \"secondSlot\": false}], "
                + "\"version\": null"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("schedule[0]"));
        save("tutor1", form("\"schedule\": [{\"dayOfWeek\": 1, \"firstSlot\": true, \"secondSlot\": false}, "
                + "{\"dayOfWeek\": 1, \"firstSlot\": false, \"secondSlot\": true}], \"version\": null"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("schedule"));
        save("tutor1", form("\"secondSlotStart\": \"12:00\", \"version\": null"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("secondSlotStart"));
        // cinque giorni pieni: 40 ore ammesse, il sesto le supera
        String full = "{\"dayOfWeek\": %d, \"firstSlot\": true, \"secondSlot\": true}";
        String five = String.join(", ", java.util.stream.IntStream.rangeClosed(1, 5)
                .mapToObj(full::formatted).toList());
        save("tutor1", form("\"schedule\": [%s, %s], \"version\": null".formatted(five, full.formatted(6))))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("schedule"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Il totale settimanale (48 ore) supera le 40 ore"));
        save("tutor1", form("\"schedule\": [%s], \"version\": null".formatted(five))).andExpect(status().isOk());
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
        save("tutor1", form("\"site\": \"Via Roma 1\", \"version\": null")).andExpect(status().isOk());

        ticket.setStatus(TicketStatus.IN_TIROCINIO);
        em.flush();
        save("tutor1", form("\"site\": \"Via Roma 2\", \"version\": %d".formatted(version())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.site").value("Via Roma 2"));
        mockMvc.perform(post(base + "/ateco-codes").header(USER, "tutor1")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"code\": \"56.10\"}"))
                .andExpect(status().isCreated());

        ticket.setStatus(TicketStatus.FORM_RESTITUZIONE);
        em.flush();
        save("tutor1", form("\"site\": \"Via Roma 3\", \"version\": %d".formatted(version())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.site").value("Via Roma 3"));
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
    void sedeEResidenzaConIlComune_auditEVersioniDelComune() throws Exception {
        save("tutor1", full(null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.site").value("Via Roma 1"))
                .andExpect(jsonPath("$.siteMunicipality.name").value("Firenze"))
                .andExpect(jsonPath("$.siteMunicipality.provinceAbbreviation").value("FI"))
                .andExpect(jsonPath("$.residenceMunicipality.id").value(id(firenze)))
                .andExpect(jsonPath("$.residenceStreet").value("Via Dante"))
                .andExpect(jsonPath("$.residenceStreetNumber").value("2"));
        Map<String, Object> created = ticketAudit().getLast().getChanges();
        assertThat(created.get("siteMunicipality")).extracting("after").isEqualTo("Firenze (FI)");
        assertThat(created).containsEntry("residence", Map.of("redacted", true));

        // solo il numero civico cambia: la residenza risulta cambiata, senza valori
        save("tutor1", full(version()).replace("\"residenceStreetNumber\": \"2\"", "\"residenceStreetNumber\": \"2/A\""))
                .andExpect(status().isOk());
        assertThat(ticketAudit().getLast().getChanges()).containsOnlyKeys("residence");

        // la versione salvata resta anche se nel frattempo è stata chiusa
        firenze.close(LocalDate.of(2026, 6, 1));
        em.flush();
        save("tutor1", full(version())).andExpect(status().isOk());

        // una versione chiusa non si sceglie di nuovo, un id sconosciuto nemmeno
        Municipality prato = scenarios.municipality("Prato", "PO");
        prato.close(LocalDate.of(2026, 6, 1));
        em.flush();
        save("tutor1", full(version()).replaceFirst(id(firenze), id(prato)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("siteMunicipalityId"))
                .andExpect(jsonPath("$.fieldErrors[0].message")
                        .value("Il comune Prato non è più valido: sceglilo di nuovo dall'elenco"));
        save("tutor1", full(version()).replace("\"residenceMunicipalityId\": \"" + id(firenze),
                        "\"residenceMunicipalityId\": \"0ABCDEFGHJKMN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("residenceMunicipalityId"));
    }

    @Test
    void ruoloDeiTutorAziendali_facoltativo_auditSenzaValori() throws Exception {
        String withRoles = full(null).replace("{\"fullName\": \"Anna Verdi\", \"contact\": \"055 123456\"}, "
                        + "{\"fullName\": \"Luca Neri\"}",
                "{\"fullName\": \"Anna Verdi\", \"role\": \" Responsabile di sala \", \"contact\": \"055 123456\"}, "
                        + "{\"fullName\": \"Luca Neri\", \"role\": \"   \"}");
        assertThat(withRoles).contains("Responsabile di sala");
        save("tutor1", withRoles)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyTutors[0].role").value("Responsabile di sala"))
                // spazi = ruolo non indicato; il tutor conta comunque per l'avvio
                .andExpect(jsonPath("$.companyTutors[1].role").doesNotExist())
                .andExpect(jsonPath("$.missingForStart", not(hasItem("companyTutors"))));

        // cambia solo il ruolo: i tutor risultano cambiati, senza valori
        save("tutor1", withRoles.replace("\"version\": null", "\"version\": " + version())
                        .replace(" Responsabile di sala ", "Capo sala"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyTutors[0].role").value("Capo sala"));
        Map<String, Object> changes = ticketAudit().getLast().getChanges();
        assertThat(changes).containsOnlyKeys("companyTutors").containsEntry("companyTutors", Map.of("redacted", true));

        save("tutor1", withRoles.replace("\"version\": null", "\"version\": " + version())
                        .replace(" Responsabile di sala ", "x".repeat(201)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("companyTutors[0].role"));
    }

    @Test
    void appuntamentoFallito_restanoSoloIDatiDelBeneficiario() throws Exception {
        save("tutor1", withAgreementAndProjects(full(null), TWO_PROJECTS)).andExpect(status().isOk());
        // anche una proroga con il suo progetto (US-1012), scritta direttamente: dal modulo serve il tirocinio avviato
        Internship withExtension = internships.findByTicketId(ticket.getId()).orElseThrow();
        InternshipExtension extension = withExtension.addExtension("2027/200", LocalDate.of(2027, 6, 1),
                LocalDate.of(2027, 6, 10), LocalDate.of(2027, 6, 15), LocalDate.of(2027, 9, 30));
        em.flush();
        withExtension.addTrainingProject(extension, LocalDate.of(2027, 6, 15), LocalDate.of(2027, 9, 30), "Cuoco", null);
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
        assertThat(left.getResidenceStreet()).isEqualTo("Via Dante");
        assertThat(left.getResidenceStreetNumber()).isEqualTo("2");
        assertThat(left.getResidenceMunicipality().getId()).isEqualTo(firenze.getId());
        assertThat(left.getSiteMunicipality()).isNull();
        // il tipo è della segnalazione e resta (US-605)
        assertThat(left.getInternshipType()).isNotNull();
        assertThat(left.getStartDate()).isNull();
        assertThat(left.getSite()).isNull();
        assertThat(left.getNotes()).isNull();
        assertThat(left.getCompanyTutors()).isEmpty();
        assertThat(left.getSuspensions()).isEmpty();
        assertThat(left.getScheduleDays()).isEmpty();
        // convenzione e progetti formativi riguardavano quell'azienda (US-1008)
        assertThat(left.getAgreementNumber()).isNull();
        assertThat(left.getAgreementDate()).isNull();
        assertThat(left.getAgreementSignedDate()).isNull();
        assertThat(left.getTrainingProjects()).isEmpty();
        assertThat(left.getExtensions()).isEmpty();
        assertThat(left.isExcludeHolidays()).isTrue();
        assertThat(ticketAudit().getLast().getChanges().get("internshipReset")).extracting("after").isEqualTo(true);
    }
}
