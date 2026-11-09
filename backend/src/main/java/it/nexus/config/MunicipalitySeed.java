package it.nexus.config;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import it.nexus.repository.MunicipalityImportRepository;
import it.nexus.services.MunicipalityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Dati dimostrativi (profilo {@code seed}): se l'elenco dei comuni è vuoto, carica il file ISTAT incluso nel codice
 * (US-114). In produzione lo carica l'ADMIN dai Cataloghi.
 */
@Slf4j
@Component
@Profile("seed")
@RequiredArgsConstructor
public class MunicipalitySeed implements ApplicationRunner {

    static final String FILE = "seed/istat/Elenco-comuni-italiani.csv";
    /** Data da cui vale l'elenco della demo: fissa, così i dati dimostrativi non cambiano da un avvio all'altro. */
    static final LocalDate VALID_FROM = LocalDate.of(2026, 1, 1);

    private final MunicipalityImportRepository imports;
    private final MunicipalityService municipalityService;

    @Override
    public void run(ApplicationArguments args) throws IOException {
        if (imports.count() > 0) {
            return;
        }
        try (InputStream in = new ClassPathResource(FILE).getInputStream()) {
            var result = municipalityService.importFile(in.readAllBytes(), VALID_FROM);
            log.info("Elenco dei comuni della demo caricato: {} comuni, {} province", result.municipalities().added(),
                    result.provinces().added());
        }
    }
}
