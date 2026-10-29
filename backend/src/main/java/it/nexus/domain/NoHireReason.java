package it.nexus.domain;

import java.io.Serial;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Motivo della mancata assunzione del report di restituzione (US-109): catalogo dell'ADMIN. */
@Entity
@Table(name = "no_hire_reason")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoHireReason extends AbstractReferenceEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Nuova voce del catalogo (US-109). */
    public NoHireReason(String code, String name) {
        setCode(code);
        setName(name);
    }
}
