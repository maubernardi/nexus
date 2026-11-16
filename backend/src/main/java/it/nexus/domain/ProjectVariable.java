package it.nexus.domain;

import java.io.Serial;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Variabile libera di un progetto (US-1013): il nome è il segnaposto {@code {NOME}} dei modelli del progetto, il valore
 * quello che compare nei documenti. Il nome non cambia: una variabile con un altro nome è un'altra variabile.
 */
@Entity
@Table(name = "project_variable")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectVariable extends AbstractTsidEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, updatable = false)
    private Project project;

    @Column(name = "name", nullable = false, updatable = false, length = 50)
    private String name;

    @Setter
    @Column(name = "value", nullable = false, length = 500)
    private String value;

    @Setter
    @Column(name = "description", length = 300)
    private String description;

    public ProjectVariable(Project project, String name, String value, String description) {
        this.project = project;
        this.name = name;
        this.value = value;
        this.description = description;
    }
}
