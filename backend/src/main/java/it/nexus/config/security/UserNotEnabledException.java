package it.nexus.config.security;

import java.io.Serial;

import org.springframework.security.access.AccessDeniedException;

/** Utente autenticato dall'identity provider ma non censito o disattivato in NEXUS: risposta 403. */
public class UserNotEnabledException extends AccessDeniedException {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String MESSAGE = "Utente non abilitato a NEXUS";

    /** Codice dell'errore: il client mostra una pagina dedicata invece di un errore generico (US-101). */
    public static final String CODE = "USER_NOT_ENABLED";

    public UserNotEnabledException() {
        super(MESSAGE);
    }
}
