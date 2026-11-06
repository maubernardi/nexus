package it.nexus.domain.dto;

import it.nexus.domain.enumeration.Role;

/** Utente di NEXUS nell'elenco dell'ADMIN (US-103). */
public record UserDTO(
        String id,
        String username,
        String firstName,
        String lastName,
        String email,
        String phone,
        Role role,
        boolean active) {
}
