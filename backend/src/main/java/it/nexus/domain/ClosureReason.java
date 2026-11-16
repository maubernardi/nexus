package it.nexus.domain;

import java.io.Serial;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Motivo di chiusura del tirocinio (US-1103): catalogo dell'ADMIN. {@link #REGULAR} è il motivo automatico dei tirocini
 * chiusi alla data prevista e non si disattiva; le altre voci attive sono le interruzioni anticipate.
 */
@Entity
@Table(name = "closure_reason")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClosureReason extends AbstractReferenceEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Codice della chiusura regolare, fissato da V41. */
    public static final String REGULAR = "CHIUSURA_REGOLARE";

    /** Nuova voce del catalogo. */
    public ClosureReason(String code, String name) {
        setCode(code);
        setName(name);
    }

    public boolean isRegular() {
        return REGULAR.equals(getCode());
    }
}
