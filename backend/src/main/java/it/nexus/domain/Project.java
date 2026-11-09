package it.nexus.domain;

import java.io.Serial;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Progetto finanziato (es. GOL, POLIS, FSE). */
@Entity
@Table(name = "project")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project extends AbstractReferenceEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Tipi di tirocinio ammessi (US-115); vuoto = tutti i tipi, fino al censimento dei progetti. */
    @Getter
    @ManyToMany
    @JoinTable(name = "project_internship_type", joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "internship_type_id"))
    private Set<InternshipType> allowedInternshipTypes = new HashSet<>();

    /** Nuovo progetto del catalogo (US-102). */
    public Project(String code, String name) {
        setCode(code);
        setName(name);
    }
}
