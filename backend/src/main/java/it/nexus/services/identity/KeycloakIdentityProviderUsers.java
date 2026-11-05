package it.nexus.services.identity;

import java.time.Duration;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
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
import lombok.extern.slf4j.Slf4j;

/**
 * Utenti Keycloak tramite la API di amministrazione (US-110), con il client tecnico {@code nexus-backend-admin}
 * ({@code client_credentials}, ruoli view-users e manage-users). Il token si chiede a ogni operazione: sono rare.
 */
@Slf4j
@Component
@Profile("keycloak")
public class KeycloakIdentityProviderUsers implements IdentityProviderUsers {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final ParameterizedTypeReference<Map<String, Object>> JSON_OBJECT = new ParameterizedTypeReference<>() {
    };

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
            log.warn("Cambio email non disponibile: manca il segreto del client Keycloak {} "
                    + "(NEXUS_KEYCLOAK_ADMIN_CLIENT_SECRET nel .env del server)", properties.getClientId());
        }
    }

    @Override
    public void updateEmail(String externalId, String email) {
        if (!StringUtils.hasText(properties.getClientSecret())) {
            throw new IdentityProviderException("Segreto del client Keycloak non configurato", false, null);
        }
        try {
            String token = accessToken();
            String userPath = "/admin/realms/{realm}/users/{id}";
            // si riscrive l'utente intero: con lo User Profile un PUT parziale può azzerare gli attributi mancanti
            Map<String, Object> user = restClient.get().uri(userPath, properties.getRealm(), externalId)
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve().body(JSON_OBJECT);
            if (user == null) {
                throw new IdentityProviderException("Utente Keycloak senza dati", false, null);
            }
            user.put("email", email);
            user.put("emailVerified", true);
            restClient.put().uri(userPath, properties.getRealm(), externalId)
                    .headers(h -> h.setBearerAuth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(user)
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            boolean taken = e.getStatusCode().isSameCodeAs(HttpStatus.CONFLICT);
            log.warn("Keycloak ha rifiutato il cambio email: HTTP {}", e.getStatusCode().value());
            throw new IdentityProviderException("Keycloak ha risposto " + e.getStatusCode().value(), taken, e);
        } catch (RestClientException e) {
            log.warn("Keycloak non raggiungibile per il cambio email: {}", e.getClass().getSimpleName());
            throw new IdentityProviderException("Keycloak non raggiungibile", false, e);
        }
    }

    private String accessToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());
        Map<String, Object> response = restClient.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", properties.getRealm())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve().body(JSON_OBJECT);
        Object token = response == null ? null : response.get("access_token");
        if (!(token instanceof String value) || value.isBlank()) {
            throw new IdentityProviderException("Token del client Keycloak assente", false, null);
        }
        return value;
    }
}
