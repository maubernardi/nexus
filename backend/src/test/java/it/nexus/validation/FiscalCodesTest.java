package it.nexus.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class FiscalCodesTest {

    @Test
    void codiceValido_ancheMinuscoloOConSpazi() {
        assertThat(FiscalCodes.isValid("RSSMRA85T10A562S")).isTrue();
        assertThat(FiscalCodes.isValid(" rssmra85t10a562s ")).isTrue();
        assertThat(FiscalCodes.isValid("BNCLRA80A41H501D")).isTrue();
    }

    @Test
    void carattereDiControlloSbagliato() {
        assertThat(FiscalCodes.isValid("RSSMRA85T10A562T")).isFalse();
    }

    @Test
    void strutturaSbagliata() {
        assertThat(FiscalCodes.isValid("RSSMRA85T10A562")).isFalse();
        assertThat(FiscalCodes.isValid("12345678901")).isFalse();
        // mese inesistente (Z non è una lettera dei mesi)
        assertThat(FiscalCodes.isValid("RSSMRA85Z10A562S")).isFalse();
    }

    @Test
    void corrispondenzaConLaDataDiNascita() {
        assertThat(FiscalCodes.matchesBirthDate("RSSMRA85T10A562S", LocalDate.of(1985, 12, 10))).isTrue();
        // donna: giorno + 40
        assertThat(FiscalCodes.matchesBirthDate("bnclra80a41h501d", LocalDate.of(1980, 1, 1))).isTrue();
        // omocodia: il giorno 10 scritto "1L"
        assertThat(FiscalCodes.isValid("RSSMRA85T1LARSNR")).isTrue();
        assertThat(FiscalCodes.matchesBirthDate("RSSMRA85T1LARSNR", LocalDate.of(1985, 12, 10))).isTrue();

        assertThat(FiscalCodes.matchesBirthDate("RSSMRA85T10A562S", LocalDate.of(1985, 12, 11))).isFalse();
        assertThat(FiscalCodes.matchesBirthDate("RSSMRA85T10A562S", LocalDate.of(1985, 11, 10))).isFalse();
        assertThat(FiscalCodes.matchesBirthDate("RSSMRA85T10A562S", LocalDate.of(1986, 12, 10))).isFalse();
    }
}
