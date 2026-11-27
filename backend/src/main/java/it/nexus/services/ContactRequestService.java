package it.nexus.services;

import java.util.List;

import it.nexus.domain.dto.ContactRequestDTO;
import it.nexus.domain.dto.ContactRequestFormDTO;

/** Richieste di contatto di un'azienda dal Tutor al Call Center (US-507). */
public interface ContactRequestService {

    /** Invio da parte del Tutor corrente: azienda già presente e attiva, oppure nuova con nome e zona. */
    ContactRequestDTO create(ContactRequestFormDTO form);

    /** Le richieste del Tutor corrente, dalla più recente. */
    List<ContactRequestDTO> mine();

    /** Le richieste aperte e in carico, dalla più vecchia, per il Call Center. */
    List<ContactRequestDTO> queue();

    /** Richieste ancora da prendere in carico. */
    long openCount();

    /** Presa in carico da parte dell'operatore corrente (409 se già presa o cambiata). */
    ContactRequestDTO take(String id, long expectedVersion);
}
