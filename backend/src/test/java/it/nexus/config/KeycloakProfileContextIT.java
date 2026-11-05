package it.nexus.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import it.nexus.services.identity.IdentityProviderUsers;
import it.nexus.services.identity.KeycloakIdentityProviderUsers;

/**
 * Il backend parte con il profilo di produzione ({@code keycloak}): gli altri test usano gli utenti mock e non creano i
 * bean propri di questo profilo. Nato dal deploy fallito di US-110 (05/11): un bean del profilo {@code keycloak}
 * chiedeva un {@code RestClient.Builder} che in Spring Boot 4 non esiste.
 */
@SpringBootTest
@ActiveProfiles("keycloak")
@Import(KeycloakProfileContextIT.Database.class)
class KeycloakProfileContextIT {

    /** Credenziali uguali ai default di application-postgresql.yml, usate anche da Flyway. */
    @TestConfiguration(proxyBeanMethods = false)
    static class Database {

        @Bean
        @ServiceConnection
        PostgreSQLContainer postgresContainer() {
            return new PostgreSQLContainer(DockerImageName.parse("postgres:18.6-alpine"))
                    .withDatabaseName("nexus").withUsername("nexus").withPassword("nexus");
        }
    }

    @Autowired IdentityProviderUsers identityProviderUsers;

    @Test
    void ilContestoParteConIBeanDiProduzione() {
        assertThat(identityProviderUsers).isInstanceOf(KeycloakIdentityProviderUsers.class);
    }
}
