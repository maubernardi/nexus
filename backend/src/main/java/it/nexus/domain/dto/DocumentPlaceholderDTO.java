package it.nexus.domain.dto;

/** Un segnaposto dei modelli per l'ADMIN (US-1011): si scrive {@code {name}}; gruppo e descrizione per la pagina. */
public record DocumentPlaceholderDTO(String name, String group, String description) {
}
