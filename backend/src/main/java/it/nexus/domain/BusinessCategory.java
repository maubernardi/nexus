package it.nexus.domain;

import java.io.Serial;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Categoria merceologica di un'azienda (US-509): catalogo dell'ADMIN. Le voci iniziali sono le categorie del database
 * attuale, con l'id {@link #LEGACY_ID_BASE} + il numero di Access.
 */
@Entity
@Table(name = "business_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BusinessCategory extends AbstractReferenceEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Base degli id delle voci iniziali (V46): l'importazione (US-1309) ci somma il numero della categoria in Access. */
    public static final long LEGACY_ID_BASE = 893700000000000400L;

    /** Nuova voce del catalogo. */
    public BusinessCategory(String code, String name) {
        setCode(code);
        setName(name);
    }
}
