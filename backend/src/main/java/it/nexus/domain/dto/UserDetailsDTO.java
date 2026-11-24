package it.nexus.domain.dto;

/**
 * Dati di un utente che l'ADMIN può correggere (US-116). {@code usernameEditable} è falso quando l'utente ha già una
 * password: l'audit può avere righe con il suo username come autore.
 */
public record UserDetailsDTO(
        String id,
        String username,
        String firstName,
        String lastName,
        String email,
        String phone,
        boolean active,
        boolean usernameEditable) {
}
