package it.nexus.domain.dto;

/** Riepilogo per elenchi e selezioni. */
public record CandidateSummaryDTO(String id, String firstName, String lastName, Integer birthYear, String residenceZoneName) {
}
