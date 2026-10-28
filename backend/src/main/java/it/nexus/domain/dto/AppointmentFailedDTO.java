package it.nexus.domain.dto;

import it.nexus.domain.enumeration.AppointmentFailureCause;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Appuntamento non andato a buon fine (US-802): causa, nota (obbligatoria con "Altro") e versione; {@code relaunch}
 * chiede il rilancio della mansione in bacheca (US-803).
 */
public record AppointmentFailedDTO(
        @NotNull @PositiveOrZero Long version,
        @NotNull AppointmentFailureCause cause,
        @Size(max = 1000) String note,
        Boolean relaunch) {

    public boolean relaunchRequested() {
        return Boolean.TRUE.equals(relaunch);
    }
}
