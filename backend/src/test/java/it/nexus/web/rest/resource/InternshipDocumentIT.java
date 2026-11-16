package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.security.MockHeaderAuthenticationFilter;
import it.nexus.domain.ClosureReport;
import it.nexus.domain.CompanyAtecoCode;
import it.nexus.domain.CompanyLegalRepresentative;
import it.nexus.domain.InternshipType;
import it.nexus.domain.JobSlot;
import it.nexus.domain.Municipality;
import it.nexus.domain.Project;
import it.nexus.domain.Ticket;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.EducationLevel;
import it.nexus.domain.enumeration.Gender;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AuditEventRepository;
import it.nexus.repository.CompanyAtecoCodeRepository;
import it.nexus.repository.CareAgencyRepository;
import it.nexus.repository.ClosureReasonRepository;
import it.nexus.repository.ClosureReportRepository;
import it.nexus.repository.CompetenceSiteRepository;
import it.nexus.repository.NoHireReasonRepository;
import it.nexus.repository.CompanyLegalRepresentativeRepository;
import it.nexus.repository.InternshipTypeRepository;
import it.nexus.services.documents.DocxTemplate;
import it.nexus.services.impl.InternshipDocumentServiceImpl;
import it.nexus.support.TestDocx;
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
 * Documenti in Word (US-1004, US-1005, US-1011): compilati dalla scheda, solo per il Tutor della segnalazione; uno per
 * convenzione e per progetto formativo, la relazione finale a tirocinio chiuso.
 */
class InternshipDocumentIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;
    private static final String DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired WorkflowScenarios scenarios;
    @Autowired InternshipTypeRepository internshipTypes;
    @Autowired CompanyAtecoCodeRepository atecoCodes;
    @Autowired CompanyLegalRepresentativeRepository legalRepresentatives;
    @Autowired AuditEventRepository audit;
    @Autowired CareAgencyRepository careAgencies;
    @Autowired JdbcTemplate jdbc;
    @Autowired ClosureReportRepository closureReports;
    @Autowired ClosureReasonRepository closureReasons;
    @Autowired NoHireReasonRepository noHireReasons;
    @Autowired CompetenceSiteRepository competenceSites;

    private Place place;
    private Ticket ticket;
    private String internshipPath;
    private String atecoId;
    private Municipality sesto;
    private Project project;

    @BeforeEach
    void setUp() {
        Actors a = scenarios.actors();
        place = scenarios.place();
        place.company().setLegalAddress("Via Garibaldi 3");
        place.company().setLegalMunicipality(scenarios.municipality("Firenze", "FI"));
        sesto = scenarios.municipality("Sesto Fiorentino", "FI");
        project = scenarios.project("GOL", a.tutor1(), a.tutor2());
        project.setHeaderLine("Progetto di prova – Codice 1 – CUP X1");
        JobSlot slot = scenarios.freeSlot(place, "Aiuto cuoco");
        ticket = scenarios.appointmentTicket(a.tutor1(), project, slot, a.operator());
        internshipPath = "/api/v1/tickets/" + TsidMapper.toExternal(ticket.getId()) + "/internship";
        atecoId = TsidMapper.toExternal(atecoCodes.save(new CompanyAtecoCode(place.company(), "56.10.11",
                "Ristorazione con somministrazione")).getId());
        CompanyLegalRepresentative rep = new CompanyLegalRepresentative(place.company());
        rep.setFirstName("Laura");
        rep.setLastName("Bianchi");
        rep.setFiscalCode("BNCLRA80A41H501D");
        rep.setBirthDate(LocalDate.of(1980, 1, 1));
        rep.setBirthPlace("Roma");
        rep.setResidence("Via Dante 2, Firenze");
        legalRepresentatives.save(rep);
        em.flush();
    }

    private String typeId(String code) {
        return internshipTypes.findByActiveTrueOrderByNameAsc().stream()
                .filter(t -> t.getCode().equals(code))
                .map(t -> TsidMapper.toExternal(t.getId()))
                .findFirst()
                .orElseThrow();
    }

    /** Salva la scheda e restituisce il monte ore calcolato, come lo scrive il documento. */
    private String save(String typeId, boolean complete) throws Exception {
        String json = """
                {"internshipTypeId": "%s", "startDate": "2026-12-01", "expectedEndDate": "2027-05-31",
                 "site": "Via Roma 1", "siteMunicipalityId": "%s", "atecoCodeId": "%s",
                 "companyTutors": [{"fullName": "Anna Verdi", "role": "Responsabile di sala", "contact": "055 123456"}],
                 "fiscalCode": "RSSMRA85T10A562S", "birthDate": "1985-12-10", "birthPlace": "Firenze",
                 "residenceMunicipalityId": "%s", "residenceStreet": "Via Gramsci", "residenceStreetNumber": "12",
                 "suspensions": [{"from": "2026-12-22", "to": "2027-01-06"}], "trainingProjects": [], "extensions": [],
                 "agreementNumber": "2026/145", "agreementDate": "2026-11-20", "agreementSignedDate": "2026-11-25",
                 "firstSlotStart": "09:00", "firstSlotEnd": "13:00", "secondSlotStart": "14:00", "secondSlotEnd": "18:00",
                 "excludeHolidays": true,
                 "schedule": [{"dayOfWeek": 1, "firstSlot": true, "secondSlot": false},
                              {"dayOfWeek": 3, "firstSlot": true, "secondSlot": true, "minutes": 360}],
                 "version": null}""".formatted(typeId, TsidMapper.toExternal(sesto.getId()), atecoId,
                TsidMapper.toExternal(sesto.getId()));
        if (!complete) {
            json = json.replace("\"fiscalCode\": \"RSSMRA85T10A562S\", ", "")
                    .replace("[{\"fullName\": \"Anna Verdi\", \"role\": \"Responsabile di sala\", "
                            + "\"contact\": \"055 123456\"}]", "[]");
        }
        String body = mockMvc.perform(put(internshipPath).header(USER, "tutor1")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        int minutes = JsonPath.read(body, "$.totalMinutes");
        return minutes % 60 == 0 ? String.valueOf(minutes / 60) : String.valueOf(minutes / 60.0).replace('.', ',');
    }

    private static String text(MvcResult result) throws IOException {
        return DocxTemplate.text(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
    }

    @Test
    void convenzioneCompilata_modelloDelTipo_auditECronologia() throws Exception {
        String totalHours = save(typeId("CURRICULARE"), true);

        MvcResult result = mockMvc.perform(get(internshipPath + "/documents/convenzione").header(USER, "tutor1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", DOCX))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"Convenzione-segnalazione-%d.docx\"".formatted(ticket.getNumber())))
                .andReturn();

        String text = text(result);
        assertThat(text)
                .contains("CONVENZIONE DI TIROCINIO CURRICULARE")
                .contains("Progetto di prova – Codice 1 – CUP X1")
                .contains("Convenzione n. 2026/145 del 20/11/2026, stipulata il 25/11/2026, a valere sull’Avviso "
                        + "pubblico n. 10")
                .contains("Cooperativa di prova, con sede in Via della Prova 1, Firenze, codice fiscale 01234567890, "
                        + "soggetto promotore, rappresentato da Paola Neri")
                .contains(place.company().getName() + ", partita IVA " + place.company().getVatCode()
                        + ", con sede legale in Via Garibaldi 3, Firenze (FI)")
                .contains("codice ATECO 56.10.11 – Ristorazione con somministrazione")
                .contains("legale rappresentante Laura Bianchi, nato/a a Roma il 01/01/1980, codice fiscale "
                        + "BNCLRA80A41H501D, residente in Via Dante 2, Firenze")
                .contains("Mario Rossi, nato/a a Firenze il 10/12/1985, codice fiscale RSSMRA85T10A562S, residente in "
                        + "Via Gramsci 12, Sesto Fiorentino (FI).")
                .contains("Sede del tirocinio\nVia Roma 1, Sesto Fiorentino (FI)")
                .contains("Aiuto cuoco")
                .contains("Periodo della convenzione\ndal 01/12/2026 al 31/05/2027")
                .contains("dal 22/12/2026 al 06/01/2027")
                .contains("10 ore settimanali, per un monte ore complessivo di " + totalHours + " ore.")
                .contains("Lunedì: 09:00–13:00 (4 ore)\nMercoledì: 09:00–13:00 e 14:00–18:00 (6 ore)")
                .contains("Tutor del soggetto ospitante: Anna Verdi, Responsabile di sala (055 123456).")
                .contains("segnalazione n. " + ticket.getNumber())
                .doesNotContain("{");
        // nessun dato mancante: le sole righe da completare a mano sono quelle delle firme
        assertThat(text.lines().filter(line -> line.contains(InternshipDocumentServiceImpl.BLANK)))
                .allMatch(line -> line.startsWith("Per il soggetto") || line.startsWith("Il tirocinante"));

        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.TICKET, ticket.getId())
                .getLast().getChanges()).containsKey("document");
        mockMvc.perform(get("/api/v1/tickets/" + TsidMapper.toExternal(ticket.getId()) + "/history")
                        .header(USER, "tutor1"))
                .andExpect(jsonPath("$[*].action", hasItem("DOCUMENT_GENERATED")))
                .andExpect(jsonPath("$[-1].detail").value("Convenzione"));
    }

    @Test
    void progettoFormativo_modelloDelTipoECronologia() throws Exception {
        String totalHours = save(typeId("SOCIO_TERAPEUTICO"), true);

        MvcResult result = mockMvc.perform(get(internshipPath + "/documents/progetto-formativo")
                        .header(USER, "tutor1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", DOCX))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"Progetto-formativo-segnalazione-%d.docx\"".formatted(ticket.getNumber())))
                .andReturn();

        assertThat(text(result))
                .startsWith("PROGETTO FORMATIVO DEL TIROCINIO DI INSERIMENTO SOCIO-TERAPEUTICO\n")
                .contains("Mario Rossi, nato/a a Firenze il 10/12/1985")
                .contains("Tutor aziendali: Anna Verdi, Responsabile di sala (055 123456).")
                .contains("Monte ore complessivo\n" + totalHours)
                .contains("Lunedì: 09:00–13:00 (4 ore)\nMercoledì: 09:00–13:00 e 14:00–18:00 (6 ore)")
                .doesNotContain("{");
        mockMvc.perform(get("/api/v1/tickets/" + TsidMapper.toExternal(ticket.getId()) + "/history")
                        .header(USER, "tutor1"))
                .andExpect(jsonPath("$[-1].detail").value("Progetto Formativo"));
    }

    @Test
    void tipoSenzaModello_eSchedaIncompleta_righeVuote() throws Exception {
        String typeId = TsidMapper.toExternal(internshipTypes.save(new InternshipType("T_DOC_" + ticket.getId(),
                "Tipo nuovo " + ticket.getId())).getId());
        em.flush();
        save(typeId, false);

        String text = text(mockMvc.perform(get(internshipPath + "/documents/convenzione").header(USER, "tutor1"))
                .andExpect(status().isOk())
                .andReturn());
        assertThat(text)
                .startsWith("CONVENZIONE DI TIROCINIO\n")
                .contains("Tipo nuovo " + ticket.getId())
                .contains("codice fiscale " + InternshipDocumentServiceImpl.BLANK
                        + ", residente in Via Gramsci 12, Sesto Fiorentino (FI)")
                .contains("Tutor del soggetto ospitante: " + InternshipDocumentServiceImpl.BLANK + ".")
                .doesNotContain("{");
    }

    /** Carica un modello finto della convenzione come l'ADMIN (US-1010) e ne restituisce l'id. */
    private String uploadTemplate(String name, String typeId, String careAgencyId, String text) throws Exception {
        return upload("CONVENZIONE", name, typeId, careAgencyId, TestDocx.of(text, "{NOME_TIROCINANTE}"));
    }

    private String upload(String document, String name, String typeId, String careAgencyId, byte[] file)
            throws Exception {
        var request = multipart("/api/v1/document-templates")
                .file(new MockMultipartFile("file", "modello.docx", DOCX, file))
                .param("document", document)
                .param("projectId", TsidMapper.toExternal(project.getId()))
                .param("internshipTypeId", typeId)
                .param("name", name)
                .header(USER, "admin");
        if (careAgencyId != null) {
            request.param("careAgencyId", careAgencyId);
        }
        return JsonPath.read(mockMvc.perform(request).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id");
    }

    private String convenzione(String template) throws Exception {
        var request = get(internshipPath + "/documents/convenzione").header(USER, "tutor1");
        if (template != null) {
            request.param("template", template);
        }
        return text(mockMvc.perform(request).andExpect(status().isOk()).andReturn());
    }

    @Test
    void modelliCaricati_conLEnte_piuModelliDaScegliere_eRipiego() throws Exception {
        String typeId = typeId("CURRICULARE");
        save(typeId, true);
        String choices = internshipPath + "/documents/convenzione/templates";
        mockMvc.perform(get(choices).header(USER, "tutor1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        // un modello senza ente: si usa quello, e la cronologia lo ricorda
        String general = uploadTemplate("Convenzione GOL", typeId, null, "CONVENZIONE CARICATA");
        assertThat(convenzione(null)).contains("CONVENZIONE CARICATA", "Mario Rossi").doesNotContain("{");
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.TICKET, ticket.getId())
                .getLast().getChanges().get("template")).extracting("after").isEqualTo("Convenzione GOL");

        // il modello dell'ente del beneficiario vale più di quello senza ente
        String comune = TsidMapper.toExternal(893700000000000351L);
        ticket.getBeneficiary().setCareAgency(careAgencies.findById(893700000000000351L).orElseThrow());
        em.flush();
        uploadTemplate("Convenzione Comune", typeId, comune, "CONVENZIONE DEL COMUNE");
        mockMvc.perform(get(choices).header(USER, "tutor1"))
                .andExpect(jsonPath("$[*].name").value(contains("Convenzione Comune")));
        assertThat(convenzione(null)).contains("CONVENZIONE DEL COMUNE").doesNotContain("CARICATA");

        // due modelli per l'ente: il Tutor sceglie
        String bis = uploadTemplate("Convenzione Comune bis", typeId, comune, "CONVENZIONE DEL COMUNE BIS");
        mockMvc.perform(get(choices).header(USER, "tutor1"))
                .andExpect(jsonPath("$[*].name").value(
                        contains("Convenzione Comune", "Convenzione Comune bis")));
        mockMvc.perform(get(internshipPath + "/documents/convenzione").header(USER, "tutor1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("template"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Scegli il modello"));
        assertThat(convenzione(bis)).contains("CONVENZIONE DEL COMUNE BIS");
        // un modello che non vale per questa segnalazione
        mockMvc.perform(get(internshipPath + "/documents/convenzione").param("template", general)
                        .header(USER, "tutor1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("template"));

        // disattivati i modelli, il documento torna al modello generico del tipo
        for (var row : jdbc.queryForList("SELECT id, version FROM document_template")) {
            mockMvc.perform(put("/api/v1/document-templates/" + TsidMapper.toExternal((Long) row.get("id")) + "/active")
                            .header(USER, "admin").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"active\": false, \"version\": %d}".formatted((Long) row.get("version"))))
                    .andExpect(status().isOk());
        }
        assertThat(convenzione(null)).contains("CONVENZIONE DI TIROCINIO CURRICULARE").doesNotContain("CARICATA");
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.TICKET, ticket.getId())
                .getLast().getChanges().get("template")).extracting("after").isEqualTo("modello generico");
    }

    /** Salva progetti formativi e una proroga: la proroga si registra a tirocinio avviato (US-1012). */
    private void saveWithExtension() throws Exception {
        save(typeId("EXTRACURRICULARE"), true);
        ticket.setStatus(TicketStatus.IN_TIROCINIO);
        em.flush();
        long version = jdbc.queryForObject("SELECT version FROM internship WHERE ticket_id = ?", Long.class,
                ticket.getId());
        String json = """
                {"internshipTypeId": "%s", "startDate": "2026-12-01", "expectedEndDate": "2027-05-31",
                 "site": "Via Roma 1", "siteMunicipalityId": "%s", "atecoCodeId": "%s",
                 "companyTutors": [{"fullName": "Anna Verdi", "role": "Responsabile di sala", "contact": "055 123456"}],
                 "fiscalCode": "RSSMRA85T10A562S", "birthDate": "1985-12-10", "birthPlace": "Firenze",
                 "residenceMunicipalityId": "%s", "residenceStreet": "Via Gramsci", "residenceStreetNumber": "12",
                 "suspensions": [],
                 "agreementNumber": "2026/145", "agreementDate": "2026-11-20", "agreementSignedDate": "2026-11-25",
                 "trainingProjects": [
                   {"from": "2026-12-01", "to": "2027-02-28", "jobTitle": "Aiuto cuoco", "jobDetails": "Piatti freddi"},
                   {"from": "2027-03-01", "to": "2027-05-31", "jobTitle": "Cuoco", "jobDetails": null}],
                 "extensions": [{"agreementNumber": "2027/200", "agreementDate": "2027-05-20",
                   "agreementSignedDate": "2027-05-25", "from": "2027-06-01", "to": "2027-07-15",
                   "trainingProjects": [{"from": "2027-06-01", "to": "2027-07-15", "jobTitle": "Capo partita",
                     "jobDetails": null}]}],
                 "firstSlotStart": "09:00", "firstSlotEnd": "13:00", "secondSlotStart": "14:00", "secondSlotEnd": "18:00",
                 "excludeHolidays": true,
                 "schedule": [{"dayOfWeek": 1, "firstSlot": true, "secondSlot": false},
                              {"dayOfWeek": 3, "firstSlot": true, "secondSlot": true, "minutes": 360}],
                 "version": %d}""".formatted(typeId("EXTRACURRICULARE"), TsidMapper.toExternal(sesto.getId()), atecoId,
                TsidMapper.toExternal(sesto.getId()), version);
        mockMvc.perform(put(internshipPath).header(USER, "tutor1").contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    private MvcResult document(String kind, String filename, String... params) throws Exception {
        var request = get(internshipPath + "/documents/" + kind).header(USER, "tutor1");
        for (int i = 0; i < params.length; i += 2) {
            request.param(params[i], params[i + 1]);
        }
        return mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"%s-segnalazione-%d.docx\"".formatted(filename, ticket.getNumber())))
                .andReturn();
    }

    @Test
    void unaConvenzionePerProroga_unProgettoFormativoPerProgetto() throws Exception {
        saveWithExtension();

        assertThat(text(document("convenzione", "Convenzione-proroga-1", "extension", "1")))
                .contains("Convenzione n. 2027/200 del 20/05/2027, stipulata il 25/05/2027")
                .contains("Periodo della convenzione\ndal 01/06/2027 al 15/07/2027")
                .doesNotContain("{");
        assertThat(text(document("progetto-formativo", "Progetto-formativo-2", "project", "2")))
                .contains("Progetto formativo n. 2, convenzione n. 2026/145 stipulata il 25/11/2026.")
                .contains("Mansione\nCuoco")
                .contains("Periodo del progetto\ndal 01/03/2027 al 31/05/2027")
                .contains("Durata del progetto\n3 mesi")
                // il tirocinio finisce con la proroga
                .contains("Periodo del tirocinio\ndal 01/12/2026 al 15/07/2027")
                .doesNotContain("{");
        assertThat(text(document("progetto-formativo", "Progetto-formativo-proroga-1-progetto-1", "extension", "1",
                "project", "1")))
                .contains("Progetto formativo n. 1, convenzione n. 2027/200 stipulata il 25/05/2027.")
                .contains("Mansione\nCapo partita");
        mockMvc.perform(get("/api/v1/tickets/" + TsidMapper.toExternal(ticket.getId()) + "/history")
                        .header(USER, "tutor1"))
                .andExpect(jsonPath("$[-1].detail").value("Progetto Formativo 1, proroga 1"));
        // senza progetto resta il documento unico
        assertThat(text(document("progetto-formativo", "Progetto-formativo")))
                .contains("Progetto formativo n. " + InternshipDocumentServiceImpl.BLANK);

        mockMvc.perform(get(internshipPath + "/documents/convenzione").param("extension", "2").header(USER, "tutor1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Proroga non trovata"));
        mockMvc.perform(get(internshipPath + "/documents/progetto-formativo").param("project", "3")
                        .header(USER, "tutor1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Progetto formativo non trovato"));
    }

    @Test
    void relazioneFinale_soloATirocinioChiuso() throws Exception {
        saveWithExtension();
        mockMvc.perform(get(internshipPath + "/documents/relazione-finale").header(USER, "tutor1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("La relazione finale si scarica a tirocinio chiuso"));

        // chiuso con un'interruzione anticipata a metà della proroga
        ticket.setCompetenceSite(competenceSites.findByActiveTrueOrderByNameAsc().getFirst());
        closureReports.save(new ClosureReport(ticket, LocalDate.of(2027, 6, 20),
                closureReasons.findByCode("INTERRUZIONE_AZIENDA").orElseThrow(), "Il reparto ha chiuso",
                (short) 4, (short) 4, "Obiettivi", null, null,
                noHireReasons.findAllByOrderByActiveDescNameAsc().getFirst(), false));
        ticket.setStatus(TicketStatus.FORM_RESTITUZIONE);
        em.flush();

        String text = text(document("relazione-finale", "Relazione-finale"));
        assertThat(text)
                .startsWith("RELAZIONE FINALE DEL TIROCINIO\n")
                .contains("Si attesta che Mario Rossi, codice fiscale RSSMRA85T10A562S, ha partecipato al tirocinio "
                        + "Extracurriculare promosso da Cooperativa di prova")
                .contains("Periodo\ndal 01/12/2026 al 20/06/2027")
                // 6 mesi e 20 giorni: per eccesso
                .contains("Durata\n7 mesi")
                .contains("Motivo di chiusura\nInterruzione anticipata per volontà dell'azienda")
                .contains("Descrizione dell'interruzione\nIl reparto ha chiuso")
                .contains(ticket.getCompetenceSite().getName() + ", ")
                .doesNotContain("{");
    }

    /** I segnaposto dei modelli reali (US-1011) in un modello caricato dall'ADMIN. */
    @Test
    void segnapostiDeiModelliReali() throws Exception {
        saveWithExtension();
        var b = ticket.getBeneficiary();
        b.setGender(Gender.F);
        b.setCitizenship("IT");
        b.setEducationLevel(EducationLevel.DIPLOMA);
        b.setPhone("333 1234567");
        b.setEmail("mario.rossi@example.org");
        b.setCareAgency(careAgencies.findById(893700000000000351L).orElseThrow());
        b.setCaseManagerName("Giulia Blu");
        b.setCaseManagerOffice("Servizio sociale Q1");
        place.company().setPhone("055 987654");
        place.company().setEmail("info@azienda.example");
        em.flush();
        String typeId = typeId("EXTRACURRICULARE");
        upload("PROGETTO_FORMATIVO", "Progetto GOL", typeId, null, TestDocx.of(
                "Sesso: {SESSO_M} M {SESSO_F} F",
                "Cognome {COGNOME_TIROCINANTE} Nome {NOME_PROPRIO_TIROCINANTE}",
                "Cittadinanza {CITTADINANZA_TIROCINANTE}, titolo {TITOLO_STUDIO_TIROCINANTE}",
                "Contatti {TELEFONO_TIROCINANTE} {EMAIL_TIROCINANTE}",
                "Residenza {VIA_RESIDENZA_TIROCINANTE} n° {CIVICO_RESIDENZA_TIROCINANTE}, "
                        + "{COMUNE_RESIDENZA_TIROCINANTE} ({PROVINCIA_RESIDENZA_TIROCINANTE})",
                "Ente {ENTE}; case manager {CASE_MANAGER} ({SERVIZIO_CASE_MANAGER}), tel. {TELEFONO_CASE_MANAGER}",
                "Azienda {TELEFONO_AZIENDA} {EMAIL_AZIENDA}, sede legale a {COMUNE_SEDE_LEGALE} "
                        + "({PROVINCIA_SEDE_LEGALE})",
                "Sede del tirocinio nella provincia di {NOME_PROVINCIA_SEDE_TIROCINIO}",
                "Lu {GIORNO_LUNEDI} Ma {GIORNO_MARTEDI} Me {GIORNO_MERCOLEDI}",
                "Lunedì [{LUNEDI_MATTINA}] [{LUNEDI_POMERIGGIO}] Mercoledì [{MERCOLEDI_POMERIGGIO}]",
                "Mattina dalle {MATTINA_DALLE} alle {MATTINA_ALLE}",
                "Mesi {MESI_TIROCINIO}, progetto {MESI_PROGETTO_FORMATIVO} mesi e {ORE_PROGETTO_FORMATIVO} ore",
                "INAIL {POSIZIONE_INAIL}, RC {COMPAGNIA_RC} {POLIZZA_RC} scadenza {SCADENZA_RC}",
                "{AVVISO}",
                "Chiusura [{DESCRIZIONE_INTERRUZIONE}] {DATA_FINE_EFFETTIVA}"));

        String text = text(document("progetto-formativo", "Progetto-formativo-1", "project", "1"));
        assertThat(text)
                .contains("Sesso: ☐ M ☒ F")
                .contains("Cognome Rossi Nome Mario")
                .contains("Cittadinanza Italia, titolo Diploma di scuola superiore")
                .contains("Contatti 333 1234567 mario.rossi@example.org")
                .contains("Residenza Via Gramsci n° 12, Sesto Fiorentino (FI)")
                .contains("Ente Comune di Firenze; case manager Giulia Blu (Servizio sociale Q1), tel. "
                        + InternshipDocumentServiceImpl.BLANK)
                .contains("Azienda 055 987654 info@azienda.example, sede legale a Firenze (FI)")
                .contains("Lu ☒ Ma ☐ Me ☒")
                .contains("Lunedì [09:00–13:00] [] Mercoledì [14:00–18:00]")
                .contains("Mattina dalle 09:00 alle 13:00")
                // tirocinio non chiuso: fino alla fine della proroga, 7 mesi e 15 giorni
                .contains("Mesi 8, progetto 3 mesi e ")
                .contains("INAIL 123456, RC Assicurazioni di prova RC-1 scadenza 22/07/2027")
                .contains("Avviso pubblico n. 10 per l’attuazione del Programma Garanzia Occupabilità")
                .contains("Chiusura [] " + InternshipDocumentServiceImpl.BLANK);
        assertThat(text).containsPattern("Sede del tirocinio nella provincia di \\S+");
    }

    @Test
    void soloIlTutorDellaSegnalazione_documentoNotoEDallAppuntamento() throws Exception {
        mockMvc.perform(get(internshipPath + "/documents/convenzione").header(USER, "tutor2"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(internshipPath + "/documents/convenzione").header(USER, "operatore.cc"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(internshipPath + "/documents/lettera").header(USER, "tutor1"))
                .andExpect(status().isNotFound());

        ticket.setStatus(TicketStatus.PROPOSTA_ACCOLTA);
        em.flush();
        mockMvc.perform(get(internshipPath + "/documents/convenzione").header(USER, "tutor1"))
                .andExpect(status().isConflict());
    }
}
