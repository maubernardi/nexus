package it.nexus.domain.dto;

import jakarta.validation.constraints.NotNull;

/** Attiva o disattiva un utente (US-105). */
public record UserActiveDTO(@NotNull(message = "Indica se l'utente è attivo") Boolean active) {
}
