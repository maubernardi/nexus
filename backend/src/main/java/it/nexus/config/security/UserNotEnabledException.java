package it.nexus.config.security;

import java.io.Serial;

import org.springframework.security.access.AccessDeniedException;

/** Utente autenticato dall'identity provider ma non censito o disattivato in NEXUS: risposta 403. */
public class UserNotEnabledException extends AccessDeniedException {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String MESSAGE = "Utente non abilitato a NEXUS";

    public UserNotEnabledException() {
        super(MESSAGE);
    }
}
