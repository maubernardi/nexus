package it.nexus.services;

import java.time.Instant;
import java.util.List;

import it.nexus.domain.AppUser;
import it.nexus.domain.dto.InternshipToCloseDTO;

/**
 * Fine dei tirocini (US-1203, PO 22/11): avvisi al Tutor prima e dopo la fine prevista, e i tirocini finiti da chiudere
 * che bloccano le sue nuove segnalazioni. Giorni di calendario, ora di Roma.
 */
public interface InternshipEndReminderService {

    /**
     * Manda gli avvisi dovuti: 7 e 3 giorni prima della fine prevista (con le proroghe) «la convenzione è in chiusura»,
     * 3 e 7 giorni dopo «chiudi o proroga». Per ogni fine parte ogni avviso una volta sola; se ne è saltato qualcuno,
     * parte solo il più recente. Una proroga cambia la fine e gli avvisi ripartono.
     *
     * @return gli avvisi inviati
     */
    int sendDue(Instant now);

    /** Tirocini in corso del Tutor con la fine prevista già passata, dal numero di segnalazione più basso. */
    List<InternshipToCloseDTO> toClose(AppUser tutor);
}
