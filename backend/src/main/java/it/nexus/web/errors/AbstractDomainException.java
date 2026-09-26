package it.nexus.web.errors;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * Base delle eccezioni di dominio: il {@link GlobalExceptionHandler} le traduce nello status associato.
 */
@Getter
public abstract class AbstractDomainException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final HttpStatus status;

    protected AbstractDomainException(HttpStatus status, String message, Object... args) {
        super(args.length == 0 ? message : message.formatted(args));
        this.status = status;
    }
}
