package it.nexus.web.errors;

import java.io.Serial;

import org.springframework.http.HttpStatus;

public class BadRequestException extends AbstractDomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BadRequestException(String message, Object... args) {
        super(HttpStatus.BAD_REQUEST, message, args);
    }
}
