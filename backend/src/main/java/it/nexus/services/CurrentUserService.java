package it.nexus.services;

import it.nexus.domain.dto.CurrentUserDTO;

public interface CurrentUserService {

    /** Restituisce l'utente autenticato della richiesta corrente. */
    CurrentUserDTO getCurrentUser();
}
