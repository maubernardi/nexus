package it.nexus.domain;

import java.io.Serial;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Ruolo di un referente dell'azienda (US-510): catalogo dell'ADMIN. Le voci iniziali sono i «Tipi di contatti» del
 * database attuale, ripuliti, con l'id {@link #LEGACY_ID_BASE} + il numero di Access.
 */
@Entity
@Table(name = "contact_role")
@BatchSize(size = 50)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContactRole extends AbstractReferenceEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Base degli id delle voci iniziali (V47): l'importazione (US-1309) ci somma il numero del ruolo in Access. */
    public static final long LEGACY_ID_BASE = 893700000000000500L;

    /** Nuova voce del catalogo. */
    public ContactRole(String code, String name) {
        setCode(code);
        setName(name);
    }
}
