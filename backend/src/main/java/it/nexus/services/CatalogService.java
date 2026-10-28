package it.nexus.services;

import java.util.List;

import it.nexus.domain.dto.CatalogFormDTO;
import it.nexus.domain.dto.CatalogItemDTO;
import it.nexus.domain.enumeration.CatalogKind;

/**
 * Cataloghi di zone e tipologie di mansione per l'ADMIN (US-106): le voci si aggiungono, si rinominano e si
 * disattivano, mai si cancellano. Una voce disattivata non è più selezionabile ma resta sui dati esistenti.
 */
public interface CatalogService {

    /** Tutte le voci, prima le attive, per nome. */
    List<CatalogItemDTO> list(CatalogKind kind);

    /** @throws it.nexus.web.errors.FieldValidationException se una voce attiva ha già lo stesso nome */
    CatalogItemDTO create(CatalogKind kind, CatalogFormDTO dto);

    CatalogItemDTO rename(CatalogKind kind, String id, CatalogFormDTO dto);

    /** @throws it.nexus.web.errors.ConflictException riattivando una voce con il nome di un'altra attiva */
    CatalogItemDTO setActive(CatalogKind kind, String id, boolean active);
}
