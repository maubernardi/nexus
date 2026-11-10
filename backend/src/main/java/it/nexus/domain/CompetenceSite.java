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

/**
 * Sede di competenza (US-115): il luogo dei documenti di fine tirocinio. Copre più comuni e vale per uno o più
 * progetti.
 */
@Getter
@Entity
@Table(name = "competence_site")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompetenceSite extends AbstractReferenceEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @ManyToMany
    @JoinTable(name = "competence_site_project", joinColumns = @JoinColumn(name = "competence_site_id"),
            inverseJoinColumns = @JoinColumn(name = "project_id"))
    private Set<Project> projects = new HashSet<>();

    public CompetenceSite(String code, String name) {
        setCode(code);
        setName(name);
    }
}
