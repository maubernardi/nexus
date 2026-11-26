package it.nexus.services;

import java.util.Collection;
import java.util.Map;

import it.nexus.domain.dto.CompanyReviewsDTO;

/** Valutazioni dei tirocini delle aziende, dai report di restituzione (US-1104). */
public interface CompanyReviewService {

    /** Valutazioni per pagina. */
    int PAGE_SIZE = 10;

    /** Media delle medie e numero delle valutazioni di un'azienda. */
    record Summary(double average, long count) {
    }

    /**
     * Valutazioni di un'azienda, dalla più recente, una pagina alla volta.
     *
     * @throws it.nexus.web.errors.NotFoundException se l'azienda non esiste
     * @throws it.nexus.web.errors.BadRequestException se la pagina è negativa
     */
    CompanyReviewsDTO reviews(String companyId, int page);

    /** Media e numero delle valutazioni delle aziende indicate; quelle senza valutazioni non ci sono. */
    Map<Long, Summary> summaries(Collection<Long> companyIds);
}
