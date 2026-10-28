package it.nexus.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

class ReferenceCodesTest {

    @Test
    void dalNome_maiuscoleSenzaAccentiConUnderscore() {
        assertThat(ReferenceCodes.fromName("Addetto vendite", "X", c -> false)).isEqualTo("ADDETTO_VENDITE");
        assertThat(ReferenceCodes.fromName("  Città – Oltrarno ", "X", c -> false)).isEqualTo("CITTA_OLTRARNO");
    }

    @Test
    void nomeSenzaLettereNeCifre_codiceDiRiserva() {
        assertThat(ReferenceCodes.fromName("!!!", "ZONA", c -> false)).isEqualTo("ZONA");
    }

    @Test
    void codiceGiaUsato_suffissoNumerico() {
        Set<String> taken = Set.of("MAGAZZINIERE", "MAGAZZINIERE_2");
        assertThat(ReferenceCodes.fromName("Magazziniere", "X", taken::contains)).isEqualTo("MAGAZZINIERE_3");
    }

    @Test
    void nomeLungo_alPiu30CaratteriAncheConSuffisso() {
        String code = ReferenceCodes.fromName("Operatore specializzato logistica integrata", "X", c -> !c.endsWith("_2"));
        assertThat(code).hasSizeLessThanOrEqualTo(30).endsWith("_2").startsWith("OPERATORE_SPECIALIZZATO_LO");
    }
}
