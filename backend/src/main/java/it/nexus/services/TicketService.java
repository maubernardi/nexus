package it.nexus.services;

import java.util.List;

import it.nexus.domain.dto.ApproveCategoryDTO;
import it.nexus.domain.dto.JobSlotMatchDTO;
import it.nexus.domain.dto.MatchRequestDTO;
import it.nexus.domain.dto.RejectCategoryDTO;
import it.nexus.domain.dto.TicketListItemDTO;
import it.nexus.domain.dto.TicketCreateDTO;
import it.nexus.domain.dto.TicketDTO;
import it.nexus.domain.dto.TicketDetailDTO;
import it.nexus.domain.dto.TicketEventDTO;
import it.nexus.domain.enumeration.TicketListView;
import it.nexus.domain.enumeration.TicketStatus;

public interface TicketService {

    /** Segnalazione normale del Tutor corrente: nasce in {@code NUOVA} ed entra nella coda del Call Center. */
    TicketDTO submit(TicketCreateDTO dto);


    /** Presa in carico da parte dell'operatore corrente (US-402); 409 se un collega l'ha già presa. */
    TicketListItemDTO takeCharge(String ticketId, long expectedVersion);


    /**
     * Dettaglio della segnalazione (US-305): Call Center e ADMIN vedono tutte le segnalazioni, il Tutor le proprie e
     * quelle dei suoi progetti; altrimenti 404 (non si rivela l'esistenza).
     */
    TicketDetailDTO getDetail(String ticketId);

    /** Cronologia della segnalazione, dalla più vecchia, con le stesse regole di visibilità del dettaglio. */
    List<TicketEventDTO> history(String ticketId);

    /**
     * Elenco delle segnalazioni della Home (US-303) nella vista indicata (null = predefinita per il ruolo), con filtri
     * facoltativi per stato e progetto.
     *
     * @throws it.nexus.web.errors.BadRequestException se la vista non è consentita al ruolo o il filtro non è valido
     */
    List<TicketListItemDTO> list(TicketListView view, TicketStatus status, String projectId);

    /** Mansioni proponibili per la segnalazione; filtri facoltativi (id esterni, null = tutte). */
    List<JobSlotMatchDTO> compatibleJobSlots(String ticketId, String zoneId, String jobCategoryId);

    /**
     * Abbina la mansione e propone il beneficiario all'azienda: ticket in PROPOSTA_AZIENDA, mansione BLOCCATA.
     *
     * @throws it.nexus.web.errors.ConflictException se ticket o mansione sono cambiati o la mansione non è più proponibile
     */
    TicketDetailDTO match(String ticketId, MatchRequestDTO request);

    /** "Prendila in carico tu": la segnalazione in carico a un collega passa all'operatore corrente (US-405). */
    TicketDetailDTO takeOver(String ticketId, long expectedVersion);

    /**
     * Il Tutor della segnalazione accetta la proposta (US-701): stato PROPOSTA_ACCOLTA e timer fino al 7° giorno
     * lavorativo successivo; evento nello storico dell'azienda.
     */
    TicketDetailDTO accept(String ticketId, long expectedVersion);

    /** Il Call Center approva la nuova tipologia proposta dal Tutor, che entra nel catalogo (US-302). */
    TicketDetailDTO approveCategory(String ticketId, ApproveCategoryDTO dto);

    /** Il Call Center respinge la nuova tipologia e sceglie quella del catalogo, con il motivo (US-302). */
    TicketDetailDTO rejectCategory(String ticketId, RejectCategoryDTO dto);

    /** Il Tutor della segnalazione ha visto il cambio di tipologia: l'evidenza nella Home si spegne (US-302). */
    void markCategoryChangeSeen(String ticketId);
}
