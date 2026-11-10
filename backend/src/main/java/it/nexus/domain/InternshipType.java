package it.nexus.domain;

import java.io.Serial;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Tipo di tirocinio (es. Curriculare): catalogo dell'ADMIN, determina il tipo di documento (US-108). */
@Entity
@Table(name = "internship_type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InternshipType extends AbstractReferenceEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Codice del tipo «Di inclusione» (voce iniziale della V21): richiede ente e case manager (US-206). */
    public static final String INCLUSION = "INCLUSIONE";

    /** Nuovo tipo del catalogo (US-108). */
    public InternshipType(String code, String name) {
        setCode(code);
        setName(name);
    }

    public boolean isInclusion() {
        return INCLUSION.equals(getCode());
    }
}
