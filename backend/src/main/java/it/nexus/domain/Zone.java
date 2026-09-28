package it.nexus.domain;

import java.io.Serial;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Territorio della cooperativa, criterio di matching. */
@Entity
@Table(name = "zone")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Zone extends AbstractReferenceEntity {

    @Serial
    private static final long serialVersionUID = 1L;
}
