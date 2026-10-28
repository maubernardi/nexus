package it.nexus.services;

import java.util.Collection;
import java.util.List;

import it.nexus.domain.JobSlot;
import it.nexus.domain.Ticket;
import it.nexus.domain.dto.BoardPostCreateDTO;
import it.nexus.domain.dto.BoardPostDTO;
import it.nexus.domain.dto.BoardPostFormDTO;
import it.nexus.domain.dto.TutorBoardPostDTO;
import it.nexus.domain.enumeration.BoardPostStatus;

/** Gestione della bacheca per Call Center e ADMIN (US-902). */
public interface BoardPostService {

    List<BoardPostDTO> list(BoardPostStatus status);

    BoardPostDTO get(String id);

    /** Bacheca del Tutor corrente (US-901): pubblicati per tutti o per i suoi progetti, senza azienda, titolo e note. */
    List<TutorBoardPostDTO> listForTutor(String zoneId, String jobCategoryId);

    /** Bozza da una mansione libera e a disposizione, senza altri annunci attivi (US-904). */
    BoardPostDTO create(BoardPostCreateDTO dto);

    BoardPostDTO update(String id, BoardPostFormDTO dto);

    BoardPostDTO publish(String id, long expectedVersion);

    BoardPostDTO archive(String id, long expectedVersion);

    /**
     * Archivia gli annunci attivi delle mansioni non più disponibili (abbinate, ritirate, azienda disattivata): un
     * annuncio vive solo finché la sua mansione si può proporre (decisione del PO, 28/10). Va chiamato nella
     * transazione dell'operazione che rende la mansione indisponibile.
     *
     * @return i numeri degli annunci archiviati
     */
    /**
     * Bozza del rilancio in bacheca dopo un appuntamento fallito (US-803): mansione appena liberata, riservata al
     * progetto della segnalazione e collegata a essa. Va chiamato nella transazione dell'esito dell'appuntamento.
     *
     * @return il numero dell'annuncio
     */
    Long createRelaunchDraft(JobSlot slot, Ticket ticket);

    List<Long> archiveForUnavailableSlots(Collection<Long> jobSlotIds, String reason);
}
