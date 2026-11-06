package it.nexus.domain.dto;

import java.util.List;

import it.nexus.domain.enumeration.Role;

/**
 * Utente di NEXUS nell'elenco dell'ADMIN (US-103), con i progetti assegnati per nome (US-104): contano solo per i
 * Tutor, perché Call Center e ADMIN vedono tutti i progetti.
 */
public record UserDTO(
        String id,
        String username,
        String firstName,
        String lastName,
        String email,
        String phone,
        Role role,
        boolean active,
        List<ReferenceItemDTO> projects) {
}
