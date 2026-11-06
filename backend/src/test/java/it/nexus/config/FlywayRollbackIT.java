package it.nexus.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Ripristino della release precedente (A16): una release nuova applica le sue migrazioni e poi non parte; la precedente
 * deve ripartire sullo stesso database, che contiene una migrazione che non conosce. Il 05/11 la demo (profilo
 * {@code seed}) non è ripartita perché {@code ignore-migration-patterns} sostituiva il valore predefinito di Flyway,
 * {@code *:future}.
 */
@Testcontainers
class FlywayRollbackIT {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(DockerImageName.parse("postgres:18.6-alpine"));

    @ParameterizedTest(name = "profilo {0}")
    @ValueSource(strings = {"default", "seed"})
    void laReleasePrecedenteRipartesConUnaMigrazioneDelFuturo(String profile) {
        Properties config = config(profile);
        List<String> locations = Arrays.asList(config.getProperty("spring.flyway.locations").split(",\\s*"));
        // le migrazioni usano lo schema "nexus" per nome: ogni caso riparte da uno schema vuoto
        flyway(config, locations).clean();

        // la release nuova: le migrazioni di oggi più una che la precedente non ha
        List<String> newer = new ArrayList<>(locations);
        newer.add("classpath:db/future");
        flyway(config, newer).migrate();

        // la release precedente: stesse impostazioni del profilo, senza la migrazione nuova
        assertThat(flyway(config, locations).validateWithResult().validationSuccessful)
                .as("la release precedente deve ignorare le migrazioni del futuro")
                .isTrue();
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
