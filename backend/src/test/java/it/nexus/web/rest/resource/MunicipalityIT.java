package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.security.MockHeaderAuthenticationFilter;
import it.nexus.support.IstatFiles;
import it.nexus.support.WorkflowScenarios;

/** Comuni e province (US-114): anteprima e caricamento del file ISTAT con le versioni, ricerca, permessi. */
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, WorkflowScenarios.class})
class MunicipalityIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;
    private static final LocalDate JANUARY = LocalDate.of(2026, 1, 1);
    private static final LocalDate MARCH = LocalDate.of(2026, 3, 1);

    @Autowired MockMvc mockMvc;
    @Autowired WorkflowScenarios scenarios;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        scenarios.actors();
    }

    private ResultActions preview(String user, byte[] file) throws Exception {
        return mockMvc.perform(multipart("/api/v1/municipalities/import/preview")
                .file(new MockMultipartFile("file", "Elenco-comuni-italiani.csv", "text/csv", file))
                .header(USER, user));
    }

    private ResultActions importFile(String user, byte[] file, LocalDate validFrom) throws Exception {
        return mockMvc.perform(multipart("/api/v1/municipalities/import")
                .file(new MockMultipartFile("file", "Elenco-comuni-italiani.csv", "text/csv", file))
                .param("validFrom", validFrom.toString())
                .header(USER, user));
    }

    private ResultActions search(String user, String q) throws Exception {
        return mockMvc.perform(get("/api/v1/municipalities").param("q", q).header(USER, user));
    }

    private void loadJanuary() throws Exception {
        importFile("admin", IstatFiles.file(IstatFiles.firenze(), IstatFiles.sesto(), IstatFiles.fiesole(),
                IstatFiles.prato()), JANUARY).andExpect(status().isCreated());
    }

    @Test
    void primoCaricamento_anteprimaPoiConferma() throws Exception {
        byte[] file = IstatFiles.file(IstatFiles.firenze(), IstatFiles.sesto(), IstatFiles.fiesole(),
                IstatFiles.prato());
        preview("admin", file)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows").value(4))
                .andExpect(jsonPath("$.municipalities.added").value(4))
                .andExpect(jsonPath("$.municipalities.addedExamples[0]").value("Fiesole (FI, 048015)"))
                .andExpect(jsonPath("$.provinces.added").value(2));
        // l'anteprima non salva nulla
        search("tutor1", "sesto").andExpect(jsonPath("$", hasSize(0)));

        importFile("admin", file, JANUARY).andExpect(status().isCreated())
                .andExpect(jsonPath("$.municipalities.added").value(4));

        search("tutor1", "sesto f")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Sesto Fiorentino"))
                .andExpect(jsonPath("$[0].provinceAbbreviation").value("FI"))
                .andExpect(jsonPath("$[0].validFrom").value("2026-01-01"))
                .andExpect(jsonPath("$[0].validTo").doesNotExist());
        mockMvc.perform(get("/api/v1/municipalities/status").header(USER, "admin"))
                .andExpect(jsonPath("$.municipalities").value(4))
                .andExpect(jsonPath("$.provinces").value(2))
                .andExpect(jsonPath("$.lastImport.validFrom").value("2026-01-01"));
        assertThat(jdbc.queryForObject(
                "SELECT count(*) FROM audit_event WHERE entity_type = 'MUNICIPALITY_IMPORT' AND action = 'IMPORT'",
                Integer.class)).isEqualTo(1);
    }

    @Test
    void comuneCambiatoESoppresso_versioniChiuseENuove() throws Exception {
        loadJanuary();
        // marzo: Sesto passa a Prato con un nuovo codice, Fiesole è soppresso, arriva Vaiano
        byte[] march = IstatFiles.file(IstatFiles.firenze(),
                IstatFiles.row("I684", "100099", "Sesto Fiorentino", "100", "Prato", "PO"), IstatFiles.prato(),
                IstatFiles.row("L537", "100006", "Vaiano", "100", "Prato", "PO"));
        preview("admin", march)
                .andExpect(jsonPath("$.municipalities.added").value(1))
                .andExpect(jsonPath("$.municipalities.removed").value(1))
                .andExpect(jsonPath("$.municipalities.removedExamples[0]").value("Fiesole (FI, 048015)"))
                .andExpect(jsonPath("$.municipalities.changed").value(1))
                .andExpect(jsonPath("$.municipalities.changedExamples[0]")
                        .value("Sesto Fiorentino (FI, 048043) → Sesto Fiorentino (PO, 100099)"))
                .andExpect(jsonPath("$.provinces.changed").value(0));
        importFile("admin", march, MARCH).andExpect(status().isCreated());

        search("tutor1", "sesto").andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].provinceAbbreviation").value("PO"))
                .andExpect(jsonPath("$[0].validFrom").value("2026-03-01"));
        search("tutor1", "fiesole").andExpect(jsonPath("$", hasSize(0)));
        // le versioni passate restano, chiuse alla data del caricamento
        mockMvc.perform(get("/api/v1/municipalities/history").param("q", "sesto").header(USER, "admin"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].provinceAbbreviation").value("FI"))
                .andExpect(jsonPath("$[1].validTo").value("2026-03-01"));
        mockMvc.perform(get("/api/v1/municipalities/history").param("q", "fiesole").header(USER, "admin"))
                .andExpect(jsonPath("$[0].validTo").value("2026-03-01"));
    }

    @Test
    void provinciaRinominata_nuoveVersioniAncheDeiSuoiComuni() throws Exception {
        loadJanuary();
        byte[] march = IstatFiles.file(
                IstatFiles.row("D612", "048017", "Firenze", "248", "Città metropolitana di Firenze", "FI"),
                IstatFiles.row("I684", "048043", "Sesto Fiorentino", "248", "Città metropolitana di Firenze", "FI"),
                IstatFiles.row("D575", "048015", "Fiesole", "248", "Città metropolitana di Firenze", "FI"),
                IstatFiles.prato());
        importFile("admin", march, MARCH).andExpect(status().isCreated())
                .andExpect(jsonPath("$.provinces.changed").value(1))
                .andExpect(jsonPath("$.provinces.changedExamples[0]")
                        .value("Firenze (FI) → Città metropolitana di Firenze (FI)"))
                .andExpect(jsonPath("$.municipalities.changed").value(3));
        search("tutor1", "firenze").andExpect(jsonPath("$[0].provinceName").value("Città metropolitana di Firenze"));
    }

    @Test
    void dataNonSuccessivaONelFuturo_rifiutata() throws Exception {
        loadJanuary();
        byte[] file = IstatFiles.file(IstatFiles.firenze(), IstatFiles.sesto(), IstatFiles.prato());
        importFile("admin", file, JANUARY).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("La data deve essere successiva a quella dell'ultimo caricamento (01/01/2026)"));
        importFile("admin", file, LocalDate.now().plusDays(2)).andExpect(status().isBadRequest());
        search("tutor1", "fiesole").andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void fileSbagliato_rifiutatoSenzaCambiare() throws Exception {
        loadJanuary();
        importFile("admin", "nome;cognome\r\nMario;Rossi\r\n".getBytes(), MARCH)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("ISTAT")));
        importFile("admin", IstatFiles.file(IstatFiles.firenze()), MARCH).andExpect(status().isBadRequest());
        search("tutor1", "fiesole").andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void ricerca_dallaSecondaLetteraEConICaratteriSpeciali() throws Exception {
        loadJanuary();
        search("operatore.cc", "f").andExpect(jsonPath("$", hasSize(0)));
        // prima i nomi che iniziano con il testo
        search("operatore.cc", "fi").andExpect(jsonPath("$[0].name").value("Fiesole"))
                .andExpect(jsonPath("$[1].name").value("Firenze"))
                .andExpect(jsonPath("$[2].name").value("Sesto Fiorentino"));
        search("tutor1", "%").andExpect(jsonPath("$", hasSize(0)));
        search("tutor1", "_e").andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void soloLAdminCarica() throws Exception {
        byte[] file = IstatFiles.file(IstatFiles.firenze(), IstatFiles.sesto(), IstatFiles.prato());
        preview("tutor1", file).andExpect(status().isForbidden());
        importFile("operatore.cc", file, JANUARY).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/municipalities/status").header(USER, "tutor1")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/municipalities/history").param("q", "fi").header(USER, "tutor1"))
                .andExpect(status().isForbidden());
        search("tutor1", "fi").andExpect(status().isOk());
    }
}
