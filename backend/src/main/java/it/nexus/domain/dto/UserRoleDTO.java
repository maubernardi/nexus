package it.nexus.domain.dto;

import it.nexus.domain.enumeration.Role;
import jakarta.validation.constraints.NotNull;

/** Nuovo ruolo di un utente (US-105). */
public record UserRoleDTO(@NotNull(message = "Scegli il ruolo") Role role) {
}
