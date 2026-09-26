package it.nexus.web.errors;

import java.io.Serial;

import org.springframework.http.HttpStatus;

public class ConflictException extends AbstractDomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ConflictException(String message, Object... args) {
        super(HttpStatus.CONFLICT, message, args);
    }
}
