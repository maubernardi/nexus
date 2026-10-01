package it.nexus.web.errors;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/** Errore di validazione su un campo, verificato nel service (es. riferimento inesistente): 400 con {@code fieldErrors}. */
@Getter
public class FieldValidationException extends AbstractDomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String field;

    public FieldValidationException(String field, String message) {
        super(HttpStatus.BAD_REQUEST, message);
        this.field = field;
    }
}
