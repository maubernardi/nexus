package it.nexus.domain.dto;

import java.util.Set;

import it.nexus.domain.enumeration.Role;

public record CurrentUserDTO(
        String id,
        String username,
        String firstName,
        String lastName,
        String email,
        Set<Role> roles,
        /** Tutor che coordina almeno un progetto (US-112): vede la pagina del coordinamento. */
        boolean coordinator) {

    public CurrentUserDTO withCoordinator(boolean value) {
        return new CurrentUserDTO(id, username, firstName, lastName, email, roles, value);
    }
}
