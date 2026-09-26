package it.nexus.web.errors;

import java.io.Serial;

import org.springframework.http.HttpStatus;

public class NotFoundException extends AbstractDomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    public NotFoundException(String message, Object... args) {
        super(HttpStatus.NOT_FOUND, message, args);
    }
}
