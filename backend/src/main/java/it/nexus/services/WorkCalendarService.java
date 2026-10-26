package it.nexus.services;

import it.nexus.domain.dto.ClosureFormDTO;
import it.nexus.domain.dto.PatronFormDTO;
import it.nexus.domain.dto.WorkCalendarDTO;

public interface WorkCalendarService {

    /** Calendario unico: patrono e chiusure. */
    WorkCalendarDTO get();

    /** Cambia il patrono (blocco ottimistico sulla versione); audit con prima e dopo. */
    WorkCalendarDTO updatePatron(PatronFormDTO dto);

    /** Aggiunge una chiusura (un giorno o un periodo); audit con i valori. */
    WorkCalendarDTO addClosure(ClosureFormDTO dto);

    /** Rimuove una chiusura; audit con i valori rimossi. */
    WorkCalendarDTO removeClosure(String closureId);
}
