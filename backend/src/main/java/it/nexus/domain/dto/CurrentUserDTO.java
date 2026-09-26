package it.nexus.domain.dto;

import java.util.Set;

import it.nexus.domain.enumeration.Role;

public record CurrentUserDTO(
        String id,
        String username,
        String firstName,
        String lastName,
        String email,
        Set<Role> roles) {
}
