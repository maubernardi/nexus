package it.nexus.validation;

import static org.assertj.core.api.Assertions.assertThat;

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
}
