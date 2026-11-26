package it.nexus.legacy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * V52 (US-514): i simboli del database attuale all'inizio dei nomi diventano stelle e pollici, il nome si ripulisce e
 * ogni azienda convertita ha la sua voce nell'audit. Gli stessi esempi del test della regola Java dell'importazione.
 */
@Testcontainers
class LegacyCompanyMarksMigrationIT {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(DockerImageName.parse("postgres:19.0-alpine"));

    private static final String PLACEHOLDER_VAT = "00000000000";

    private static Flyway flyway(String target) {
        return Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .schemas("nexus")
                .defaultSchema("nexus")
                .cleanDisabled(false)
                .locations("classpath:db/migration")
                .target(target)
                .load();
    }

    private static JdbcTemplate jdbc() {
        // ogni comando apre una connessione: lo schema va nell'indirizzo
        String url = POSTGRES.getJdbcUrl() + (POSTGRES.getJdbcUrl().contains("?") ? "&" : "?") + "currentSchema=nexus";
        return new JdbcTemplate(new DriverManagerDataSource(url, POSTGRES.getUsername(), POSTGRES.getPassword()));
    }

    private static void insert(JdbcTemplate jdbc, long id, String name) {
        jdbc.update("INSERT INTO company (id, name, vat_code, created_at, created_by, updated_at, updated_by) "
                + "VALUES (?, ?, ?, now(), 'test', now(), 'test')", id, name, PLACEHOLDER_VAT);
    }

    @Test
    void simboliInStelleEPollici_nomeRipulito_audit_poiIlVincolo() {
        flyway("latest").clean();
        flyway("51").migrate();
        JdbcTemplate jdbc = jdbc();
        List<LegacyMarksExamples.Example> examples = LegacyMarksExamples.NAMED;
        for (int i = 0; i < examples.size(); i++) {
            insert(jdbc, LegacyIds.company(100 + i), examples.get(i).name());
        }
        // nome fatto solo di simboli: azienda importata (n. 1234) e azienda creata a mano
        insert(jdbc, LegacyIds.company(1234), LegacyMarksExamples.ONLY_MARKS.name());
        insert(jdbc, 42L, "&");

        flyway("52").migrate();

        for (int i = 0; i < examples.size(); i++) {
            LegacyMarksExamples.Example e = examples.get(i);
            Map<String, Object> row = jdbc.queryForMap(
                    "SELECT name, reliability, project_interest, version FROM company WHERE id = ?",
                    LegacyIds.company(100 + i));
            assertThat(row.get("name")).as(e.name()).isEqualTo(e.cleanName());
            assertThat(number(row.get("reliability"))).as(e.name()).isEqualTo(e.reliability());
            assertThat(number(row.get("project_interest"))).as(e.name()).isEqualTo(e.projectInterest());
            // la versione cambia solo se l'azienda è stata convertita: un collega con la scheda aperta riceve il 409
            assertThat(number(row.get("version"))).as(e.name()).isEqualTo(e.name().equals(e.cleanName()) ? 0 : 1);
        }
        assertThat(jdbc.queryForMap("SELECT name, reliability FROM company WHERE id = ?", LegacyIds.company(1234)))
                .containsEntry("name", "Azienda senza nome n. 1234");
        assertThat(jdbc.queryForObject("SELECT name FROM company WHERE id = 42", String.class))
                .isEqualTo("Azienda senza nome n. 42");

        // una voce di audit per azienda convertita, con il nome di prima, negli id riservati
        long converted = examples.stream().filter(e -> !e.name().equals(e.cleanName())).count() + 2;
        List<Map<String, Object>> audit = jdbc.queryForList("SELECT id, entity_id, changes::text AS changes, created_by "
                + "FROM audit_event WHERE entity_type = 'COMPANY' AND action = 'LEGACY_MARKS' ORDER BY id");
        assertThat(audit).hasSize((int) converted);
        assertThat(audit).allSatisfy(a -> {
            assertThat(((Number) a.get("id")).longValue()).isGreaterThan(LegacyIds.MARKS_AUDIT);
            assertThat(a.get("created_by")).isEqualTo("database-attuale");
        });
        List<String> changes = new ArrayList<>(audit.stream().map(a -> (String) a.get("changes")).toList());
        assertThat(changes).anySatisfy(c -> assertThat(c).contains("\"before\": \"£$$ Officina Rossi\"",
                "\"after\": \"Officina Rossi\"", "\"reliability\": {\"after\": 2, \"before\": null}",
                "\"projectInterest\": {\"after\": 1, \"before\": null}"));
        assertThat(changes).anySatisfy(c -> assertThat(c).contains("\"before\": \"&Ferramenta Bianchi\"")
                .doesNotContain("reliability", "projectInterest"));

        // d'ora in poi il nome non può iniziare con i simboli
        assertThatThrownBy(() -> insert(jdbc, 43L, "$$ Nuova"))
                .isInstanceOf(DataAccessException.class)
                .rootCause().hasMessageContaining("ck_company_name_legacy_marks");
        insert(jdbc, 44L, "Nuova & Figli");
    }

    private static Integer number(Object value) {
        return value == null ? null : ((Number) value).intValue();
    }
}
