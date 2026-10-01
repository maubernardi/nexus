package it.nexus.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Versione vista dal client per un'azione senza altri dati (blocco ottimistico). */
public record VersionDTO(@NotNull(message = "Versione mancante") @PositiveOrZero Long version) {
}
