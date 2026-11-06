package it.nexus.domain.dto;

/**
 * Che cosa tocca disattivare un utente o cambiargli il ruolo (US-105): segnalazioni aperte di cui è il Tutor (le
 * blocca), ticket che ha in carico come operatore (tornano in coda) e se è l'ADMIN stesso (non può).
 */
public record UserImpactDTO(long openTickets, long ticketsInCharge, boolean self) {
}
