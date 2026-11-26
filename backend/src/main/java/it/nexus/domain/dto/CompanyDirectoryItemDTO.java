package it.nexus.domain.dto;

/**
 * Azienda nell'elenco del Tutor (US-511): solo i dati essenziali, per minimizzazione. Niente partita IVA, recapiti,
 * referenti, note, mansioni o storico. Indirizzo e comune sono quelli della sede operativa, se indicata, altrimenti
 * della sede legale.
 */
public record CompanyDirectoryItemDTO(
        String id,
        String name,
        /** Settore: la categoria merceologica (US-509); assente se non indicata. */
        ReferenceItemDTO businessCategory,
        /** Zona del catalogo (US-511); assente se non indicata. */
        ReferenceItemDTO zone,
        String address,
        /** Comune con la sigla della provincia, per esempio «Prato (PO)». */
        String municipality,
        /** Stelle (1–4) e pollici (1–2) del Call Center (US-514); assenti se non valutata. */
        Integer reliability,
        Integer projectInterest) {
}
