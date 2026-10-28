package it.nexus.domain.dto;

import java.time.Instant;
import java.time.LocalDate;

import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.domain.enumeration.TicketType;

/** Riga dell'elenco delle segnalazioni (Home, US-303); {@code version} serve alle azioni (presa in carico). */
public record TicketListItemDTO(
        String id,
        Long number,
        TicketType type,
        TicketStatus status,
        boolean fastTrack,
        long version,
        BeneficiarySummaryDTO beneficiary,
        ReferenceItemDTO project,
        ReferenceItemDTO requestedJobCategory,
        /** Nuova tipologia proposta dal Tutor, in attesa di approvazione (US-302). */
        String requestedJobFreeText,
        String tutorName,
        /** Operatore che ha in carico la segnalazione (null se nessuno). */
        String assignedOperatorName,
        Instant createdAt,
        /** Ultimo giorno utile del timer attivo (US-307), null se non c'è scadenza. */
        LocalDate timerDeadline,
        /** Promemoria del timer già inviato: la scadenza è vicina (US-307). */
        boolean timerDueSoon) {
}
