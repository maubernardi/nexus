package it.nexus.domain.dto;

/** Voce di un catalogo per l'ADMIN (US-106), anche disattivata. */
public record CatalogItemDTO(String id, String code, String name, boolean active) {
}
