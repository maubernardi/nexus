package it.nexus.domain.dto;

/** Esito dello spostamento: quante segnalazioni e a chi (US-111). */
public record TicketTransferResultDTO(int moved, String tutorName) {
}
