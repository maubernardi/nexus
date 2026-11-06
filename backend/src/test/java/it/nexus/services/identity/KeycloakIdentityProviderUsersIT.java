package it.nexus.services.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import it.nexus.config.ApplicationProperties;
import it.nexus.domain.enumeration.Role;

/**
 * Cambio email in un Keycloak vero (US-110), con il realm di sviluppo di {@code infra/keycloak}: verifica anche che il
 * client tecnico {@code nexus-backend-admin} del realm abbia i permessi giusti.
 */
@Testcontainers
class KeycloakIdentityProviderUsersIT {

    private static final String DEV_SECRET = "nexus-backend-admin-dev-secret";
    private static final String TUTOR1 = "00000000-0000-4000-8000-000000000001";
    private static final String TUTOR2 = "00000000-0000-4000-8000-000000000002";
    private static final ParameterizedTypeReference<Map<String, Object>> JSON = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<List<Map<String, Object>>> JSON_LIST =
            new ParameterizedTypeReference<>() {
            };

    @Container
    static final GenericContainer<?> KEYCLOAK = new GenericContainer<>("quay.io/keycloak/keycloak:26.7.4")
            .withCommand("start-dev", "--import-realm")
            .withCopyFileToContainer(MountableFile.forHostPath("../infra/keycloak/nexus-realm.json"),
                    "/opt/keycloak/data/import/nexus-realm.json")
            // come in produzione (infra/deploy/compose.yaml): dominio pubblico, console solo da localhost, backend
            // che chiama Keycloak da un indirizzo interno
            .withEnv("KC_HOSTNAME", "https://auth.nexus.test")
            .withEnv("KC_HOSTNAME_ADMIN", "http://localhost:8081")
            .withEnv("KC_HOSTNAME_BACKCHANNEL_DYNAMIC", "true")
            .withEnv("KC_HTTP_ENABLED", "true")
            .withEnv("KC_PROXY_HEADERS", "xforwarded")
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/realms/nexus").forPort(8080).withStartupTimeout(Duration.ofMinutes(3)));

    private static String baseUrl;

    @BeforeAll
    static void baseUrl() {
        baseUrl = "http://" + KEYCLOAK.getHost() + ":" + KEYCLOAK.getMappedPort(8080);
    }

    private static KeycloakIdentityProviderUsers users(String secret) {
        ApplicationProperties properties = new ApplicationProperties();
        properties.getKeycloakAdmin().setBaseUrl(baseUrl);
        properties.getKeycloakAdmin().setClientSecret(secret);
        return new KeycloakIdentityProviderUsers(properties);
    }

    @Test
    void cambiaLEmailEMantieneGliAltriDati() {
        users(DEV_SECRET).updateEmail(TUTOR1, "nuovo.tutor1@example.org");

        Map<String, Object> user = readUser(TUTOR1);
        assertThat(user).containsEntry("email", "nuovo.tutor1@example.org")
                .containsEntry("emailVerified", true)
                .containsEntry("username", "tutor1")
                .containsEntry("firstName", "Tutor")
                .containsEntry("lastName", "Uno");
    }

    @Test
    void indirizzoDiUnAltroUtente_rifiutatoComeGiaUsato() {
        assertThatThrownBy(() -> users(DEV_SECRET).updateEmail(TUTOR2, "operatore.cc@nexus.local"))
                .isInstanceOfSatisfying(IdentityProviderException.class, e -> assertThat(e.isEmailTaken()).isTrue());
        assertThat(readUser(TUTOR2)).containsEntry("email", "tutor2@nexus.local");
    }

    @Test
    void segretoSbagliatoOMancante_errore() {
        assertThatThrownBy(() -> users("sbagliato").updateEmail(TUTOR2, "x@example.org"))
                .isInstanceOfSatisfying(IdentityProviderException.class, e -> assertThat(e.isEmailTaken()).isFalse());
        assertThatThrownBy(() -> users("").updateEmail(TUTOR2, "x@example.org"))
                .isInstanceOf(IdentityProviderException.class);
    }


    @Test
    void nuovoUtente_conIlRuoloScelto_eCancellabile() {
        KeycloakIdentityProviderUsers users = users(DEV_SECRET);
        String id = users.createUser(new NewIdentityUser("mario.rossi", "Mario", "Rossi", "mario.rossi@example.org",
                Role.CALL_CENTER));

        assertThat(readUser(id)).containsEntry("username", "mario.rossi")
                .containsEntry("email", "mario.rossi@example.org")
                .containsEntry("firstName", "Mario")
                .containsEntry("enabled", true);
        assertThat(realmRoles(id)).contains("CALL_CENTER").doesNotContain("TUTOR", "ADMIN");

        users.deleteUser(id);
        assertThatThrownBy(() -> readUser(id)).isInstanceOf(RestClientResponseException.class);
    }

    @Test
    void nuovoUtente_usernameOEmailGiaUsati_rifiutatiConIlMotivo() {
        KeycloakIdentityProviderUsers users = users(DEV_SECRET);
        assertThatThrownBy(() -> users.createUser(
                new NewIdentityUser("tutor1", "Altro", "Tutor", "altro@example.org", Role.TUTOR)))
                .isInstanceOfSatisfying(IdentityProviderException.class,
                        e -> assertThat(e.isUsernameTaken()).isTrue());
        assertThatThrownBy(() -> users.createUser(
                new NewIdentityUser("altro.tutor", "Altro", "Tutor", "admin@nexus.local", Role.TUTOR)))
                .isInstanceOfSatisfying(IdentityProviderException.class, e -> assertThat(e.isEmailTaken()).isTrue());
    }

    @Test
    void emailPerLaPassword_senzaServerDiPostaNelRealm_errore() {
        KeycloakIdentityProviderUsers users = users(DEV_SECRET);
        String id = users.createUser(new NewIdentityUser("senza.posta", "Senza", "Posta", "senza.posta@example.org",
                Role.TUTOR));
        assertThatThrownBy(() -> users.sendPasswordSetupEmail(id, Duration.ofDays(3), "http://localhost:5173/"))
                .isInstanceOf(IdentityProviderException.class);
        users.deleteUser(id);
    }

    @Test
    void disattivazione_cambioRuolo_eChiusuraDelleSessioni() {
        KeycloakIdentityProviderUsers users = users(DEV_SECRET);
        String id = users.createUser(new NewIdentityUser("da.cambiare", "Da", "Cambiare", "da.cambiare@example.org",
                Role.TUTOR));
        List<String> others = realmRoles(id).stream().filter(r -> Role.fromName(r).isEmpty()).toList();

        users.setEnabled(id, false);
        assertThat(readUser(id)).containsEntry("enabled", false).containsEntry("username", "da.cambiare");
        users.setEnabled(id, true);
        assertThat(readUser(id)).containsEntry("enabled", true);

        users.changeRole(id, Role.CALL_CENTER);
        // il ruolo NEXUS si sostituisce, gli altri ruoli del realm restano
        assertThat(realmRoles(id)).contains("CALL_CENTER").doesNotContain("TUTOR", "ADMIN")
                .containsAll(others);
        users.changeRole(id, Role.CALL_CENTER);
        assertThat(realmRoles(id)).filteredOn(r -> Role.fromName(r).isPresent()).containsExactly("CALL_CENTER");

        users.logout(id);
        users.deleteUser(id);
    }

    private static List<String> realmRoles(String id) {
        String token = adminToken();
        List<Map<String, Object>> roles = RestClient.create(baseUrl).get()
                .uri("/admin/realms/nexus/users/{id}/role-mappings/realm", id)
                .headers(h -> h.setBearerAuth(token))
                .retrieve().body(JSON_LIST);
        return roles.stream().map(r -> (String) r.get("name")).toList();
    }

    private static String adminToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", "nexus-backend-admin");
        form.add("client_secret", DEV_SECRET);
        Map<String, Object> token = RestClient.create(baseUrl).post().uri("/realms/nexus/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve().body(JSON);
        return (String) token.get("access_token");
    }

    private static Map<String, Object> readUser(String id) {
        String token = adminToken();
        return RestClient.create(baseUrl).get().uri("/admin/realms/nexus/users/{id}", id)
                .headers(h -> h.setBearerAuth(token))
                .retrieve().body(JSON);
    }
}
