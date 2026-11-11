package it.nexus.domain.dto;

import java.time.LocalDate;

/**
 * Progetto formativo salvato (US-1008): il numero è la posizione nell'ordine delle date. Durata in mesi arrotondati e
 * monte ore in minuti sono calcolati sulle sue date; il monte ore manca senza griglia oraria.
 */
public record TrainingProjectDTO(
        int number,
        LocalDate from,
        LocalDate to,
        String jobTitle,
        String jobDetails,
        int months,
        Integer totalMinutes) {
}
