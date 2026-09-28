package it.nexus.domain;

import java.io.Serial;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/** Tabella di riferimento: codice unico (maiuscole, cifre, underscore), nome, stato attivo. */
@Getter
@Setter
@MappedSuperclass
public abstract class AbstractReferenceEntity extends AbstractTsidEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
