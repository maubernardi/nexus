package it.nexus.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import it.nexus.TestcontainersConfiguration;
import it.nexus.repository.ZoneRepository;

/** Le zone reali di partenza (V11) ci sono in ogni ambiente, anche senza dati demo. */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class InitialZonesIT {

    @Autowired ZoneRepository zones;

    @Test
    void quartieriDiFirenzeEComuniDellHinterland() {
        assertThat(zones.findAll())
                .filteredOn(z -> "system".equals(z.getCreatedBy()))
                .hasSize(40)
                .allMatch(Zone::isActive)
                .extracting(Zone::getName)
                .contains("Novoli", "San Niccolò", "Porta al Prato", "Casellina", "Scarperia e San Piero", "Fiesole");
        assertThat(zones.findByCode("CAMPO_DI_MARTE")).get().extracting(Zone::getName).isEqualTo("Campo di Marte");
    }
}
