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

    @Getter
    @Setter
    public static class Cors {
        /** Origini ammesse; vuoto = CORS disabilitato (in locale il proxy Vite evita CORS). */
        private List<String> allowedOrigins = List.of();
    }
}
