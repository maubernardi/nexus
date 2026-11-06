package it.nexus.services;

import java.util.List;

import it.nexus.domain.dto.UserCreateDTO;
import it.nexus.domain.dto.UserCreatedDTO;
import it.nexus.domain.dto.UserDTO;
import it.nexus.domain.dto.UserProjectsDTO;

/** Gestione degli utenti da parte dell'ADMIN, senza la console di Keycloak (US-103, US-104). */
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

    /** @throws it.nexus.web.errors.NotFoundException utente inesistente */
    UserDTO get(String id);

    /**
     * Sostituisce i progetti assegnati a un Tutor (US-104). Un progetto disattivato non si assegna, ma si può togliere.
     *
     * @throws it.nexus.web.errors.BadRequestException l'utente non è un Tutor
     * @throws it.nexus.web.errors.FieldValidationException progetto inesistente o disattivato
     */
    UserDTO updateProjects(String id, UserProjectsDTO dto);
}
