package it.nexus.services.identity;

import java.io.Serial;

import lombok.Getter;

/** Operazione rifiutata dall'identity provider o non riuscita. Il messaggio non contiene dati personali. */
@Getter
public class IdentityProviderException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Dato già usato da un altro account, se è questo il motivo del rifiuto. */
    public enum Conflict {
        NONE,
        EMAIL,
        USERNAME
    }

    private final Conflict conflict;

    public IdentityProviderException(String message, Conflict conflict, Throwable cause) {
        super(message, cause);
        this.conflict = conflict;
    }

    /** @param emailTaken vero se l'indirizzo è già usato da un altro utente */
    public IdentityProviderException(String message, boolean emailTaken, Throwable cause) {
        this(message, emailTaken ? Conflict.EMAIL : Conflict.NONE, cause);
    }

    public boolean isEmailTaken() {
        return conflict == Conflict.EMAIL;
    }

    public boolean isUsernameTaken() {
        return conflict == Conflict.USERNAME;
    }
}
