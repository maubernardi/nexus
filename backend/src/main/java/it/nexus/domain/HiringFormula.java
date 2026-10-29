package it.nexus.domain;

import java.io.Serial;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Formula di assunzione del report di restituzione (US-109): catalogo dell'ADMIN. */
@Entity
@Table(name = "hiring_formula")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HiringFormula extends AbstractReferenceEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Nuova voce del catalogo (US-109). */
    public HiringFormula(String code, String name) {
        setCode(code);
        setName(name);
    }
}
