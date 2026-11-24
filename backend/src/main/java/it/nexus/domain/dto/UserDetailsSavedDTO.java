package it.nexus.domain.dto;

/**
 * Esito della correzione dei dati (US-116). {@code invitationSent} c'è solo se l'email è cambiata a un utente che non
 * ha ancora la password: {@code true} se il nuovo invito è partito.
 */
public record UserDetailsSavedDTO(UserDetailsDTO user, Boolean invitationSent) {
}
