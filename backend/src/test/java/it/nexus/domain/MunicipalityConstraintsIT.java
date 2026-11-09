package it.nexus.domain;

import static it.nexus.domain.DomainConstraintsIT.assertViolates;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.TestcontainersConfiguration;
import it.nexus.repository.MunicipalityImportRepository;
import it.nexus.repository.MunicipalityRepository;
import it.nexus.repository.ProvinceRepository;

/** Vincoli di comuni e province (US-114), verificati per nome su PostgreSQL. */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class MunicipalityConstraintsIT {

    private static final LocalDate JANUARY = LocalDate.of(2026, 1, 1);

    @Autowired ProvinceRepository provinces;
    @Autowired MunicipalityRepository municipalities;
    @Autowired MunicipalityImportRepository imports;

    private Province firenze() {
        return provinces.saveAndFlush(new Province("248", "Firenze", "FI", JANUARY));
    }

    @Test
    void unaSolaProvinciaAttualePerCodice() {
        firenze();
        assertViolates("uq_province_current_code",
                () -> provinces.saveAndFlush(new Province("248", "Firenze bis", "FI", JANUARY)));
    }

    @Test
    void siglaDellaProvinciaFuoriFormato() {
        assertViolates("ck_province_abbreviation",
                () -> provinces.saveAndFlush(new Province("248", "Firenze", "Fi", JANUARY)));
    }

    @Test
    void unSoloComuneAttualePerCodiceCatastale() {
        Province fi = firenze();
        municipalities.saveAndFlush(new Municipality("D612", "048017", "Firenze", fi, JANUARY));
        assertViolates("uq_municipality_current_cadastral_code",
                () -> municipalities.saveAndFlush(new Municipality("D612", "048017", "Firenze", fi, JANUARY)));
    }

    @Test
    void versioneChiusaPrimaDiAprirsi() {
        Municipality m = municipalities.saveAndFlush(
                new Municipality("D612", "048017", "Firenze", firenze(), JANUARY));
        m.close(JANUARY);
        assertViolates("ck_municipality_validity", () -> municipalities.saveAndFlush(m));
    }

    @Test
    void codiceCatastaleFuoriFormato() {
        Province fi = firenze();
        assertViolates("ck_municipality_cadastral_code",
                () -> municipalities.saveAndFlush(new Municipality("d612", "048017", "Firenze", fi, JANUARY)));
    }

    @Test
    void codiceIstatFuoriFormato() {
        Province fi = firenze();
        assertViolates("ck_municipality_istat_code",
                () -> municipalities.saveAndFlush(new Municipality("D612", "48017", "Firenze", fi, JANUARY)));
    }

    @Test
    void unCaricamentoPerData() {
        imports.saveAndFlush(new MunicipalityImport(JANUARY, 1, 0, 0, 1, 0, 0));
        assertViolates("uq_municipality_import_valid_from",
                () -> imports.saveAndFlush(new MunicipalityImport(JANUARY, 0, 0, 0, 0, 0, 0)));
    }
}
