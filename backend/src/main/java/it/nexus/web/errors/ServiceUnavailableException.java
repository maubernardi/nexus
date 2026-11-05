package it.nexus.web.errors;

import java.io.Serial;

import org.springframework.http.HttpStatus;

/** Servizio esterno (server di posta, identity provider) non disponibile: 503, l'utente può riprovare più tardi. */
public class ServiceUnavailableException extends AbstractDomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ServiceUnavailableException(String message, Object... args) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message, args);
    }
}
