package it.nexus.domain.dto;

import java.time.LocalDate;
import java.util.List;

/** Calendario unico (US-107): patrono (giorno e mese) e chiusure, dalla prima. */
public record WorkCalendarDTO(int patronDay, int patronMonth, long version, List<Closure> closures) {

    public record Closure(String id, LocalDate startDate, LocalDate endDate, String description) {
    }
}
