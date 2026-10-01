package it.nexus.services;

import it.nexus.domain.AppUser;

public interface CurrentAppUserService {

    /** Utente NEXUS della richiesta corrente (garantito censito e attivo dal filtro di sicurezza). */
    AppUser getCurrentAppUser();

    /** Vero se l'utente corrente ha visibilità globale (Call Center o ADMIN). */
    boolean hasGlobalVisibility();
}
