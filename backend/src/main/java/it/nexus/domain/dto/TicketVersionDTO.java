package it.nexus.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Versione del ticket vista dal client: un'azione su una versione superata viene rifiutata con 409. */
public record TicketVersionDTO(@NotNull(message = "Versione mancante") @PositiveOrZero Long version) {
}
