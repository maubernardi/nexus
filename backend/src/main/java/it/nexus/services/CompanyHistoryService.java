package it.nexus.services;

import it.nexus.domain.dto.CompanyHistoryDTO;

public interface CompanyHistoryService {

    /** Storico e pregressi dell'azienda: riepilogo ed eventi dalla più recente, esclusioni comprese. */
    CompanyHistoryDTO history(String companyId);
}
