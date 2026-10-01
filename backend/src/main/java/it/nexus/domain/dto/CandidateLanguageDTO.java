package it.nexus.domain.dto;

import it.nexus.domain.enumeration.LanguageLevel;
import it.nexus.validation.IsoLanguage;
import jakarta.validation.constraints.NotNull;

public record CandidateLanguageDTO(@NotNull @IsoLanguage String language, @NotNull LanguageLevel level) {
}
