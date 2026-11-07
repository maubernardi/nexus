package it.nexus.services;

import java.time.Instant;
import java.util.List;

import it.nexus.domain.dto.BeneficiaryRetentionItemDTO;

/**
 * Conservazione dei dati dei beneficiari (US-1303, decisioni del PO del 07/11): il job notturno segna chi non ha attività
 * da più del periodo di conservazione; il Tutor proprietario o l'ADMIN confermano l'anonimizzazione oppure tengono i
 * dati per sempre con un motivo.
 */
public interface BeneficiaryRetentionService {

    /**
     * Controllo notturno: toglie il segno a chi ha avuto attività e segna chi non ne ha da prima di
     * {@code now - periodo}.
     *
     * @return quanti beneficiari sono stati segnati
     */
    int review(Instant now);

    /** Tutti i beneficiari, per l'ADMIN; solo quelli da anonimizzare se {@code onlyToAnonymize}. */
    List<BeneficiaryRetentionItemDTO> listAll(boolean onlyToAnonymize);

    /**
     * Anonimizza un beneficiario da anonimizzare: di nome e cognome restano le iniziali, si cancellano CV, vincoli e i
     * dati identificativi e le note delle schede dei tirocini.
     *
     * @throws it.nexus.web.errors.BadRequestException non è da anonimizzare
     * @throws org.springframework.security.access.AccessDeniedException né ADMIN né Tutor proprietario
     */
    void anonymize(String id);

    /** Tiene per sempre i dati di un beneficiario da anonimizzare, con il motivo. */
    void keep(String id, String reason);
}
