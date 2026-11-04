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
    }
}
