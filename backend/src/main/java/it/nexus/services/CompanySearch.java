package it.nexus.services;

/**
 * Filtri dell'elenco delle aziende: testo nel nome o nella partita IVA, anche le disattivate, solo quelle da completare
 * (US-508), categoria merceologica (US-509), zona (US-511), stelle e pollici minimi (US-514), solo le navi scuola
 * (US-515). {@code null} = filtro non usato.
 */
public record CompanySearch(String search, boolean includeInactive, boolean toComplete, String businessCategoryId,
        String zoneId, Integer minReliability, Integer minProjectInterest, boolean trainingShip) {
}
