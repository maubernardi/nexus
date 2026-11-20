package it.nexus.config;

import static org.assertj.core.api.Assertions.assertThat;

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
 * V49 (US-605): le segnalazioni che bloccano una mansione diventano assegnate; il tipo di tirocinio passa dalla scheda
 * alla segnalazione. Il riassunto sulla mansione resta com'era.
 */
@Testcontainers
class TicketSlotAssignmentMigrationIT {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(DockerImageName.parse("postgres:18.6-alpine"));

    private static final long INCLUSION = 893700000000000103L;

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
    void assegnazioniDaiBlocchiETipiDalleSchede() {
        flyway("latest").clean();
        flyway("48").migrate();
        // ogni comando apre una connessione: lo schema va nell'indirizzo
        String url = POSTGRES.getJdbcUrl() + (POSTGRES.getJdbcUrl().contains("?") ? "&" : "?") + "currentSchema=nexus";
        JdbcTemplate jdbc = new JdbcTemplate(
                new DriverManagerDataSource(url, POSTGRES.getUsername(), POSTGRES.getPassword()));
        String audit = "now(), 'test', now(), 'test'";
        jdbc.update("INSERT INTO zone (id, code, name, created_at, created_by, updated_at, updated_by) VALUES "
                + "(1, 'ZTEST', 'Zona', " + audit + ")");
        jdbc.update("INSERT INTO job_category (id, code, name, created_at, created_by, updated_at, updated_by) VALUES "
                + "(2, 'CTEST', 'Cameriere', " + audit + ")");
        jdbc.update("INSERT INTO project (id, code, name, created_at, created_by, updated_at, updated_by) VALUES "
                + "(3, 'PTEST', 'Progetto', " + audit + ")");
        jdbc.update("INSERT INTO app_user (id, external_id, username, first_name, last_name, email, role, created_at, "
                + "created_by, updated_at, updated_by) VALUES (4, 'ext-4', 'tutor', 'Tu', 'Tor', 'tutor@test.it', "
                + "'TUTOR', " + audit + ")");
        for (long b = 5; b <= 7; b++) {
            jdbc.update("INSERT INTO beneficiary (id, owner_tutor_id, first_name, last_name, birth_year, gender, "
                    + "residence_zone_id, created_at, created_by, updated_at, updated_by) VALUES (?, 4, 'Be', 'Ne', "
                    + "1990, 'F', 1, " + audit + ")", b);
        }
        jdbc.update("INSERT INTO company (id, name, vat_code, created_at, created_by, updated_at, updated_by) VALUES "
                + "(8, 'Alfa', '12345678901', " + audit + ")");
        String slot = "INSERT INTO job_slot (id, company_id, job_category_id, zone_id, title, created_at, created_by, "
                + "updated_at, updated_by) VALUES (?, 8, 2, 1, 'Cameriere', " + audit + ")";
        jdbc.update(slot, 9L);
        jdbc.update(slot, 11L);
        String ticket = "INSERT INTO ticket (id, tutor_id, project_id, beneficiary_id, type, status, "
                + "requested_job_category_id, job_slot_id, created_at, created_by, updated_at, updated_by) "
                + "VALUES (?, 4, 3, ?, 'NORMAL', ?, 2, ?, " + audit + ")";
        jdbc.update(ticket, 10L, 5L, "PROPOSTA_AZIENDA", 9L);
        jdbc.update(ticket, 12L, 6L, "FORM_RESTITUZIONE", 11L);
        jdbc.update(ticket, 13L, 7L, "NUOVA", null);
        jdbc.update("UPDATE job_slot SET status = 'BLOCCATA', blocked_by_ticket_id = 10 WHERE id = 9");
        jdbc.update("INSERT INTO internship (id, ticket_id, internship_type_id, created_at, created_by, updated_at, "
                + "updated_by) VALUES (14, 10, ?, " + audit + ")", INCLUSION);

        flyway("latest").migrate();

        Map<Long, Map<String, Object>> rows = new java.util.HashMap<>();
        jdbc.queryForList("SELECT id, job_slot_held, internship_type_id FROM nexus.ticket")
                .forEach(r -> rows.put(((Number) r.get("id")).longValue(), r));
        assertThat(rows.get(10L)).containsEntry("job_slot_held", true).containsEntry("internship_type_id", INCLUSION);
        // chiusa con la mansione ancora disponibile: collegata per lo storico, non assegnata
        assertThat(rows.get(12L)).containsEntry("job_slot_held", false);
        assertThat(rows.get(13L).get("internship_type_id")).isNull();
        assertThat(jdbc.queryForObject("SELECT status FROM nexus.job_slot WHERE id = 9", String.class))
                .isEqualTo("BLOCCATA");
    }
}
