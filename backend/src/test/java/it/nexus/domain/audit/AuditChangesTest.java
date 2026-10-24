package it.nexus.domain.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

class AuditChangesTest {

    @Test
    void valoriUgualiIgnorati_datiPersonaliSenzaValore() {
        Map<String, Object> changes = AuditChanges.of()
                .value("status", "NUOVA", "IN_LAVORAZIONE")
                .value("projectId", "P1", "P1")
                .redacted("lastName")
                .asMap();

        assertThat(changes).containsOnlyKeys("status", "lastName");
        assertThat(changes.get("lastName")).isEqualTo(Map.of("redacted", true));
    }

    @Test
    void datoPersonaleRegistratoSoloSeCambiato() {
        Map<String, Object> changes = AuditChanges.of()
                .redactedIfChanged("email", "a@x.it", "a@x.it")
                .redactedIfChanged("phone", null, "055 1")
                .asMap();

        assertThat(changes).containsOnlyKeys("phone");
        assertThat(changes.get("phone")).isEqualTo(Map.of("redacted", true));
    }
}
