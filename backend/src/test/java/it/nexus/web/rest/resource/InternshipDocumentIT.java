package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.security.MockHeaderAuthenticationFilter;
import it.nexus.domain.CompanyAtecoCode;
import it.nexus.domain.CompanyLegalRepresentative;
import it.nexus.domain.InternshipType;
import it.nexus.domain.JobSlot;
import it.nexus.domain.Project;
import it.nexus.domain.Ticket;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AuditEventRepository;
import it.nexus.repository.CompanyAtecoCodeRepository;
import it.nexus.repository.CompanyLegalRepresentativeRepository;
import it.nexus.repository.InternshipTypeRepository;
import it.nexus.services.documents.DocxTemplate;
import it.nexus.services.impl.InternshipDocumentServiceImpl;
import it.nexus.support.WorkflowScenarios;
import it.nexus.support.WorkflowScenarios.Actors;
import it.nexus.support.WorkflowScenarios.Place;
import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, WorkflowScenarios.class})
/** Convenzione in Word (US-1004): compilata dalla scheda, solo per il Tutor della segnalazione. */
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

    private Place place;
    private Ticket ticket;
    private String internshipPath;
    private String atecoId;

    @BeforeEach
    void setUp() {
        Actors a = scenarios.actors();
        place = scenarios.place();
        place.company().setLegalAddress("Via Garibaldi 3, 50123 Firenze");
        Project project = scenarios.project("GOL", a.tutor1(), a.tutor2());
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
                 "site": "Via Roma 1, Firenze", "atecoCodeId": "%s",
                 "companyTutors": [{"fullName": "Anna Verdi", "contact": "055 123456"}],
                 "fiscalCode": "RSSMRA85T10A562S", "birthDate": "1985-12-10", "birthPlace": "Firenze",
                 "residence": "Via Dante 2, Firenze",
                 "suspensions": [{"from": "2026-12-22", "to": "2027-01-06"}],
                 "firstSlotStart": "09:00", "firstSlotEnd": "13:00", "secondSlotStart": "14:00", "secondSlotEnd": "18:00",
                 "excludeHolidays": true,
                 "schedule": [{"dayOfWeek": 1, "firstSlot": true, "secondSlot": false},
                              {"dayOfWeek": 3, "firstSlot": true, "secondSlot": true, "minutes": 360}],
                 "version": null}""".formatted(typeId, atecoId);
        if (!complete) {
            json = json.replace("\"fiscalCode\": \"RSSMRA85T10A562S\", ", "")
                    .replace("[{\"fullName\": \"Anna Verdi\", \"contact\": \"055 123456\"}]", "[]");
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
                .contains(place.company().getName() + ", partita IVA " + place.company().getVatCode()
                        + ", con sede legale in Via Garibaldi 3, 50123 Firenze")
                .contains("codice ATECO 56.10.11 – Ristorazione con somministrazione")
                .contains("legale rappresentante Laura Bianchi, nato/a a Roma il 01/01/1980, codice fiscale "
                        + "BNCLRA80A41H501D, residente in Via Dante 2, Firenze")
                .contains("Mario Rossi, nato/a a Firenze il 10/12/1985, codice fiscale RSSMRA85T10A562S")
                .contains("Aiuto cuoco")
                .contains("dal 01/12/2026 al 31/05/2027")
                .contains("dal 22/12/2026 al 06/01/2027")
                .contains("10 ore settimanali, per un monte ore complessivo di " + totalHours + " ore.")
                .contains("Lunedì: 09:00–13:00 (4 ore)\nMercoledì: 09:00–13:00 e 14:00–18:00 (6 ore)")
                .contains("Tutor del soggetto ospitante: Anna Verdi (055 123456).")
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
                .contains("Tutor aziendali: Anna Verdi (055 123456).")
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
                .contains("codice fiscale " + InternshipDocumentServiceImpl.BLANK + ", residente in Via Dante 2")
                .contains("Tutor del soggetto ospitante: " + InternshipDocumentServiceImpl.BLANK + ".")
                .doesNotContain("{");
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
