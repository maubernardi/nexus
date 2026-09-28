package it.nexus.domain;

import java.io.Serial;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Tipologia di mansione del catalogo (es. Magazziniere). */
@Entity
@Table(name = "job_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobCategory extends AbstractReferenceEntity {

    @Serial
    private static final long serialVersionUID = 1L;
}
