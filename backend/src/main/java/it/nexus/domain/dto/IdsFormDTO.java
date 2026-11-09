package it.nexus.domain.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Un insieme di voci scelte dall'ADMIN (es. i progetti di una sede, i tipi ammessi da un progetto). */
public record IdsFormDTO(@NotNull @Size(max = 100) List<@NotNull String> ids) {
}
