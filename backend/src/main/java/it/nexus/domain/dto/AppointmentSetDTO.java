package it.nexus.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Appuntamento fissato (US-801): nota facoltativa (es. data e ora) e versione per il blocco ottimistico. */
public record AppointmentSetDTO(@NotNull @PositiveOrZero Long version, @Size(max = 1000) String note) {
}
