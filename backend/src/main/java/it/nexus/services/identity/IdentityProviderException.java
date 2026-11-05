package it.nexus.services.identity;

import java.io.Serial;

import lombok.Getter;

/** Aggiornamento rifiutato dall'identity provider o non riuscito. Il messaggio non contiene dati personali. */
@Getter
public class IdentityProviderException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Vero se l'identity provider ha rifiutato l'indirizzo perché già usato da un altro utente. */
    private final boolean emailTaken;

    public IdentityProviderException(String message, boolean emailTaken, Throwable cause) {
        super(message, cause);
        this.emailTaken = emailTaken;
    }
}
