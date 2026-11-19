package it.nexus.legacy;

import static it.nexus.legacy.LegacyAccessFile.at;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.TestcontainersConfiguration;
import it.nexus.domain.Company;
import it.nexus.domain.CompanyContact;
import it.nexus.domain.CompanyPlaceholders;
import it.nexus.domain.Municipality;
import it.nexus.domain.dto.CompanyNoteDTO;
import it.nexus.domain.enumeration.CompanyMissingData;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.CompanyRepository;
import it.nexus.services.CompanyNoteService;
import it.nexus.support.WorkflowScenarios;
import jakarta.persistence.EntityManager;

/** Importazione dal database attuale (US-1309) da un file Access costruito nel test, con dati inventati. */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, WorkflowScenarios.class})
class LegacyImportIT {

    private static final ZoneId ROME = ZoneId.of("Europe/Rome");

    @TempDir Path dir;
    @Autowired LegacyImportService service;
    @Autowired JdbcTemplate jdbc;
    @Autowired CompanyRepository companies;
    @Autowired CompanyNoteService notes;
    @Autowired WorkflowScenarios scenarios;
    @Autowired EntityManager em;

    /** Tre aziende e tre chiamate con le forme dei dati veri. */
    private Path accessFile() throws Exception {
        Path file = dir.resolve("nexus.mdb");
        try (LegacyAccessFile access = LegacyAccessFile.create(file)) {
            access.company(12, Map.ofEntries(
                    Map.entry("NomeSocietà", "Hotel Bellavista"),
                    Map.entry("Indirizzo_1", "Via Roma 1"),
                    Map.entry("Sede Legale", "SCANDICCI FI"),
                    Map.entry("Indirizzo_2", "Via dei Mille 3"),
                    Map.entry("Sede Locale", "PRATO"),
                    Map.entry("TelefonoUfficio", "055 123456"),
                    Map.entry("TelefonoCellulare", "333 1234567 (sera)"),
                    Map.entry("IndirizzoPostaElettronica_1", "info@bellavista.test#http://info@bellavista.test#"),
                    Map.entry("IndirizzoPostaElettronica_2", "booking@bellavista.test#http://booking@bellavista.test#"),
                    Map.entry("Nominativo_1", "Paola Neri"),
                    Map.entry("IDTipoContatto_1", 13),
                    Map.entry("IDTipoContatto_2", 3),
                    Map.entry("Id_nuova_cat", 1),
                    Map.entry("Paese", "HOTEL"),
                    Map.entry("Note", "Chiamare al mattino"),
                    Map.entry("data registrazione", at(2015, 2, 23, 0, 0)),
                    Map.entry("Data_um_4", at(2019, 5, 10, 10, 30))));
            access.company(13, Map.of(
                    "Sede Legale", "FI",
                    "Nominativo_1", "Mario Rossi",
                    "IDTipoContatto_1", 28,
                    "Nominativo_2", "Anna Bianchi",
                    "IDTipoContatto_2", 15));
            access.company(14, Map.of("NomeSocietà", " hotel  BELLAVISTA ", "Sede Legale", "Scandicci",
                    "Sede Locale", "P.I. 01234567890"));
            access.call(1, 12, Map.of(
                    "DataChiamata", at(2019, 5, 10, 0, 0),
                    "OraChiamata", at(1899, 12, 30, 9, 30),
                    "Oggetto", "Disponibilità per un tirocinio",
                    "IDEsito", 2,
                    "Note", "Sentito il titolare per l'utente Mario Rossi",
                    "tirocinio", true,
                    "Id_progetto", 26));
            access.call(2, 999, Map.of("Note", "Azienda cancellata da Access"));
            access.call(3, 0, Map.of("Evasa", true));
        }
        return file;
    }

    private Municipality scandicci;
    private Municipality prato;

    private void municipalities() {
        scandicci = scenarios.municipality("Scandicci", "FI");
        prato = scenarios.municipality("Prato", "PO");
        em.flush();
    }

    private Company company(int number) {
        return companies.findById(LegacyIds.company(number)).orElseThrow();
    }

    @Test
    void aziendeConSediRecapitiReferentiCategoriaENota() throws Exception {
        municipalities();
        LegacyImportReport report = service.importFile(accessFile());
        em.clear();

        assertThat(report.companiesAdded()).isEqualTo(3);
        Company hotel = company(12);
        assertThat(hotel.getName()).isEqualTo("Hotel Bellavista");
        assertThat(hotel.getVatCode()).isEqualTo(CompanyPlaceholders.VAT_CODE);
        assertThat(hotel.missingData()).containsExactly(CompanyMissingData.VAT_CODE);
        assertThat(hotel.getLegalAddress()).isEqualTo("Via Roma 1");
        assertThat(hotel.getLegalMunicipality().getId()).isEqualTo(scandicci.getId());
        assertThat(hotel.getOperatingAddress()).isEqualTo("Via dei Mille 3");
        assertThat(hotel.getOperatingMunicipality().getId()).isEqualTo(prato.getId());
        assertThat(hotel.getPhone()).isEqualTo("055 123456");
        assertThat(hotel.getMobile()).isEqualTo("333 1234567");
        assertThat(hotel.getEmail()).isEqualTo("info@bellavista.test");
        assertThat(hotel.getSecondaryEmail()).isEqualTo("booking@bellavista.test");
        assertThat(hotel.getBusinessCategory().getName()).isEqualTo("Albergo");
        assertThat(hotel.getActivity()).isEqualTo("HOTEL");
        assertThat(hotel.isActive()).isTrue();
        assertThat(hotel.getCreatedAt()).isEqualTo(at(2015, 2, 23, 0, 0).atZone(ROME).toInstant());
        assertThat(hotel.getContacts()).extracting(CompanyContact::getName, c -> c.getRole().getName())
                .containsExactly(org.assertj.core.groups.Tuple.tuple("Paola Neri", "Titolare"),
                        org.assertj.core.groups.Tuple.tuple(null, "Responsabile del personale"));

        // nota di Access con la data dell'ultima modifica; il cellulare con il testo va in una nota a parte
        List<CompanyNoteDTO> diary = notes.list(TsidMapper.toExternal(hotel.getId()));
        assertThat(diary).extracting(CompanyNoteDTO::authorName).containsOnly("Database attuale");
        assertThat(diary).filteredOn(n -> n.text().equals("Chiamare al mattino")).singleElement()
                .satisfies(n -> assertThat(n.createdAt())
                        .isEqualTo(at(2019, 5, 10, 10, 30).atZone(ROME).toInstant()));
        assertThat(diary).filteredOn(n -> n.text().startsWith("Recapiti del database attuale")).singleElement()
                .satisfies(n -> assertThat(n.text()).contains("Cellulare: «333 1234567 (sera)»"));

        Company unnamed = company(13);
        assertThat(unnamed.getName()).isEqualTo("Azienda senza nome n. 13");
        assertThat(unnamed.missingData()).containsExactly(CompanyMissingData.NAME, CompanyMissingData.VAT_CODE);
        assertThat(unnamed.getLegalMunicipality()).isNull();
        // ruolo 28 = doppione di «Legale Rappresentante»; ruolo 15 = riga vuota di Access: senza ruolo
        assertThat(unnamed.getContacts()).extracting(CompanyContact::getName, c -> c.getRole() == null
                ? null : c.getRole().getName())
                .containsExactly(org.assertj.core.groups.Tuple.tuple("Mario Rossi", "Legale rappresentante"),
                        org.assertj.core.groups.Tuple.tuple("Anna Bianchi", null));

        assertThat(jdbc.queryForList("SELECT changes::text FROM audit_event WHERE action = 'IMPORT' AND entity_id = ?",
                String.class, LegacyIds.company(12))).singleElement().asString().contains("\"legacyNumber\": 12");
    }

    @Test
    void chiamateNellArchivio_collegateAllAziendaQuandoEsiste() throws Exception {
        municipalities();
        LegacyImportReport report = service.importFile(accessFile());

        assertThat(report.callsAdded()).isEqualTo(3);
        Map<String, Object> first = jdbc.queryForMap("SELECT * FROM legacy_call WHERE legacy_number = 1");
        assertThat(first).containsEntry("company_id", LegacyIds.company(12))
                .containsEntry("legacy_company_number", 12)
                .containsEntry("subject", "Disponibilità per un tirocinio")
                .containsEntry("outcome", "Richiamare")
                .containsEntry("project", "GOL")
                .containsEntry("operator", "Verdi Paolo")
                .containsEntry("internship", true)
                .containsEntry("handled", false);
        assertThat(((Timestamp) first.get("called_at")).toInstant())
                .isEqualTo(at(2019, 5, 10, 9, 30).atZone(ROME).toInstant());
        assertThat(jdbc.queryForList(
                "SELECT legacy_company_number FROM legacy_call WHERE company_id IS NULL ORDER BY legacy_number",
                Integer.class)).containsExactly(999, 0);
        assertThat(jdbc.queryForObject("SELECT handled FROM legacy_call WHERE legacy_number = 3", Boolean.class))
                .isTrue();
    }

    @Test
    void secondaEsecuzione_nonDuplicaENonCambiaIDatiCorretti() throws Exception {
        municipalities();
        Path file = accessFile();
        service.importFile(file);
        jdbc.update("UPDATE company SET name = 'Hotel Bellavista S.r.l.', version = version + 1 WHERE id = ?",
                LegacyIds.company(12));
        long rows = jdbc.queryForObject("""
                SELECT (SELECT count(*) FROM company) + (SELECT count(*) FROM company_contact)
                     + (SELECT count(*) FROM company_note) + (SELECT count(*) FROM legacy_call)""", Long.class);

        LegacyImportReport again = service.importFile(file);

        assertThat(again.companiesAdded()).isZero();
        assertThat(again.callsAdded()).isZero();
        assertThat(jdbc.queryForObject("""
                SELECT (SELECT count(*) FROM company) + (SELECT count(*) FROM company_contact)
                     + (SELECT count(*) FROM company_note) + (SELECT count(*) FROM legacy_call)""", Long.class))
                .isEqualTo(rows);
        assertThat(jdbc.queryForObject("SELECT name FROM company WHERE id = ?", String.class, LegacyIds.company(12)))
                .isEqualTo("Hotel Bellavista S.r.l.");
    }

    @Test
    void senzaElencoDeiComuni_nonScriveNulla() throws Exception {
        assertThat(jdbc.queryForObject("SELECT count(*) FROM municipality WHERE valid_to IS NULL", Long.class))
                .as("il test parte senza l'elenco dei comuni").isZero();
        Path file = accessFile();

        assertThatThrownBy(() -> service.importFile(file)).isInstanceOf(LegacyImportException.class)
                .hasMessageContaining("elenco dei comuni");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM legacy_call", Long.class)).isZero();
        assertThat(companies.findById(LegacyIds.company(12))).isEmpty();
    }

    @Test
    void resoconto_daRivedereSenzaDatiPersonali() throws Exception {
        municipalities();
        String text = service.importFile(accessFile()).toText();

        assertThat(text).contains("- Aziende lette: 3", "- Chiamate nuove nell'archivio: 3",
                "Comune della sede legale abbinato: 2 (nome esatto 1, nome e sigla 1)");
        // duplicati per nome normalizzato, comuni non trovati, senza nome, chiamate da controllare: solo numeri
        assertThat(text).contains("### Duplicati per nome (1 gruppi)", "- n. 12 (/aziende/"
                + TsidMapper.toExternal(LegacyIds.company(12)) + "), n. 14 (/aziende/");
        assertThat(text).contains("- «FI» (1): n. 13 (/aziende/", "- «P.I. …» (1): n. 14 (/aziende/");
        assertThat(text).contains("### Aziende senza nome (1)", "### Chiamate con possibili nomi di beneficiari (1)",
                "- chiamata n. 1");
        assertThat(text).doesNotContainIgnoringCase("bellavista").doesNotContain("Paola", "Mario", "Rossi", "Neri",
                "333", "055", "01234567890", "Chiamare al mattino", "Via Roma", "Verdi");
    }
}
