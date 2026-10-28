package it.nexus.domain.dto;

import java.time.Instant;

/**
 * Annuncio come lo vede il Tutor (US-901): solo dati strutturati. Mai azienda, titolo né note, che potrebbero
 * contenere il nome dell'azienda (review dello Sprint 5, §8.2). {@code project} è il progetto del Tutor a cui
 * l'annuncio è riservato, null se è per tutti.
 */
public record TutorBoardPostDTO(
        String id,
        Long number,
        ReferenceItemDTO jobCategory,
        ReferenceItemDTO zone,
        Integer ageMin,
        Integer ageMax,
        Integer weeklyHours,
        Integer durationMonths,
        ReferenceItemDTO project,
        Instant publishedAt) {
}
