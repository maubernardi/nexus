package it.nexus.services;

/**
 * Filtri dell'elenco delle aziende: testo nel nome o nella partita IVA, anche le disattivate, solo quelle da completare
 * (US-508), categoria merceologica (US-509), stelle e pollici minimi (US-514). {@code null} = filtro non usato.
 */
public record CompanySearch(String search, boolean includeInactive, boolean toComplete, String businessCategoryId,
        Integer minReliability, Integer minProjectInterest) {
}
