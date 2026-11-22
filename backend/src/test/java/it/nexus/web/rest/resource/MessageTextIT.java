package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import it.nexus.domain.Ticket;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.NotificationType;
import it.nexus.repository.AuditEventRepository;
import it.nexus.services.MessageTextService;
import it.nexus.services.NotificationService;
import it.nexus.services.mail.NotificationEmail;
import it.nexus.support.WorkflowScenarios;
import it.nexus.support.WorkflowScenarios.Actors;

/**
 * Testi delle notifiche e delle email modificabili dall'ADMIN (US-1204, PO 22/11): testo nell'app, parte distintiva
 * dell'oggetto e testo dell'email, distinti; segnaposto controllati; ripristino del predefinito; audit.
 */
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, WorkflowScenarios.class})
class MessageTextIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;
    private static final String API = "/api/v1/message-texts";

    @Autowired MockMvc mockMvc;
    @Autowired WorkflowScenarios scenarios;
    @Autowired NotificationService notificationService;
    @Autowired MessageTextService messageTextService;
    @Autowired AuditEventRepository audit;

    private Actors actors;

    @BeforeEach
    void setUp() {
        actors = scenarios.actors();
    }

    private ResultActions change(String key, String part, String text) throws Exception {
        String json = "{\"text\": \"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}";
        return mockMvc.perform(put(API + "/" + key + "/" + part).header(USER, "admin")
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void elenco_tutteLeNotificheELeEmailDelCambioIndirizzo_conIPredefiniti() throws Exception {
        mockMvc.perform(get(API).header(USER, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(NotificationType.values().length + 2)))
                .andExpect(jsonPath("$[?(@.key == 'PROPOSAL_SENT')].parts[*].part")
                        .value(org.hamcrest.Matchers.contains("APP", "EMAIL_SUBJECT", "EMAIL_BODY")))
                .andExpect(jsonPath("$[?(@.key == 'PROPOSAL_SENT')].placeholders[0]").value("numero"))
                .andExpect(jsonPath("$[?(@.key == 'PROPOSAL_SENT')].parts[1].text").value("Nuova proposta di tirocinio"))
                .andExpect(jsonPath("$[?(@.key == 'PROPOSAL_SENT')].parts[1].custom").value(false))
                .andExpect(jsonPath("$[?(@.key == 'EMAIL_CHANGE_CODE')].parts[*].part")
                        .value(org.hamcrest.Matchers.contains("EMAIL_SUBJECT", "EMAIL_BODY")))
                .andExpect(jsonPath("$[?(@.key == 'EMAIL_CHANGE_CODE')].placeholders[*]")
                        .value(org.hamcrest.Matchers.contains("codice", "ora")));
    }

    @Test
    void testiDistinti_appEdEmailCambiano_ciascunoPerConto() throws Exception {
        Ticket ticket = scenarios.appointmentTicket(actors.tutor1(),
                scenarios.project("GOL", actors.tutor1()), scenarios.freeSlot(scenarios.place(), "Cuoco"),
                actors.operator());
        notificationService.notify(actors.tutor1(), NotificationType.PROPOSAL_SENT, ticket);

        change("PROPOSAL_SENT", "APP", "  Proposta per la n. {numero}:   guardala  ")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parts[0].text").value("Proposta per la n. {numero}: guardala"))
                .andExpect(jsonPath("$.parts[0].custom").value(true))
                .andExpect(jsonPath("$.parts[2].custom").value(false));
        change("PROPOSAL_SENT", "EMAIL_SUBJECT", "Valuta la proposta").andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/notifications").header(USER, "tutor1"))
                .andExpect(jsonPath("$.items[0].text")
                        .value("Proposta per la n. " + ticket.getNumber() + ": guardala"));
        NotificationEmail email = NotificationEmail.of(messageTextService.current(), NotificationType.PROPOSAL_SENT,
                "T1", ticket.getNumber(), "https://portalenexus.it");
        assertThat(email.subject())
                .isEqualTo("PORTALE NEXUS - Segnalazione n. " + ticket.getNumber() + " - Valuta la proposta");
        // il testo dell'email non è cambiato: è distinto da quello dell'app
        assertThat(email.body()).startsWith("C'è una proposta di tirocinio per la segnalazione n. " + ticket.getNumber());
    }

    @Test
    void segnapostoSconosciuto_graffeSpaiate_testoVuoto_rifiutatiSulCampo() throws Exception {
        change("PROPOSAL_SENT", "EMAIL_BODY", "Ciao {nome}, c'è una proposta")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("text"))
                .andExpect(jsonPath("$.fieldErrors[0].message")
                        .value(containsString("Segnaposto sconosciuto {nome}: si possono usare {numero}")));
        change("PROPOSAL_SENT", "EMAIL_BODY", "Proposta {numero")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].message").value(containsString("parentesi graffe")));
        change("EMAIL_CHANGE_NOTICE", "EMAIL_BODY", "Indirizzo cambiato per {numero}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].message").value(containsString("non ammette segnaposto")));
        change("PROPOSAL_SENT", "APP", "   ")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Scrivi il testo"));
        change("PROPOSAL_SENT", "EMAIL_SUBJECT", "x".repeat(101))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Massimo 100 caratteri"));
    }

    @Test
    void parteCheIlMessaggioNonHa_404() throws Exception {
        change("EMAIL_CHANGE_CODE", "APP", "Codice").andExpect(status().isNotFound());
        change("NON_ESISTE", "APP", "Testo").andExpect(status().isNotFound());
    }

    @Test
    void ripristino_tornaIlPredefinito_eLAuditRegistraEntrambi() throws Exception {
        String id = com.jayway.jsonpath.JsonPath.read(change("EMAIL_CHANGE_CODE", "EMAIL_SUBJECT", "Il tuo codice")
                .andExpect(jsonPath("$.parts[0].custom").value(true))
                .andReturn().getResponse().getContentAsString(), "$.key");
        assertThat(id).isEqualTo("EMAIL_CHANGE_CODE");

        mockMvc.perform(delete(API + "/EMAIL_CHANGE_CODE/EMAIL_SUBJECT").header(USER, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parts[0].custom").value(false))
                .andExpect(jsonPath("$.parts[0].text").value("Codice per confermare il nuovo indirizzo email"));

        assertThat(audit.findAll()).filteredOn(e -> e.getEntityType() == AuditEntityType.MESSAGE_TEXT)
                .extracting(e -> e.getAction()).containsExactly("TEXT_CHANGED", "TEXT_RESET");
    }

    @Test
    void testoUgualeAlPredefinito_nessunaModifica() throws Exception {
        change("PROPOSAL_SENT", "EMAIL_SUBJECT", "Nuova proposta di tirocinio")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parts[1].custom").value(false));
        assertThat(audit.findAll()).noneMatch(e -> e.getEntityType() == AuditEntityType.MESSAGE_TEXT);
    }

    @Test
    void soloLAdmin() throws Exception {
        mockMvc.perform(get(API).header(USER, "tutor1")).andExpect(status().isForbidden());
        mockMvc.perform(put(API + "/PROPOSAL_SENT/APP").header(USER, "operatore.cc")
                .contentType(MediaType.APPLICATION_JSON).content("{\"text\": \"x {numero}\"}"))
                .andExpect(status().isForbidden());
    }
}
