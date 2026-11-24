package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.InstanceOfAssertFactories;
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
import it.nexus.domain.AppUser;
import it.nexus.domain.AuditEvent;
import it.nexus.domain.BusinessCategory;
import it.nexus.domain.Company;
import it.nexus.domain.CompanyPlaceholders;
import it.nexus.domain.DomainFixtures;
import it.nexus.domain.ContactRole;
import it.nexus.domain.JobCategory;
import it.nexus.domain.JobSlot;
import it.nexus.domain.Municipality;
import it.nexus.domain.TestEntities;
import it.nexus.domain.Ticket;
import it.nexus.domain.Zone;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.Role;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.AuditEventRepository;
import it.nexus.repository.BeneficiaryRepository;
import it.nexus.repository.CompanyRepository;
import it.nexus.repository.JobCategoryRepository;
import it.nexus.repository.JobSlotRepository;
import it.nexus.repository.ProjectRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.ZoneRepository;
import it.nexus.support.WorkflowScenarios;
import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, WorkflowScenarios.class})
class CompanyResourceIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;
    private static final String API = "/api/v1/companies";

    @Autowired MockMvc mockMvc;
    @Autowired AppUserRepository users;
    @Autowired AuditEventRepository audit;
    @Autowired CompanyRepository companies;
    @Autowired ZoneRepository zones;
    @Autowired JobCategoryRepository categories;
    @Autowired ProjectRepository projects;
    @Autowired BeneficiaryRepository beneficiaries;
    @Autowired TicketRepository tickets;
    @Autowired JobSlotRepository jobSlots;
    @Autowired EntityManager em;
    @Autowired WorkflowScenarios scenarios;

    @BeforeEach
    void setUp() {
        users.save(TestEntities.user("tutor1", Role.TUTOR, "00000000-0000-4000-8000-000000000001"));
        users.save(TestEntities.user("operatore.cc", Role.CALL_CENTER, "00000000-0000-4000-8000-000000000003"));
        users.save(TestEntities.user("admin", Role.ADMIN, "00000000-0000-4000-8000-000000000004"));
    }

    private static String body(String name, String vat, String extra) {
        return """
                {"name": "%s", "vatCode": "%s", "legalAddress": "Via Roma 1, Firenze", "contacts": [{"name": "Paola Neri"}],
                 "phone": "+39 055 123456", "email": "info@azienda.test" %s}
                """.formatted(name, vat, extra);
    }

    private ResultActions create(String user, String json) throws Exception {
        return mockMvc.perform(post(API).header(USER, user).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private String createdId(String name, String vat) throws Exception {
        return JsonPath.read(create("operatore.cc", body(name, vat, "")).andReturn().getResponse().getContentAsString(), "$.id");
    }

    @Test
    void creazione_201EAudit() throws Exception {
        String id = JsonPath.read(create("operatore.cc", body("Officina Rossi", " 12345678901 ", ""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vatCode").value("12345678901"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.version").value(0))
                .andReturn().getResponse().getContentAsString(), "$.id");

        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.COMPANY, TsidMapper.toInternal(id)))
                .singleElement()
                .satisfies(e -> {
                    assertThat(e.getAction()).isEqualTo("CREATE");
                    assertThat(e.getCreatedBy()).isEqualTo("operatore.cc");
                    assertThat(e.getChanges()).containsKeys("name", "vatCode", "contacts");
                    // referenti e recapiti: solo "campo impostato", mai i valori (EN-4, US-510)
                    assertThat(e.getChanges()).containsEntry("contacts", Map.of("redacted", true))
                            .containsEntry("phone", Map.of("redacted", true))
                            .containsEntry("email", Map.of("redacted", true));
                    assertThat(e.getChanges().toString()).doesNotContain("Paola Neri", "055 123456", "info@azienda.test");
                });
    }

    @Test
    void sedeLegaleConIlComune_auditConIValori_versioneChiusaNonSiSceglie() throws Exception {
        Municipality firenze = scenarios.municipality("Firenze", "FI");
        Municipality sesto = scenarios.municipality("Sesto Fiorentino", "FI");
        em.flush();
        String firenzeId = TsidMapper.toExternal(firenze.getId());
        String id = JsonPath.read(create("operatore.cc", body("Officina Rossi", "12345678901",
                        ", \"legalMunicipalityId\": \"%s\"".formatted(firenzeId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.legalMunicipality.name").value("Firenze"))
                .andExpect(jsonPath("$.legalMunicipality.provinceAbbreviation").value("FI"))
                .andReturn().getResponse().getContentAsString(), "$.id");
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.COMPANY,
                TsidMapper.toInternal(id)).getFirst().getChanges().get("legalMunicipality"))
                .extracting("after").isEqualTo("Firenze (FI)");
        mockMvc.perform(get(API).header(USER, "operatore.cc").param("search", "Officina"))
                .andExpect(jsonPath("$.companies[0].legalMunicipality.name").value("Firenze"));

        // la versione salvata resta anche se chiusa; una versione chiusa non si sceglie di nuovo
        firenze.close(LocalDate.of(2026, 6, 1));
        sesto.close(LocalDate.of(2026, 6, 1));
        newRequest();
        mockMvc.perform(put(API + "/" + id).header(USER, "operatore.cc").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Officina Rossi", "12345678901",
                                ", \"legalMunicipalityId\": \"%s\", \"version\": 0".formatted(firenzeId))))
                .andExpect(status().isOk());
        mockMvc.perform(put(API + "/" + id).header(USER, "operatore.cc").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Officina Rossi", "12345678901", ", \"legalMunicipalityId\": \"%s\", \"version\": 0"
                                .formatted(TsidMapper.toExternal(sesto.getId())))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("legalMunicipalityId"));

        // senza comune: tolto, e l'audit registra il cambio
        mockMvc.perform(put(API + "/" + id).header(USER, "operatore.cc").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Officina Rossi", "12345678901", ", \"version\": 0")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.legalMunicipality").doesNotExist());
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.COMPANY,
                TsidMapper.toInternal(id)).getLast().getChanges())
                .containsOnlyKeys("legalMunicipality");
    }

    @Test
    void partitaIvaDuplicataONonValida_400SulCampo() throws Exception {
        createdId("Prima", "12345678901");
        create("operatore.cc", body("Seconda", "12345678901", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("vatCode"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Esiste già un'azienda con questa partita IVA"));
        create("operatore.cc", body("Terza", "1234ABC", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("vatCode"));
    }

    @Test
    void ricercaPerNomeOPartitaIva_disattivateSoloSeRichieste() throws Exception {
        createdId("Panificio Bianchi", "11111111111");
        String id = createdId("Bianchi Logistica", "22222222222");
        createdId("Officina Verdi", "33333333333");
        mockMvc.perform(post(API + "/" + id + "/deactivate").header(USER, "operatore.cc")
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\": 0}")).andExpect(status().isOk());

        mockMvc.perform(get(API).param("search", "BIANCHI").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$.companies[*].name", contains("Panificio Bianchi")));
        mockMvc.perform(get(API).param("search", "bianchi").param("includeInactive", "true").header(USER, "admin"))
                .andExpect(jsonPath("$.companies[*].name", contains("Bianchi Logistica", "Panificio Bianchi")));
        mockMvc.perform(get(API).param("search", "3333").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$.companies[*].name", contains("Officina Verdi")));
    }

    @Test
    void elencoAPagine_totaleEOrdinePerNome_dimensioniAmmesse() throws Exception {
        // inserite al contrario: l'ordine per nome vale su tutte le pagine (US-513)
        for (int i = 12; i >= 1; i--) {
            Company company = TestEntities.company(DomainFixtures.vatCode());
            company.setName("Paginata %02d".formatted(i));
            companies.saveAndFlush(company);
        }

        mockMvc.perform(get(API).param("search", "paginata").param("size", "10").header(USER, "operatore.cc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(12))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.companies", hasSize(10)))
                .andExpect(jsonPath("$.companies[0].name").value("Paginata 01"))
                .andExpect(jsonPath("$.companies[9].name").value("Paginata 10"));
        mockMvc.perform(get(API).param("search", "paginata").param("size", "10").param("page", "1")
                        .header(USER, "operatore.cc"))
                .andExpect(jsonPath("$.total").value(12))
                .andExpect(jsonPath("$.companies[*].name", contains("Paginata 11", "Paginata 12")));
        // oltre l'ultima pagina: nessuna azienda, ma il totale c'è
        mockMvc.perform(get(API).param("search", "paginata").param("size", "10").param("page", "5")
                        .header(USER, "operatore.cc"))
                .andExpect(jsonPath("$.companies").isEmpty())
                .andExpect(jsonPath("$.total").value(12));
        // senza indicazioni: la prima pagina da 25
        mockMvc.perform(get(API).param("search", "paginata").header(USER, "admin"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(25))
                .andExpect(jsonPath("$.companies", hasSize(12)));
        // solo 10, 25, 50 o 100 per pagina; niente pagine negative
        for (String size : new String[] {"0", "1", "26", "1000"}) {
            mockMvc.perform(get(API).param("size", size).header(USER, "operatore.cc"))
                    .andExpect(status().isBadRequest());
        }
        mockMvc.perform(get(API).param("page", "-1").header(USER, "operatore.cc")).andExpect(status().isBadRequest());
        mockMvc.perform(get(API).param("size", "100").header(USER, "operatore.cc")).andExpect(status().isOk());
    }

    @Test
    void modifica_soloCampiCambiatiNellAudit_versioneSuperata409() throws Exception {
        String id = createdId("Officina Rossi", "12345678901");
        mockMvc.perform(put(API + "/" + id).header(USER, "operatore.cc").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Officina Rossi S.r.l.", "12345678901", ", \"version\": 0")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Officina Rossi S.r.l."))
                .andExpect(jsonPath("$.version").value(1));

        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.COMPANY, TsidMapper.toInternal(id)))
                .last()
                .satisfies(e -> {
                    assertThat(e.getAction()).isEqualTo("UPDATE");
                    assertThat(e.getChanges()).containsOnlyKeys("name")
                            .containsEntry("name", Map.of("before", "Officina Rossi", "after", "Officina Rossi S.r.l."));
                });

        mockMvc.perform(put(API + "/" + id).header(USER, "admin").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Altro nome", "12345678901", ", \"version\": 0")))
                .andExpect(status().isConflict());
    }

    @Test
    void modificaDelReferente_auditSenzaValori() throws Exception {
        String id = createdId("Officina Rossi", "12345678901");
        mockMvc.perform(put(API + "/" + id).header(USER, "operatore.cc").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Officina Rossi", "12345678901", ", \"version\": 0")
                                .replace("Paola Neri", "Marco Gialli")))
                .andExpect(status().isOk());

        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.COMPANY, TsidMapper.toInternal(id)))
                .last()
                .satisfies(e -> {
                    assertThat(e.getAction()).isEqualTo("UPDATE");
                    assertThat(e.getChanges()).containsOnlyKeys("contacts")
                            .containsEntry("contacts", Map.of("redacted", true));
                    assertThat(e.getChanges().toString()).doesNotContain("Paola Neri", "Marco Gialli");
                });
    }

    @Test
    void modificaConPartitaIvaDiUnAltra_400() throws Exception {
        createdId("Prima", "11111111111");
        String id = createdId("Seconda", "22222222222");
        mockMvc.perform(put(API + "/" + id).header(USER, "operatore.cc").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Seconda", "11111111111", ", \"version\": 0")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("vatCode"));
    }

    /** Azienda come arriverà dal database attuale (US-1309): i segnaposto li scrive solo l'importazione. */
    private Company imported(String name) {
        Company company = TestEntities.company(CompanyPlaceholders.VAT_CODE);
        company.setName(name);
        return companies.saveAndFlush(company);
    }

    @Test
    void segnapostoDelDatabaseAttuale_nonSiSalvano_400SuiCampi() throws Exception {
        long before = companies.count();
        create("operatore.cc", body("  azienda SENZA nome n. 12", " 00000000000 ", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field", containsInAnyOrder("name", "vatCode")))
                .andExpect(jsonPath("$.fieldErrors[*].message", containsInAnyOrder(
                        "Inserisci la ragione sociale vera: «Azienda senza nome n. …» vuol dire che manca",
                        "Inserisci la partita IVA vera: «00000000000» vuol dire che manca")));
        assertThat(companies.count()).isEqualTo(before);

        String id = TsidMapper.toExternal(imported(CompanyPlaceholders.name(1234)).getId());
        mockMvc.perform(put(API + "/" + id).header(USER, "operatore.cc").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Azienda senza nome n. 1234", "00000000000", ", \"version\": 0")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field", containsInAnyOrder("name", "vatCode")));
    }

    @Test
    void aziendeDaCompletare_datiMancanti_filtro_eCompletamento() throws Exception {
        Company noName = imported(CompanyPlaceholders.name(1234));
        imported("Bar Centrale");
        createdId("Bar Completo", "12345678901");

        mockMvc.perform(get(API).param("search", "bar").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$.companies[*].name", contains("Bar Centrale", "Bar Completo")))
                .andExpect(jsonPath("$.companies[0].missingData", contains("VAT_CODE")))
                .andExpect(jsonPath("$.companies[1].missingData").isEmpty());
        mockMvc.perform(get(API).param("toComplete", "true").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$.companies[*].name", contains("Azienda senza nome n. 1234", "Bar Centrale")))
                .andExpect(jsonPath("$.companies[0].missingData", contains("NAME", "VAT_CODE")));

        String id = TsidMapper.toExternal(noName.getId());
        mockMvc.perform(put(API + "/" + id).header(USER, "operatore.cc").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Trattoria da Mario", "98765432109", ", \"version\": 0")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missingData").isEmpty());
        mockMvc.perform(get(API).param("toComplete", "true").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$.companies[*].name", contains("Bar Centrale")));
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.COMPANY, noName.getId()))
                .last()
                .satisfies(e -> assertThat(e.getChanges())
                        .containsEntry("name", Map.of("before", "Azienda senza nome n. 1234", "after", "Trattoria da Mario"))
                        .containsEntry("vatCode", Map.of("before", "00000000000", "after", "98765432109")));
    }

    private static String category(int legacyNumber) {
        return TsidMapper.toExternal(BusinessCategory.LEGACY_ID_BASE + legacyNumber);
    }

    @Test
    void categoriaEAttivita_audit_filtro_categoriaDisattivataRestaSoloSullAzienda() throws Exception {
        String ristorante = category(11);
        String bar = category(14);
        String id = JsonPath.read(create("operatore.cc", body("Pizzeria Napoli", "12345678901",
                        ", \"businessCategoryId\": \"" + ristorante + "\", \"activity\": \" Pizzeria \""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.businessCategory.name").value("Ristorante / Trattoria / Bistrot"))
                .andExpect(jsonPath("$.activity").value("Pizzeria"))
                .andReturn().getResponse().getContentAsString(), "$.id");
        createdId("Bar Sport", "22222222222");
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.COMPANY, TsidMapper.toInternal(id)))
                .singleElement()
                .satisfies(e -> {
                    assertThat(e.getChanges().get("businessCategory")).asInstanceOf(InstanceOfAssertFactories.MAP)
                            .containsEntry("after", "Ristorante / Trattoria / Bistrot");
                    assertThat(e.getChanges().get("activity")).asInstanceOf(InstanceOfAssertFactories.MAP)
                            .containsEntry("after", "Pizzeria");
                });

        mockMvc.perform(get(API).param("businessCategoryId", ristorante).header(USER, "operatore.cc"))
                .andExpect(jsonPath("$.companies[*].name", contains("Pizzeria Napoli")));
        mockMvc.perform(get(API).param("businessCategoryId", "non-un-id").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$.companies").isEmpty()).andExpect(jsonPath("$.total").value(0));

        // categoria disattivata dall'ADMIN: resta sull'azienda che l'ha, non si sceglie per le altre
        mockMvc.perform(post("/api/v1/catalogs/business-categories/" + ristorante + "/deactivate").header(USER, "admin"))
                .andExpect(status().isOk());
        mockMvc.perform(put(API + "/" + id).header(USER, "operatore.cc").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Pizzeria Napoli S.n.c.", "12345678901",
                                ", \"businessCategoryId\": \"" + ristorante + "\", \"activity\": \"Pizzeria\", \"version\": 0")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessCategory.name").value("Ristorante / Trattoria / Bistrot"));
        create("operatore.cc", body("Trattoria Nuova", "33333333333", ", \"businessCategoryId\": \"" + ristorante + "\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("businessCategoryId"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Categoria non valida"));

        mockMvc.perform(put(API + "/" + id).header(USER, "operatore.cc").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Pizzeria Napoli S.n.c.", "12345678901",
                                ", \"businessCategoryId\": \"" + bar + "\", \"activity\": \"Pizzeria e bar\", \"version\": 1")))
                .andExpect(status().isOk());
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.COMPANY, TsidMapper.toInternal(id)))
                .last()
                .satisfies(e -> assertThat(e.getChanges()).containsOnlyKeys("businessCategory", "activity")
                        .containsEntry("businessCategory",
                                Map.of("before", "Ristorante / Trattoria / Bistrot", "after", "Bar / Pub"))
                        .containsEntry("activity", Map.of("before", "Pizzeria", "after", "Pizzeria e bar")));
    }

    private static String role(int legacyNumber) {
        return TsidMapper.toExternal(ContactRole.LEGACY_ID_BASE + legacyNumber);
    }

    /** Come {@link #body}, ma con i referenti indicati (JSON di un array). */
    private static String withContacts(String name, String vat, String contacts, String extra) {
        return """
                {"name": "%s", "vatCode": "%s", "contacts": %s %s}
                """.formatted(name, vat, contacts, extra);
    }

    /**
     * La richiesta che segue parte da un contesto di persistenza nuovo, come nell'applicazione: nel contesto condiviso
     * del test un'azienda appena creata con i referenti verrebbe riscritta al salvataggio successivo, con la versione
     * incrementata anche senza modifiche.
     */
    private void newRequest() {
        em.flush();
        em.clear();
    }

    private ResultActions update(String id, String json) throws Exception {
        return mockMvc.perform(put(API + "/" + id).header(USER, "operatore.cc").contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private List<AuditEvent> companyAudit(String id) {
        return audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.COMPANY,
                TsidMapper.toInternal(id));
    }

    @Test
    void referentiConIlRuolo_inOrdine_auditSenzaValori() throws Exception {
        String contacts = """
                [{"name": " Paola Neri ", "roleId": "%s", "mobile": "+39 333 1234567"},
                 {"roleId": "%s", "email": "personale@azienda.test"}]""".formatted(role(13), role(3));
        String id = JsonPath.read(create("operatore.cc", withContacts("Hotel Bellavista", "12345678901", contacts, ""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contacts.length()").value(2))
                .andExpect(jsonPath("$.contacts[0].name").value("Paola Neri"))
                .andExpect(jsonPath("$.contacts[0].role.name").value("Titolare"))
                .andExpect(jsonPath("$.contacts[0].mobile").value("+39 333 1234567"))
                .andExpect(jsonPath("$.contacts[1].name").doesNotExist())
                .andExpect(jsonPath("$.contacts[1].role.name").value("Responsabile del personale"))
                .andExpect(jsonPath("$.contacts[1].email").value("personale@azienda.test"))
                .andReturn().getResponse().getContentAsString(), "$.id");
        mockMvc.perform(get(API).param("search", "Bellavista").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$.companies[0].contacts[*].role.name", contains("Titolare", "Responsabile del personale")));
        assertThat(companyAudit(id)).singleElement().satisfies(e -> {
            assertThat(e.getChanges()).containsEntry("contacts", Map.of("redacted", true));
            assertThat(e.getChanges().toString()).doesNotContain("Paola", "333 1234567", "personale@");
        });

        newRequest();
        // salvare senza cambiare nulla non tocca versione e audit
        update(id, withContacts("Hotel Bellavista", "12345678901", contacts, ", \"version\": 0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(0));
        assertThat(companyAudit(id)).hasSize(1);

        // cambia solo l'ordine: è una modifica dei referenti, che incrementa la versione
        String swapped = """
                [{"roleId": "%s", "email": "personale@azienda.test"},
                 {"name": "Paola Neri", "roleId": "%s", "mobile": "+39 333 1234567"}]""".formatted(role(3), role(13));
        String json = withContacts("Hotel Bellavista", "12345678901", swapped, ", \"version\": 0");
        update(id, json)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(greaterThan(0)))
                .andExpect(jsonPath("$.contacts[0].role.name").value("Responsabile del personale"))
                .andExpect(jsonPath("$.contacts[1].name").value("Paola Neri"));
        assertThat(companyAudit(id)).last().satisfies(e -> assertThat(e.getChanges()).containsOnlyKeys("contacts")
                .containsEntry("contacts", Map.of("redacted", true)));
        // un collega che partiva dalla versione di prima riceve il 409
        update(id, json).andExpect(status().isConflict());
    }

    @Test
    void referenteSenzaNomeNeRuolo_ruoloNonValido_troppiReferenti_400() throws Exception {
        create("operatore.cc", withContacts("Officina Rossi", "12345678901",
                        "[{\"name\": \"Paola Neri\"}, {\"phone\": \"055 1234\"}]", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("contacts[1].name"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Inserisci il nome o scegli il ruolo"));
        create("operatore.cc", withContacts("Officina Rossi", "12345678901",
                        "[{\"name\": \"Paola Neri\", \"roleId\": \"non-un-id\"}]", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("contacts[0].roleId"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Ruolo non valido"));
        create("operatore.cc", withContacts("Officina Rossi", "12345678901",
                        "[{\"name\": \"Paola Neri\", \"email\": \"non-una-email\"}]", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("contacts[0].email"));
        String eleven = "[" + String.join(", ", Collections.nCopies(11, "{\"name\": \"Referente\"}")) + "]";
        create("operatore.cc", withContacts("Officina Rossi", "12345678901", eleven, ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("contacts"));
        assertThat(companies.findByVatCode("12345678901")).isEmpty();
    }

    @Test
    void ruoloDisattivato_restaSuiReferentiDellAzienda_nonSiScegliePerUnAltra() throws Exception {
        String partner = "[{\"name\": \"Paola Neri\", \"roleId\": \"%s\"}]".formatted(role(24));
        String id = JsonPath.read(create("operatore.cc", withContacts("Officina Rossi", "12345678901", partner, ""))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id");

        mockMvc.perform(post("/api/v1/catalogs/contact-roles/" + role(24) + "/deactivate").header(USER, "admin"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/reference/contact-roles").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$[*].name", not(hasItem("Socio"))));
        update(id, withContacts("Officina Rossi S.r.l.", "12345678901", partner, ", \"version\": 0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contacts[0].role.name").value("Socio"));
        create("operatore.cc", withContacts("Officina Bianchi", "22222222222", partner, ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("contacts[0].roleId"));
    }

    @Test
    void recapitiESedeOperativa_auditConIValoriSoloPerLaSede() throws Exception {
        Municipality prato = scenarios.municipality("Prato", "PO");
        em.flush();
        String extra = """
                , "mobile": "+39 333 7654321", "fax": "055 765432", "secondaryEmail": "ordini@azienda.test",
                  "operatingAddress": " Via dei Mille 3 ", "operatingMunicipalityId": "%s"
                """.formatted(TsidMapper.toExternal(prato.getId()));
        String id = JsonPath.read(create("operatore.cc", body("Officina Rossi", "12345678901", extra))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mobile").value("+39 333 7654321"))
                .andExpect(jsonPath("$.fax").value("055 765432"))
                .andExpect(jsonPath("$.secondaryEmail").value("ordini@azienda.test"))
                .andExpect(jsonPath("$.operatingAddress").value("Via dei Mille 3"))
                .andExpect(jsonPath("$.operatingMunicipality.name").value("Prato"))
                .andExpect(jsonPath("$.operatingMunicipality.provinceAbbreviation").value("PO"))
                .andReturn().getResponse().getContentAsString(), "$.id");
        assertThat(companyAudit(id)).singleElement().satisfies(e -> {
            assertThat(e.getChanges()).containsEntry("mobile", Map.of("redacted", true))
                    .containsEntry("fax", Map.of("redacted", true))
                    .containsEntry("secondaryEmail", Map.of("redacted", true));
            assertThat(e.getChanges().get("operatingAddress")).asInstanceOf(InstanceOfAssertFactories.MAP)
                    .containsEntry("after", "Via dei Mille 3");
            assertThat(e.getChanges().get("operatingMunicipality")).asInstanceOf(InstanceOfAssertFactories.MAP)
                    .containsEntry("after", "Prato (PO)");
            assertThat(e.getChanges().toString()).doesNotContain("7654321", "765432", "ordini@");
        });

        update(id, body("Officina Rossi", "12345678901", ", \"fax\": \"055 ABC\", \"version\": 0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("fax"));
        update(id, body("Officina Rossi", "12345678901", ", \"operatingMunicipalityId\": \"non-un-id\", \"version\": 0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("operatingMunicipalityId"));

        // tutto tolto: la sede con i valori prima e dopo, i recapiti solo come cambiati
        update(id, body("Officina Rossi", "12345678901", ", \"version\": 0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operatingMunicipality").doesNotExist())
                .andExpect(jsonPath("$.mobile").doesNotExist());
        assertThat(companyAudit(id)).last().satisfies(e -> {
            assertThat(e.getChanges())
                    .containsOnlyKeys("mobile", "fax", "secondaryEmail", "operatingAddress", "operatingMunicipality")
                    .containsEntry("fax", Map.of("redacted", true));
            assertThat(e.getChanges().get("operatingMunicipality")).asInstanceOf(InstanceOfAssertFactories.MAP)
                    .containsEntry("before", "Prato (PO)");
        });
    }

    @Test
    void conMansioneBloccata_nonSiDisattiva_eIlMessaggioDiceQuale() throws Exception {
        String id = createdId("Officina Rossi", "12345678901");
        Company company = companies.findById(TsidMapper.toInternal(id)).orElseThrow();
        AppUser tutor = users.findByExternalId("00000000-0000-4000-8000-000000000001").orElseThrow();
        Zone zone = zones.save(TestEntities.zone("ZBLOCCO"));
        JobCategory category = categories.save(TestEntities.jobCategory("CBLOCCO"));
        Ticket ticket = tickets.save(TestEntities.ticket(tutor, projects.save(TestEntities.project("PBLOCCO")),
                beneficiaries.save(TestEntities.beneficiary(tutor, zone)), category));
        JobSlot slot = TestEntities.jobSlot(company, category, zone);
        slot.setTitle("Magazziniere");
        slot.blockFor(ticket);
        jobSlots.saveAndFlush(slot);
        em.refresh(ticket);

        mockMvc.perform(post(API + "/" + id + "/deactivate").header(USER, "operatore.cc")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"version\": " + company.getVersion() + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Non si può disattivare l'azienda: la mansione «Magazziniere» è "
                        + "assegnata alla segnalazione n. " + ticket.getNumber() + " (tipo non indicato)"));
        assertThat(companies.findById(company.getId()).orElseThrow().isActive()).isTrue();
    }

    @Test
    void disattivazioneERiattivazione_tracciate() throws Exception {
        String id = createdId("Officina Rossi", "12345678901");
        mockMvc.perform(post(API + "/" + id + "/deactivate").header(USER, "operatore.cc")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"version\": 0}"))
                .andExpect(jsonPath("$.active").value(false));
        // con la ricerca: l'elenco è a pagine e senza la ricerca l'azienda potrebbe essere in un'altra (US-513)
        mockMvc.perform(get(API).param("search", "Officina Rossi").header(USER, "operatore.cc"))
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.companies[*].id", not(hasItem(id))));
        mockMvc.perform(get(API).param("search", "Officina Rossi").param("includeInactive", "true")
                        .header(USER, "operatore.cc"))
                .andExpect(jsonPath("$.companies[*].id", contains(id)));
        mockMvc.perform(post(API + "/" + id + "/activate").header(USER, "operatore.cc")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"version\": 1}"))
                .andExpect(jsonPath("$.active").value(true));

        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.COMPANY, TsidMapper.toInternal(id)))
                .extracting(e -> e.getAction()).containsExactly("CREATE", "DEACTIVATE", "ACTIVATE");
    }

    @Test
    void tutor403_inesistente404() throws Exception {
        create("tutor1", body("X", "12345678901", "")).andExpect(status().isForbidden());
        mockMvc.perform(get(API).header(USER, "tutor1")).andExpect(status().isForbidden());
        mockMvc.perform(get(API + "/0000000000000").header(USER, "operatore.cc")).andExpect(status().isNotFound());
    }
}
