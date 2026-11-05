package it.nexus.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Proprietà applicative con prefisso {@code nexus}.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "nexus")
public class ApplicationProperties {

    private final Cors cors = new Cors();

    private final Mail mail = new Mail();

    private final KeycloakAdmin keycloakAdmin = new KeycloakAdmin();

    @Getter
    @Setter
    public static class Cors {
        /** Origini ammesse; vuoto = CORS disabilitato (in locale il proxy Vite evita CORS). */
        private List<String> allowedOrigins = List.of();
    }

    /** Email delle notifiche (US-1202); server, utente e password sono in {@code spring.mail}. */
    @Getter
    @Setter
    public static class Mail {
        /** Invio acceso; senza la password SMTP le email restano comunque spente. */
        private boolean enabled;
        /** Mittente: un indirizzo del dominio della casella autenticata (regola IONOS). */
        private String from = "NEXUS <noreply@portalenexus.it>";
        /** Indirizzo dell'app per il collegamento alla segnalazione, senza barra finale. */
        private String appUrl = "http://localhost:5173";
        /**
         * Solo sviluppo locale (profilo {@code security-mock}): con le email spente il codice del cambio di indirizzo
         * (US-110) si scrive nel log, così si prova il flusso. Mai in produzione.
         */
        private boolean logOtp;
    }

    /** API di amministrazione di Keycloak (US-110): client tecnico con i soli ruoli view-users e manage-users. */
    @Getter
    @Setter
    public static class KeycloakAdmin {
        /** Indirizzo di Keycloak raggiungibile dal backend (in produzione la rete interna), senza barra finale. */
        private String baseUrl = "http://localhost:8180";
        private String realm = "nexus";
        private String clientId = "nexus-backend-admin";
        /** Segreto del client: mai nel repository (NEXUS_KEYCLOAK_ADMIN_CLIENT_SECRET nel .env del server). */
        private String clientSecret = "";
    }
}
