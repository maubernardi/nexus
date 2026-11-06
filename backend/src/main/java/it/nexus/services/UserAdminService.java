package it.nexus.services;

import java.util.List;

import it.nexus.domain.dto.UserCreateDTO;
import it.nexus.domain.dto.UserCreatedDTO;
import it.nexus.domain.dto.UserDTO;

/** Gestione degli utenti da parte dell'ADMIN, senza la console di Keycloak (US-103). */
public interface UserAdminService {

    List<UserDTO> list();

    /**
     * Crea l'utente in Keycloak e in NEXUS con lo stesso identificativo e il ruolo scelto, poi gli invia l'email per
     * impostare la password. Se NEXUS non salva, l'account Keycloak si cancella.
     *
     * @throws it.nexus.web.errors.FieldValidationException username o email già usati
     * @throws it.nexus.web.errors.ServiceUnavailableException Keycloak non raggiungibile
     */
    UserCreatedDTO create(UserCreateDTO dto);

    /**
     * Invia di nuovo l'email per impostare la password, con un collegamento nuovo.
     *
     * @throws it.nexus.web.errors.ServiceUnavailableException email non inviata
     */
    void resendInvitation(String id);
}
