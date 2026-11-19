package it.nexus.domain.dto;

/** Referente dell'azienda (US-510); i campi vuoti sono assenti. */
public record CompanyContactDTO(
        String name,
        /** Ruolo dal catalogo, anche se nel frattempo disattivato. */
        ReferenceItemDTO role,
        String phone,
        String mobile,
        String email) {
}
