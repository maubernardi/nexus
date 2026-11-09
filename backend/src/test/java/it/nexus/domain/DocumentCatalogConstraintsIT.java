package it.nexus.domain;

import static it.nexus.domain.DomainConstraintsIT.assertViolates;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.TestcontainersConfiguration;

/** Vincoli dei cataloghi per i documenti (US-115), verificati per nome su PostgreSQL. */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class DocumentCatalogConstraintsIT {

    private static final String AGENCY = "INSERT INTO care_agency (id, code, name, kind, fiscal_code, created_at, "
            + "created_by, updated_at, updated_by) VALUES (?, ?, ?, ?, ?, now(), 't', now(), 't')";

    @Autowired JdbcTemplate jdbc;

    @Test
    void tipologiaDellEnteNonAmmessa() {
        assertViolates("ck_care_agency_kind", () -> jdbc.update(AGENCY, 1L, "ENTE_X", "Ente X", "SCUOLA", null));
    }

    @Test
    void codiceFiscaleDellEnteFuoriFormato() {
        assertViolates("ck_care_agency_fiscal_code",
                () -> jdbc.update(AGENCY, 1L, "ENTE_X", "Ente X", "COMUNE", "123"));
    }

    @Test
    void nomeDiEnteAttivoDuplicato() {
        assertViolates("uq_care_agency_active_name",
                () -> jdbc.update(AGENCY, 1L, "ENTE_X", "comune di firenze", "COMUNE", null));
    }

    @Test
    void nomeDiSedeAttivaDuplicato() {
        assertViolates("uq_competence_site_active_name", () -> jdbc.update(
                "INSERT INTO competence_site (id, code, name, created_at, created_by, updated_at, updated_by) "
                        + "VALUES (1, 'FIRENZE_BIS', 'FIRENZE', now(), 't', now(), 't')"));
    }
}
