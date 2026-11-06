package it.nexus.services;

import java.util.List;

import it.nexus.domain.dto.UserCreateDTO;
import it.nexus.domain.dto.UserCreatedDTO;
import it.nexus.domain.dto.UserDTO;
import it.nexus.domain.dto.UserImpactDTO;
import it.nexus.domain.dto.UserProjectsDTO;
import it.nexus.domain.enumeration.Role;

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

    /** Che cosa tocca disattivare l'utente o cambiargli il ruolo (US-105). */
    UserImpactDTO impact(String id);

    /**
     * Disattiva o riattiva l'utente in NEXUS e in Keycloak (US-105). Disattivato, non accede più e le sue sessioni si
     * chiudono; i ticket che aveva in carico tornano in coda.
     *
     * @throws it.nexus.web.errors.BadRequestException l'ADMIN stesso, o un Tutor con segnalazioni aperte
     * @throws it.nexus.web.errors.ServiceUnavailableException Keycloak non raggiungibile: nulla cambia
     */
    UserDTO setActive(String id, boolean active);

    /**
     * Cambia il ruolo in NEXUS e in Keycloak (US-105); vale dal login successivo, le sessioni si chiudono. Chi diventa
     * Tutor lascia in coda i ticket che aveva in carico.
     *
     * @throws it.nexus.web.errors.BadRequestException l'ADMIN stesso, o un Tutor con segnalazioni aperte
     * @throws it.nexus.web.errors.ServiceUnavailableException Keycloak non raggiungibile: nulla cambia
     */
    UserDTO changeRole(String id, Role role);
}
