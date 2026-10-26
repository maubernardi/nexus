package it.nexus.domain.dto;

import java.time.LocalDate;

/** Prova del calcolo (US-703): promemoria al 4° e scadenza al 7° giorno lavorativo dopo {@code from}. */
public record DeadlinePreviewDTO(LocalDate from, LocalDate reminder, LocalDate deadline) {
}
