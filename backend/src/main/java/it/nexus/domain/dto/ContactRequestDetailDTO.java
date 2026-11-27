package it.nexus.domain.dto;

import java.util.List;

/**
 * Pagina della richiesta di contatto (US-512): la richiesta, la nota della chiusura, l'azienda dell'anagrafica collegata
 * e i tentativi dal più recente. {@code assignedToMe}: l'utente corrente ha la richiesta in carico, quindi segna i
 * tentativi e la chiude.
 */
public record ContactRequestDetailDTO(
        ContactRequestDTO request,
        String closingNote,
        /** Nome dell'azienda dell'anagrafica collegata alla chiusura; assente se non collegata. */
        String linkedCompanyName,
        List<ContactAttemptDTO> attempts,
        boolean assignedToMe) {
}
