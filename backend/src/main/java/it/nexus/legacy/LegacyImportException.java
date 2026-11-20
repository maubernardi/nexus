package it.nexus.legacy;

import java.io.Serial;

/** Importazione che non può partire (per esempio senza l'elenco dei comuni): il messaggio dice cosa fare. */
public class LegacyImportException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public LegacyImportException(String message) {
        super(message);
    }
}
