package it.nexus.services;

import java.util.List;

import it.nexus.domain.dto.ContactAttemptFormDTO;
import it.nexus.domain.dto.ContactRequestCloseDTO;
import it.nexus.domain.dto.ContactRequestDTO;
import it.nexus.domain.dto.ContactRequestDetailDTO;
import it.nexus.domain.dto.ContactRequestFormDTO;

/** Richieste di contatto di un'azienda dal Tutor al Call Center (US-507). */
public interface ContactRequestService {

    /** Invio da parte del Tutor corrente, per un'azienda non ancora in anagrafica: nome e zona, categoria facoltativa. */
    ContactRequestDTO create(ContactRequestFormDTO form);

    /** Le richieste del Tutor corrente, dalla più recente. */
    List<ContactRequestDTO> mine();

    /** Le richieste aperte e in carico per il Call Center: prima quelle da prendere in carico, dalla più vecchia. */
    List<ContactRequestDTO> queue();

    /** Richieste ancora da prendere in carico. */
    long openCount();

    /** Presa in carico da parte dell'operatore corrente (409 se già presa o cambiata). */
    ContactRequestDTO take(String id, long expectedVersion);

    /** Pagina della richiesta (US-512): il Tutor solo le sue, Call Center e ADMIN tutte; 404 altrimenti. */
    ContactRequestDetailDTO detail(String id);

    /** Tentativo di contatto, solo da parte dell'operatore che ha la richiesta in carico (US-512). */
    ContactRequestDetailDTO addAttempt(String id, ContactAttemptFormDTO form);

    /** Chiusura con l'esito, solo da parte dell'operatore che ha la richiesta in carico; avvisa il Tutor (US-512). */
    ContactRequestDetailDTO close(String id, ContactRequestCloseDTO form);
}
