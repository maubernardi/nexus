package it.nexus.services.identity;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import it.nexus.config.ApplicationProperties;
import it.nexus.domain.enumeration.Role;
import it.nexus.services.identity.IdentityProviderException.Conflict;
import lombok.extern.slf4j.Slf4j;

/**
 * Utenti Keycloak tramite la API di amministrazione (US-110, US-103), con il client tecnico {@code nexus-backend-admin}
 * ({@code client_credentials}, ruoli view-users e manage-users). Il token si chiede a ogni operazione: sono rare.
 */
@Slf4j
@Component
@Profile("keycloak")
public class KeycloakIdentityProviderUsers implements IdentityProviderUsers {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final ParameterizedTypeReference<Map<String, Object>> JSON_OBJECT = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<List<Map<String, Object>>> JSON_ARRAY =
            new ParameterizedTypeReference<>() {
            };
    private static final String USERS = "/admin/realms/{realm}/users";
    private static final String USER = USERS + "/{id}";
    /** Client della PWA: dopo aver impostato la password, l'utente torna all'app da qui. */
    private static final String APP_CLIENT_ID = "nexus-frontend";

    private final ApplicationProperties.KeycloakAdmin properties;
    private final RestClient restClient;

    public KeycloakIdentityProviderUsers(ApplicationProperties applicationProperties) {
        this.properties = applicationProperties.getKeycloakAdmin();
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(TIMEOUT);
        requestFactory.setReadTimeout(TIMEOUT);
        // RestClient.builder() e non il bean RestClient.Builder: in Spring Boot 4 l'autoconfigurazione del client HTTP
        // sta in un modulo che il backend non usa (il bean non esiste)
        this.restClient = RestClient.builder().baseUrl(properties.getBaseUrl()).requestFactory(requestFactory).build();
        if (!StringUtils.hasText(properties.getClientSecret())) {
            log.warn("Gestione utenti in Keycloak non disponibile: manca il segreto del client {} "
                    + "(NEXUS_KEYCLOAK_ADMIN_CLIENT_SECRET nel .env del server)", properties.getClientId());
        }
    }

    @Override
    public void updateEmail(String externalId, String email) {
        call("cambio email", token -> {
            // si riscrive l'utente intero: con lo User Profile un PUT parziale può azzerare gli attributi mancanti
            Map<String, Object> user = restClient.get().uri(USER, realm(), externalId)
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve().body(JSON_OBJECT);
            if (user == null) {
                throw new IdentityProviderException("Utente Keycloak senza dati", Conflict.NONE, null);
            }
            user.put("email", email);
            user.put("emailVerified", true);
            restClient.put().uri(USER, realm(), externalId)
                    .headers(h -> h.setBearerAuth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(user)
                    .retrieve().toBodilessEntity();
            return null;
        });
    }

    @Override
    public String createUser(NewIdentityUser user) {
        String id = call("creazione utente", token -> {
            URI location = restClient.post().uri(USERS, realm())
                    .headers(h -> h.setBearerAuth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "username", user.username(),
                            "firstName", user.firstName(),
                            "lastName", user.lastName(),
                            "email", user.email(),
                            // l'indirizzo lo verifica l'utente stesso aprendo l'email per la password
                            "emailVerified", true,
                            "enabled", true))
                    .retrieve().toBodilessEntity().getHeaders().getLocation();
            String path = location == null ? "" : location.getPath();
            String created = path.substring(path.lastIndexOf('/') + 1);
            if (created.isBlank()) {
                throw new IdentityProviderException("Keycloak non ha restituito l'utente creato", Conflict.NONE, null);
            }
            return created;
        });
        try {
            call("assegnazione del ruolo", token -> {
                // i ruoli assegnabili si leggono dall'utente: bastano view-users e manage-users, senza view-realm
                List<Map<String, Object>> available = restClient.get()
                        .uri(USER + "/role-mappings/realm/available", realm(), id)
                        .headers(h -> h.setBearerAuth(token))
                        .retrieve().body(JSON_ARRAY);
                Map<String, Object> role = (available == null ? List.<Map<String, Object>>of() : available).stream()
                        .filter(r -> user.role().name().equals(r.get("name")))
                        .findFirst()
                        .orElseThrow(() -> new IdentityProviderException(
                                "Ruolo " + user.role() + " assente nel realm", Conflict.NONE, null));
                restClient.post().uri(USER + "/role-mappings/realm", realm(), id)
                        .headers(h -> h.setBearerAuth(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(List.of(Map.of("id", role.get("id"), "name", role.get("name"))))
                        .retrieve().toBodilessEntity();
                return null;
            });
        } catch (IdentityProviderException e) {
            deleteQuietly(id);
            throw e;
        }
        return id;
    }

    @Override
    public void deleteUser(String externalId) {
        call("cancellazione utente", token -> {
            restClient.delete().uri(USER, realm(), externalId)
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve().toBodilessEntity();
            return null;
        });
    }

    @Override
    public void sendPasswordSetupEmail(String externalId, Duration lifespan, String redirectUri) {
        call("email per la password", token -> {
            restClient.put()
                    .uri(b -> b.path(USER + "/execute-actions-email")
                            .queryParam("lifespan", lifespan.toSeconds())
                            .queryParam("client_id", APP_CLIENT_ID)
                            .queryParam("redirect_uri", redirectUri)
                            .build(realm(), externalId))
                    .headers(h -> h.setBearerAuth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(List.of("UPDATE_PASSWORD"))
                    .retrieve().toBodilessEntity();
            return null;
        });
    }

    @Override
    public void setEnabled(String externalId, boolean enabled) {
        call(enabled ? "riattivazione" : "disattivazione", token -> {
            Map<String, Object> user = restClient.get().uri(USER, realm(), externalId)
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve().body(JSON_OBJECT);
            if (user == null) {
                throw new IdentityProviderException("Utente Keycloak senza dati", Conflict.NONE, null);
            }
            user.put("enabled", enabled);
            restClient.put().uri(USER, realm(), externalId)
                    .headers(h -> h.setBearerAuth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(user)
                    .retrieve().toBodilessEntity();
            return null;
        });
    }

    @Override
    public void changeRole(String externalId, Role role) {
        call("cambio ruolo", token -> {
            List<Map<String, Object>> assigned = realmRoles(token, externalId, "");
            List<Map<String, Object>> old = assigned.stream()
                    .filter(r -> Role.fromName(String.valueOf(r.get("name"))).filter(x -> x != role).isPresent())
                    .map(KeycloakIdentityProviderUsers::reference)
                    .toList();
            boolean alreadyThere = assigned.stream().anyMatch(r -> role.name().equals(r.get("name")));
            Map<String, Object> added = null;
            if (!alreadyThere) {
                added = realmRoles(token, externalId, "/available").stream()
                        .filter(r -> role.name().equals(r.get("name")))
                        .findFirst().map(KeycloakIdentityProviderUsers::reference)
                        .orElseThrow(() -> new IdentityProviderException(
                                "Ruolo " + role + " assente nel realm", Conflict.NONE, null));
                roleMappings(HttpMethod.POST, token, externalId, List.of(added));
            }
            if (!old.isEmpty()) {
                try {
                    roleMappings(HttpMethod.DELETE, token, externalId, old);
                } catch (RestClientException e) {
                    // il vecchio ruolo non si toglie: si ritira il nuovo, l'account resta com'era
                    Optional.ofNullable(added).ifPresent(r -> {
                        try {
                            roleMappings(HttpMethod.DELETE, token, externalId, List.of(r));
                        } catch (RestClientException ignored) {
                            log.error("Utente Keycloak {} con due ruoli NEXUS: va sistemato dalla console", externalId);
                        }
                    });
                    throw e;
                }
            }
            return null;
        });
    }

    @Override
    public void logout(String externalId) {
        call("chiusura delle sessioni", token -> {
            restClient.post().uri(USER + "/logout", realm(), externalId)
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve().toBodilessEntity();
            return null;
        });
    }

    private List<Map<String, Object>> realmRoles(String token, String externalId, String suffix) {
        List<Map<String, Object>> roles = restClient.get()
                .uri(USER + "/role-mappings/realm" + suffix, realm(), externalId)
                .headers(h -> h.setBearerAuth(token))
                .retrieve().body(JSON_ARRAY);
        return roles == null ? List.of() : roles;
    }

    private void roleMappings(HttpMethod method, String token, String externalId, List<Map<String, Object>> roles) {
        restClient.method(method).uri(USER + "/role-mappings/realm", realm(), externalId)
                .headers(h -> h.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .body(roles)
                .retrieve().toBodilessEntity();
    }

    private static Map<String, Object> reference(Map<String, Object> role) {
        return Map.of("id", role.get("id"), "name", role.get("name"));
    }

    private void deleteQuietly(String externalId) {
        try {
            deleteUser(externalId);
        } catch (IdentityProviderException e) {
            log.error("Utente Keycloak {} creato a metà e non cancellato: va rimosso dalla console", externalId);
        }
    }

    /** Esegue una chiamata con un token nuovo, traducendo gli errori HTTP in {@link IdentityProviderException}. */
    private <T> T call(String operation, Function<String, T> request) {
        if (!StringUtils.hasText(properties.getClientSecret())) {
            throw new IdentityProviderException("Segreto del client Keycloak non configurato", Conflict.NONE, null);
        }
        try {
            return request.apply(accessToken());
        } catch (RestClientResponseException e) {
            Conflict conflict = conflictOf(e);
            log.warn("Keycloak ha rifiutato {}: HTTP {}", operation, e.getStatusCode().value());
            throw new IdentityProviderException("Keycloak ha risposto " + e.getStatusCode().value(), conflict, e);
        } catch (RestClientException e) {
            log.warn("Keycloak non raggiungibile per {}: {}", operation, e.getClass().getSimpleName());
            throw new IdentityProviderException("Keycloak non raggiungibile", Conflict.NONE, e);
        }
    }

    /** 409 di Keycloak: «User exists with same username» oppure «… same email». */
    private static Conflict conflictOf(RestClientResponseException e) {
        if (!e.getStatusCode().isSameCodeAs(HttpStatus.CONFLICT)) {
            return Conflict.NONE;
        }
        return e.getResponseBodyAsString().toLowerCase().contains("username") ? Conflict.USERNAME : Conflict.EMAIL;
    }

    private String realm() {
        return properties.getRealm();
    }

    private String accessToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());
        Map<String, Object> response = restClient.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", realm())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve().body(JSON_OBJECT);
        Object token = response == null ? null : response.get("access_token");
        if (!(token instanceof String value) || value.isBlank()) {
            throw new IdentityProviderException("Token del client Keycloak assente", Conflict.NONE, null);
        }
        return value;
    }
}
