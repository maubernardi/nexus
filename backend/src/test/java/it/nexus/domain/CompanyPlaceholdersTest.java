package it.nexus.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import it.nexus.domain.enumeration.CompanyMissingData;

/** Segnaposto dei dati mancanti nel database attuale (US-508): riconosciuti stretti, rifiutati larghi. */
class CompanyPlaceholdersTest {

    private static boolean accepted(String regex, String value) {
        return Pattern.matches(regex, value);
    }

    @Test
    void nomeDellImportazione_riconosciuto() {
        assertThat(CompanyPlaceholders.name(1234)).isEqualTo("Azienda senza nome n. 1234");
        assertThat(CompanyPlaceholders.isName("Azienda senza nome n. 1234")).isTrue();
        assertThat(CompanyPlaceholders.isName("Trattoria da Mario")).isFalse();
        assertThat(CompanyPlaceholders.isName(null)).isFalse();
        assertThat(CompanyPlaceholders.isVatCode("00000000000")).isTrue();
        assertThat(CompanyPlaceholders.isVatCode("12345678901")).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Azienda senza nome n. 1234", "  azienda SENZA nome n. 12", "Azienda senza nome n.",
        "AZIENDA SENZA NOME N.5 e altro"})
    void nomeSegnaposto_rifiutatoDalModulo(String name) {
        assertThat(accepted(CompanyPlaceholders.NOT_NAME_REGEX, name)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Trattoria da Mario", "Azienda senza nomi S.r.l.", "La azienda senza nome n. 3", "",
        "Riga\nsu due righe"})
    void altriNomi_accettati(String name) {
        assertThat(accepted(CompanyPlaceholders.NOT_NAME_REGEX, name)).isTrue();
    }

    @Test
    void partitaIvaSegnaposto_rifiutataDalModulo() {
        assertThat(accepted(CompanyPlaceholders.NOT_VAT_CODE_REGEX, "00000000000")).isFalse();
        assertThat(accepted(CompanyPlaceholders.NOT_VAT_CODE_REGEX, " 00000000000 ")).isFalse();
        assertThat(accepted(CompanyPlaceholders.NOT_VAT_CODE_REGEX, "12345678901")).isTrue();
        assertThat(accepted(CompanyPlaceholders.NOT_VAT_CODE_REGEX, "000000000001")).isTrue();
    }

    @Test
    void datiMancantiDellAzienda_inOrdine() {
        assertThat(new Company(CompanyPlaceholders.name(7), CompanyPlaceholders.VAT_CODE).missingData())
                .containsExactly(CompanyMissingData.NAME, CompanyMissingData.VAT_CODE);
        assertThat(new Company("Bar Centrale", CompanyPlaceholders.VAT_CODE).missingData())
                .containsExactly(CompanyMissingData.VAT_CODE);
        assertThat(new Company("Bar Centrale", "12345678901").missingData()).isEmpty();
    }
}
