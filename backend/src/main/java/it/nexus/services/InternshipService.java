package it.nexus.services;

import it.nexus.domain.Ticket;
import it.nexus.domain.dto.AtecoCodeDTO;
import it.nexus.domain.dto.AtecoCodeFormDTO;
import it.nexus.domain.dto.InternshipDTO;
import it.nexus.domain.dto.InternshipFormDTO;
import it.nexus.domain.dto.LegalRepresentativeDTO;
import it.nexus.domain.dto.LegalRepresentativeFormDTO;

/**
 * Scheda amministrativa del tirocinio (US-1001). È solo del Tutor della segnalazione: i documenti sono di sua
 * competenza (PO, 29/10). C'è dall'appuntamento fissato in poi e il Tutor la modifica in qualunque momento, anche a
 * tirocinio avviato (PO, 29/10).
 */
public interface InternshipService {

    /** Scheda della segnalazione, anche mai salvata (versione nulla), con i dati dell'azienda per i documenti. */
    InternshipDTO get(String ticketId);

    /**
     * Salva la scheda, anche incompleta.
     *
     * @throws it.nexus.web.errors.ConflictException se la segnalazione non è ancora arrivata all'appuntamento fissato o
     *         la versione è superata
     * @throws it.nexus.web.errors.FieldValidationException se un dato presente non è valido
     */
    InternshipDTO save(String ticketId, InternshipFormDTO dto);

    /**
     * Inserisce il legale rappresentante dell'azienda della segnalazione, solo se manca: correggerlo spetta al Call
     * Center (PO, 29/10).
     */
    LegalRepresentativeDTO addLegalRepresentative(String ticketId, LegalRepresentativeFormDTO dto);

    /** Aggiunge un codice ATECO all'azienda della segnalazione, per sceglierlo nella scheda. */
    AtecoCodeDTO addAtecoCode(String ticketId, AtecoCodeFormDTO dto);

    /**
     * Appuntamento non andato a buon fine: della scheda restano solo i dati del beneficiario.
     *
     * @return {@code true} se c'era qualcosa da azzerare
     */
    boolean clearCompanyData(Ticket ticket);
}
