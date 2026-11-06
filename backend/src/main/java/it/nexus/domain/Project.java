package it.nexus.domain;

import java.io.Serial;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Progetto finanziato (es. GOL, POLIS, FSE). */
@Entity
@Table(name = "project")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project extends AbstractReferenceEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Nuovo progetto del catalogo (US-102). */
    public Project(String code, String name) {
        setCode(code);
        setName(name);
    }
}
