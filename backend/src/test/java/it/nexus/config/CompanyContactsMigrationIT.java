package it.nexus.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * V47 (US-510): il referente di oggi di ogni azienda diventa il primo referente, senza ruolo; la colonna
 * {@code contact_person} resta per il ripristino della release precedente (A16).
 */
@Testcontainers
class CompanyContactsMigrationIT {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(DockerImageName.parse("postgres:18.6-alpine"));

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

    @Test
    void ilReferenteDiOggiDiventaIlPrimo() {
        flyway("latest").clean();
        flyway("46").migrate();
        JdbcTemplate jdbc = new JdbcTemplate(
                new DriverManagerDataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword()));
        String insert = """
                INSERT INTO nexus.company (id, name, vat_code, contact_person, created_at, created_by, updated_at,
                                           updated_by)
                VALUES (?, ?, ?, ?, now(), 'test', now(), 'test')
                """;
        jdbc.update(insert, 1001L, "Con referente", "11111111111", "  Mario Rossi ");
        jdbc.update(insert, 1002L, "Referente vuoto", "22222222222", "   ");
        jdbc.update(insert, 1003L, "Senza referente", "33333333333", null);

        flyway("latest").migrate();

        List<Map<String, Object>> contacts = jdbc.queryForList(
                "SELECT id, company_id, position, name, role_id FROM nexus.company_contact ORDER BY company_id");
        assertThat(contacts).singleElement().satisfies(c -> {
            assertThat(c).containsEntry("id", 1001L).containsEntry("company_id", 1001L)
                    .containsEntry("name", "Mario Rossi");
            assertThat(((Number) c.get("position")).intValue()).isZero();
            assertThat(c.get("role_id")).isNull();
        });
        assertThat(jdbc.queryForObject("SELECT contact_person FROM nexus.company WHERE id = 1001", String.class))
                .isEqualTo("  Mario Rossi ");
    }
}
