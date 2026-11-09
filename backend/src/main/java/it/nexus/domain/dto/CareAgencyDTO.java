package it.nexus.domain.dto;

import java.time.LocalDate;

import it.nexus.domain.enumeration.CareAgencyKind;

/** Ente che ha in carico i beneficiari, con i dati per i documenti di inclusione (US-115). */
public record CareAgencyDTO(String id, String code, String name, boolean active, CareAgencyKind kind,
        String fiscalCode, String legalAddress, String representativeName, String representativeRole,
        String representativeBirthPlace, LocalDate representativeBirthDate, String referentName,
        String referentPhone, String referentEmail, long version) {
}
