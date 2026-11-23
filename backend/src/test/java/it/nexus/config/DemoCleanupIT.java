package it.nexus.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * Pulizia della demo per il passaggio in produzione (US-1306, {@code infra/deploy/pulizia-demo.sql}), sullo schema vero
 * con i dati dimostrativi: via segnalazioni, beneficiari, utenti, aziende e la loro storia; restano configurazione e
 * cataloghi; le regole di sola aggiunta tornano attive; il backend riparte senza il profilo {@code seed}.
 */
@Testcontainers
class DemoCleanupIT {

    private static final String SCRIPT = "/tmp/pulizia-demo.sql";

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(DockerImageName.parse("postgres:19.0-alpine"))
            .withCopyFileToContainer(MountableFile.forHostPath(Path.of("..", "infra", "deploy", "pulizia-demo.sql")),
                    SCRIPT);

    /** Tabelle che la pulizia svuota. */
    private static final List<String> REMOVED = List.of("ticket", "board_post", "ticket_status_history",
            "ticket_company_blacklist", "beneficiary", "beneficiary_language", "app_user", "user_project", "company",
            "company_contact", "company_note", "company_audit_event", "job_slot");

    /** Configurazione e cataloghi, che restano come sono. */
    private static final List<String> KEPT = List.of("project", "job_category", "zone", "competence_site",
            "competence_site_project", "project_internship_type", "care_agency", "internship_type", "closure_reason",
            "hiring_formula", "no_hire_reason", "business_category", "contact_role", "work_calendar",
            "document_template");

    private static Map<String, Long> keptBefore;

    @BeforeAll
    static void demoDatabase() throws SQLException {
        Properties seed = config("seed");
        Flyway flyway = flyway(seed, Arrays.asList(seed.getProperty("spring.flyway.locations").split(",\\s*")));
        flyway.clean();
        flyway.migrate();
        try (Connection c = connection(); Statement s = c.createStatement()) {
            // un curriculum di un beneficiario e il file di un modello dei documenti
            s.execute("""
                    INSERT INTO nexus.stored_file VALUES
                      (901, 'cv.pdf', 'application/pdf', 3, repeat('a', 64), 'cv-901', now(), 'prova', now(), 'prova'),
                      (902, 'modello.docx', 'application/octet-stream', 3, repeat('b', 64), 'modello-902', now(),
                       'prova', now(), 'prova');
                    INSERT INTO nexus.stored_file_content VALUES (901, 'abc'), (902, 'def');
                    UPDATE nexus.beneficiary SET cv_file_id = 901
                    WHERE id = (SELECT min(id) FROM nexus.beneficiary);
                    INSERT INTO nexus.document_template (id, document_kind, project_id, internship_type_id, name,
                      file_id, created_at, created_by, updated_at, updated_by)
                    SELECT 903, 'CONVENZIONE', p.id, t.id, 'Convenzione di prova', 902, now(), 'prova', now(), 'prova'
                    FROM nexus.project p, nexus.internship_type t WHERE p.code = 'GOL' LIMIT 1;
                    INSERT INTO nexus.audit_event (id, entity_type, entity_id, action, created_at, created_by)
                    SELECT 950 + row_number() OVER (), t, 1, 'CREATE', now(), 'prova'
                    FROM unnest(ARRAY['TICKET', 'BENEFICIARY', 'APP_USER', 'COMPANY', 'JOB_SLOT', 'BOARD_POST',
                      'PROJECT', 'CALENDAR']) t;
                    """);
            assertThat(count(c, "ticket")).as("la demo ha le segnalazioni").isPositive();
            assertThat(count(c, "company")).as("la demo ha le aziende").isPositive();
            assertThat(count(c, "app_user")).as("la demo ha gli utenti").isPositive();
            keptBefore = counts(c, KEPT);
        }
        // con gli script dimostrativi nella cronologia, il backend senza il profilo seed non partirebbe
        Properties production = config("default");
        assertThat(flyway(production, List.of(production.getProperty("spring.flyway.locations")))
                .validateWithResult().validationSuccessful).isFalse();

        ExecResult result = runScript();
        assertThat(result.getExitCode()).as(result.getStderr()).isZero();
        // le voci segnaposto della demo, da controllare a mano dopo il passaggio
        assertThat(result.getStdout())
                .contains("Configurazione che resta")
                .containsPattern("tipologia di mansione\\s*\\| MAGAZZINIERE")
                .containsPattern("progetto\\s*\\| POLIS")
                .containsPattern("ente, dati inventati\\s*\\| COMUNE_DI_FIRENZE");
    }

    @Test
    void segnalazioniBeneficiariUtentiEAziendeVia() throws SQLException {
        try (Connection c = connection()) {
            for (String table : REMOVED) {
                assertThat(count(c, table)).as(table).isZero();
            }
        }
    }

    @Test
    void configurazioneECataloghiRestano() throws SQLException {
        try (Connection c = connection()) {
            assertThat(counts(c, KEPT)).isEqualTo(keptBefore);
        }
    }

    @Test
    void curriculumViaFileDeiModelliRestano() throws SQLException {
        try (Connection c = connection()) {
            assertThat(longs(c, "SELECT id FROM nexus.stored_file ORDER BY id")).containsExactly(902L);
            assertThat(longs(c, "SELECT file_id FROM nexus.stored_file_content ORDER BY file_id"))
                    .containsExactly(902L);
        }
    }

    @Test
    void audit_soloLaStoriaDellaConfigurazione() throws SQLException {
        try (Connection c = connection(); Statement s = c.createStatement();
                ResultSet rs = s.executeQuery("SELECT entity_type FROM nexus.audit_event ORDER BY entity_type")) {
            List<String> types = new ArrayList<>();
            while (rs.next()) {
                types.add(rs.getString(1));
            }
            assertThat(types).containsExactly("CALENDAR", "PROJECT");
        }
    }

    @Test
    void regoleDiSolaAggiuntaDiNuovoAttive() throws SQLException {
        try (Connection c = connection(); Statement s = c.createStatement()) {
            assertThatThrownBy(() -> s.execute("DELETE FROM nexus.audit_event"))
                    .hasMessageContaining("sola aggiunta");
            assertThatThrownBy(() -> s.execute("TRUNCATE nexus.company_note"))
                    .hasMessageContaining("sola aggiunta");
        }
    }

    @Test
    void ilBackendRiparteSenzaIlProfiloSeed() {
        Properties production = config("default");
        assertThat(flyway(production, List.of(production.getProperty("spring.flyway.locations")))
                .validateWithResult().validationSuccessful).isTrue();
    }

    @Test
    void siPuoRilanciare() throws Exception {
        ExecResult again = runScript();
        assertThat(again.getExitCode()).as(again.getStderr()).isZero();
        try (Connection c = connection()) {
            assertThat(counts(c, KEPT)).isEqualTo(keptBefore);
        }
    }

    private static ExecResult runScript() {
        try {
            return POSTGRES.execInContainer("psql", "-X", "-v", "ON_ERROR_STOP=1", "-U", POSTGRES.getUsername(), "-d",
                    POSTGRES.getDatabaseName(), "-f", SCRIPT);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static Connection connection() throws SQLException {
        return DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    private static long count(Connection c, String table) throws SQLException {
        return longs(c, "SELECT count(*) FROM nexus." + table).getFirst();
    }

    private static Map<String, Long> counts(Connection c, List<String> tables) throws SQLException {
        Map<String, Long> result = new LinkedHashMap<>();
        for (String table : tables) {
            result.put(table, count(c, table));
        }
        return result;
    }

    private static List<Long> longs(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            List<Long> values = new ArrayList<>();
            while (rs.next()) {
                values.add(rs.getLong(1));
            }
            return values;
        }
    }

    private static Flyway flyway(Properties config, List<String> locations) {
        FluentConfiguration fluent = Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .schemas(config.getProperty("spring.flyway.schemas"))
                .defaultSchema(config.getProperty("spring.flyway.default-schema"))
                .cleanDisabled(false)
                .locations(locations.toArray(String[]::new));
        String patterns = config.getProperty("spring.flyway.ignore-migration-patterns");
        if (patterns != null) {
            fluent.ignoreMigrationPatterns(patterns.split(",\\s*"));
        }
        return fluent.load();
    }

    /** application.yml più, se c'è, application-{profilo}.yml: come Spring Boot all'avvio. */
    private static Properties config(String profile) {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(profile.equals("default")
                ? new ClassPathResource[] {new ClassPathResource("application.yml")}
                : new ClassPathResource[] {new ClassPathResource("application.yml"),
                        new ClassPathResource("application-" + profile + ".yml")});
        return yaml.getObject();
    }
}
