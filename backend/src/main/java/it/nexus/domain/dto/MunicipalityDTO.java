package it.nexus.domain.dto;

import java.time.LocalDate;

/** Versione di un comune con la sua provincia (US-114); {@code validTo} assente = ancora valido. */
public record MunicipalityDTO(String id, String name, String istatCode, String cadastralCode, String provinceName,
        String provinceAbbreviation, LocalDate validFrom, LocalDate validTo) {
}
