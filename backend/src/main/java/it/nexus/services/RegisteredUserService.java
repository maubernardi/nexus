package it.nexus.services;

import it.nexus.config.security.AuthenticatedUser;

public interface RegisteredUserService {

    /**
     * Verifica che l'utente autenticato sia censito e attivo in NEXUS e riallinea la copia del ruolo a quella
     * dell'identity provider (fonte autorevole).
     *
     * @throws it.nexus.config.security.UserNotEnabledException se l'utente non è censito o è disattivato
     */
    void verifyAndSyncRole(AuthenticatedUser user);
}
