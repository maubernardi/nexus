package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import it.nexus.domain.BusinessCategory;
import it.nexus.domain.Company;
import it.nexus.domain.CompanyPlaceholders;
import it.nexus.domain.TestEntities;
import it.nexus.domain.Zone;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.Role;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.AuditEventRepository;
import it.nexus.repository.BusinessCategoryRepository;
import it.nexus.repository.CompanyRepository;
import it.nexus.repository.ZoneRepository;
import jakarta.persistence.EntityManager;

/**
 * Richieste di contatto di un'azienda dal Tutor al Call Center (US-507): invio solo per un'azienda non ancora in
 * anagrafica, nomi simili, elenco del Call Center con il numero delle aperte, presa in carico, «Le mie richieste», audit.
 */
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class ContactRequestResourceIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;
    private static final String API = "/api/v1/contact-requests";

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired AppUserRepository users;
    @Autowired CompanyRepository companies;
    @Autowired ZoneRepository zones;
    @Autowired BusinessCategoryRepository businessCategories;
    @Autowired AuditEventRepository audit;

    private Zone zone;
    private BusinessCategory category;
    private Company ristorazione;

    @BeforeEach
    void setUp() {
        users.save(named(TestEntities.user("tutor1", Role.TUTOR, "00000000-0000-4000-8000-000000000001"), "Tina",
                "Tutor"));
        users.save(named(TestEntities.user("tutor2", Role.TUTOR, "00000000-0000-4000-8000-000000000002"), "Ugo",
                "Tutor"));
        users.save(named(TestEntities.user("operatore.cc", Role.CALL_CENTER, "00000000-0000-4000-8000-000000000003"),
                "Olga", "Operatrice"));
        users.save(named(TestEntities.user("admin", Role.ADMIN, "00000000-0000-4000-8000-000000000004"), "Ada",
                "Admin"));
        zone = zones.save(TestEntities.zone("Z507"));
        category = businessCategories.findAll().stream().filter(BusinessCategory::isActive).findFirst().orElseThrow();
        Company c = TestEntities.company("99050700001");
        c.setName("Trattoria Bianchi S.n.c.");
        c.setZone(zone);
        ristorazione = companies.save(c);
    }

    private static AppUser named(AppUser user, String first, String last) {
        user.setFirstName(first);
        user.setLastName(last);
        return user;
    }

    private ResultActions send(String user, String json) throws Exception {
        return mockMvc.perform(post(API).header(USER, user).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private String newCompany(String name) {
        return """
                {"companyName": "%s", "zoneId": "%s", "businessCategoryId": "%s"}"""
                .formatted(name, TsidMapper.toExternal(zone.getId()), TsidMapper.toExternal(category.getId()));
    }

    private ResultActions take(String user, String id, long version) throws Exception {
        return mockMvc.perform(post(API + "/" + id + "/take").header(USER, user)
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\": %d}".formatted(version)));
    }

    @Test
    void aziendaNuova_richiestaAperta_numerata_conLAudit() throws Exception {
        String json = send("tutor1", newCompany("  Officina del Ponte  "))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.number").isNumber())
                .andExpect(jsonPath("$.status").value("APERTA"))
                .andExpect(jsonPath("$.companyName").value("Officina del Ponte"))
                // l'azienda dell'anagrafica la collega l'operatore alla chiusura (US-512)
                .andExpect(jsonPath("$.companyId").doesNotExist())
                .andExpect(jsonPath("$.zone.code").value("Z507"))
                .andExpect(jsonPath("$.businessCategory.code").value(category.getCode()))
                .andExpect(jsonPath("$.requestedByName").value("Tina Tutor"))
                .andExpect(jsonPath("$.assignedOperatorName").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(json, "$.id");
        int number = JsonPath.read(json, "$.number");
        // il numero successivo è il seguente
        send("tutor1", newCompany("Bar Centrale")).andExpect(jsonPath("$.number").value(number + 1));

        em.flush();
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.CONTACT_REQUEST,
                TsidMapper.toInternal(id))).singleElement().satisfies(e -> {
                    assertThat(e.getAction()).isEqualTo("CREATE");
                    assertThat(e.getCreatedBy()).isEqualTo("tutor1");
                    assertThat(e.getChanges().get("companyName")).asInstanceOf(InstanceOfAssertFactories.MAP)
                            .containsEntry("after", "Officina del Ponte");
                    assertThat(e.getChanges().get("zone")).asInstanceOf(InstanceOfAssertFactories.MAP)
                            .containsEntry("after", "Z507");
                });
    }

    @Test
    void soloAziendeNonInAnagrafica_lAziendaPresenteNonSiChiede() throws Exception {
        // PO, 27/11: il contatto si chiede solo per le aziende che NEXUS non ha; l'azienda dell'anagrafica non basta
        send("tutor1", "{\"companyId\": \"%s\"}".formatted(TsidMapper.toExternal(ristorazione.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("companyName"));
    }

    @Test
    void datiMancantiONonValidi_400SulCampo() throws Exception {
        send("tutor1", "{\"zoneId\": \"%s\"}".formatted(TsidMapper.toExternal(zone.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("companyName"));
        send("tutor1", "{\"companyName\": \"Officina\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("zoneId"));
        Zone inactive = TestEntities.zone("Z507X");
        inactive.setActive(false);
        zones.save(inactive);
        send("tutor1", "{\"companyName\": \"Officina\", \"zoneId\": \"%s\"}"
                .formatted(TsidMapper.toExternal(inactive.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("zoneId"));
        send("tutor1", "{\"companyName\": \"%s\", \"zoneId\": \"%s\"}"
                .formatted("x".repeat(201), TsidMapper.toExternal(zone.getId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void soloIlTutorInvia_soloCallCenterEAdminVedonoLElenco() throws Exception {
        send("operatore.cc", newCompany("Officina")).andExpect(status().isForbidden());
        send("admin", newCompany("Officina")).andExpect(status().isForbidden());
        mockMvc.perform(get(API).header(USER, "tutor1")).andExpect(status().isForbidden());
        mockMvc.perform(get(API + "/open-count").header(USER, "tutor1")).andExpect(status().isForbidden());
        mockMvc.perform(get(API + "/mine").header(USER, "operatore.cc")).andExpect(status().isForbidden());
        mockMvc.perform(get(API)).andExpect(status().isUnauthorized());
    }

    @Test
    void elencoConLeAperteInCima_numeroDelleAperte_presaInCarico_leMieRichieste() throws Exception {
        String first = JsonPath.read(send("tutor1", newCompany("Officina del Ponte"))
                .andReturn().getResponse().getContentAsString(), "$.id");
        send("tutor2", newCompany("Trattoria Bianchi Due")).andExpect(status().isCreated());
        send("tutor1", newCompany("Bar Centrale")).andExpect(status().isCreated());

        mockMvc.perform(get(API).header(USER, "operatore.cc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].companyName",
                        contains("Officina del Ponte", "Trattoria Bianchi Due", "Bar Centrale")))
                .andExpect(jsonPath("$[1].requestedByName").value("Ugo Tutor"));
        mockMvc.perform(get(API + "/open-count").header(USER, "operatore.cc")).andExpect(jsonPath("$").value(3));

        take("operatore.cc", first, 0)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_CARICO"))
                .andExpect(jsonPath("$.assignedOperatorName").value("Olga Operatrice"))
                .andExpect(jsonPath("$.takenAt").exists());
        mockMvc.perform(get(API + "/open-count").header(USER, "admin")).andExpect(jsonPath("$").value(2));
        // in cima quelle da prendere in carico, dalla più vecchia (PO, 27/11); quella in carico resta, in fondo
        mockMvc.perform(get(API).header(USER, "admin"))
                .andExpect(jsonPath("$[*].companyName",
                        contains("Trattoria Bianchi Due", "Bar Centrale", "Officina del Ponte")))
                .andExpect(jsonPath("$[*].status", contains("APERTA", "APERTA", "IN_CARICO")))
                .andExpect(jsonPath("$[2].assignedOperatorName").value("Olga Operatrice"));

        // già presa: avviso con il nome di chi ce l'ha
        take("admin", first, 1)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("già in carico a Olga Operatrice")));

        // il Tutor vede le sue, dalla più recente, con lo stato e l'operatore; mai quelle degli altri
        mockMvc.perform(get(API + "/mine").header(USER, "tutor1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].companyName", contains("Bar Centrale", "Officina del Ponte")))
                .andExpect(jsonPath("$[1].status").value("IN_CARICO"))
                .andExpect(jsonPath("$[1].assignedOperatorName").value("Olga Operatrice"))
                .andExpect(jsonPath("$[*].companyName", not(hasItem("Trattoria Bianchi Due"))));

        em.flush();
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.CONTACT_REQUEST,
                TsidMapper.toInternal(first))).extracting(e -> e.getAction() + " " + e.getCreatedBy())
                .containsExactly("CREATE tutor1", "TAKE operatore.cc");
    }

    @Test
    void versioneVecchiaOInesistente() throws Exception {
        String id = JsonPath.read(send("tutor1", newCompany("Officina del Ponte"))
                .andReturn().getResponse().getContentAsString(), "$.id");
        take("operatore.cc", id, 7).andExpect(status().isConflict());
        take("operatore.cc", "0000000000000", 0).andExpect(status().isNotFound());
    }

    @Test
    void nomiSimili_soloAttive_conIDatiEssenziali() throws Exception {
        Company pizzeria = TestEntities.company("99050700003");
        pizzeria.setName("Pizzeria da Mario S.r.l.");
        companies.save(pizzeria);
        Company closed = TestEntities.company("99050700004");
        closed.setName("Trattoria Bianchi Due");
        closed.setActive(false);
        companies.save(closed);
        Company placeholder = TestEntities.company(CompanyPlaceholders.VAT_CODE);
        placeholder.setName(CompanyPlaceholders.NAME_PREFIX + "90507");
        companies.save(placeholder);
        em.flush();

        // con una piccola differenza di scrittura, e il nome scritto dentro quello dell'azienda
        String json = mockMvc.perform(get("/api/v1/companies/directory/similar").param("name", "trattoria bianki")
                        .header(USER, "tutor1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", contains("Trattoria Bianchi S.n.c.")))
                .andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> items = JsonPath.read(json, "$");
        assertThat(items.getFirst().keySet()).doesNotContain("vatCode", "phone", "email", "contacts");
        mockMvc.perform(get("/api/v1/companies/directory/similar").param("name", "Pizzeria Mario")
                        .header(USER, "tutor1"))
                .andExpect(jsonPath("$[*].name", contains("Pizzeria da Mario S.r.l.")));
        // nessun nome simile, o nome troppo corto
        mockMvc.perform(get("/api/v1/companies/directory/similar").param("name", "Ferramenta Lunghi")
                        .header(USER, "tutor1"))
                .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/api/v1/companies/directory/similar").param("name", "ab").header(USER, "tutor1"))
                .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/api/v1/companies/directory/similar").param("name", "Trattoria")
                        .header(USER, "operatore.cc"))
                .andExpect(status().isForbidden());
    }
}
