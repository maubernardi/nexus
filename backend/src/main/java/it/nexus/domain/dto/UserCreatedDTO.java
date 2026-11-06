package it.nexus.domain.dto;

/**
 * Utente creato (US-103).
 *
 * @param invitationSent vero se Keycloak ha inviato l'email per impostare la password; se falso, l'ADMIN la reinvia
 */
public record UserCreatedDTO(UserDTO user, boolean invitationSent) {
}
