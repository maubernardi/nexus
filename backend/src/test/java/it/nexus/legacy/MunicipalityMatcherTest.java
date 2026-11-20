package it.nexus.legacy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import it.nexus.legacy.MunicipalityMatcher.Candidate;
import it.nexus.legacy.MunicipalityMatcher.Match;
import it.nexus.legacy.MunicipalityMatcher.Rule;

/** Comuni scritti come nel database attuale (forme viste nel file vero, US-1309), con gli alias del codice. */
class MunicipalityMatcherTest {

    private static final List<Candidate> MUNICIPALITIES = List.of(
            new Candidate(1, "Firenze", "FI"),
            new Candidate(2, "Scandicci", "FI"),
            new Candidate(3, "Sesto Fiorentino", "FI"),
            new Candidate(4, "Sesto", "BZ"),
            new Candidate(5, "Prato", "PO"),
            new Candidate(6, "Prato Carnico", "UD"),
            new Candidate(7, "Castiglion Fiorentino", "AR"),
            new Candidate(8, "Ponte", "BN"),
            new Candidate(9, "Ponte Buggianese", "PT"),
            new Candidate(10, "Figline e Incisa Valdarno", "FI"),
            new Candidate(11, "Samone", "TO"),
            new Candidate(12, "Samone", "TN"),
            new Candidate(13, "Sant'Agata de' Goti", "BN"),
            new Candidate(14, "Campi Bisenzio", "FI"),
            new Candidate(15, "Lastra a Signa", "FI"));

    private static final MunicipalityMatcher MATCHER =
            new MunicipalityMatcher(MUNICIPALITIES, LegacyImportService.aliases());

    @ParameterizedTest(name = "«{0}» → {1} ({2})")
    @CsvSource(delimiter = '|', nullValues = "-", textBlock = """
            Firenze                 | 1  | EXACT
            FIRENZE                 | 1  | EXACT
            SANT'AGATA DE' GOTI     | 13 | EXACT
            SCANDICCI FI            | 2  | PROVINCE
            FI SCANDICCI            | 2  | PROVINCE
            SCANDICCI (FI)          | 2  | PROVINCE
            SAMONE TN               | 12 | PROVINCE
            S FIORENTINO FI         | 3  | ALIAS
            SFIORENTINO FI          | 3  | ALIAS
            C.BISENZIO              | 14 | ALIAS
            FIGLINE VALDARNO        | 10 | ALIAS
            SESTO FIORENTION        | 3  | ALIAS
            PONTE A SIGNA           | 15 | ALIAS
            FIRENZE NOVOLI          | 1  | PREFIX
            SCANDICCI FIRENZE       | 2  | PREFIX
            PRATO CALENZANO         | 5  | PREFIX
            CASTIGLION FIORENTIN    | 7  | TRUNCATED
            """)
    void abbinato(String text, long id, Rule rule) {
        assertThat(MATCHER.match(text)).contains(new Match(id, rule));
    }

    @ParameterizedTest(name = "«{0}» non trovato")
    @CsvSource(delimiter = '|', textBlock = """
            FI
            (FI)
            SAMONE
            SCANDICCI PO
            SESTO FIORENTIONE
            PONTE A GIOGOLI
            CASTIGLION F
            CITTÀ METROPOLITANA
            """)
    void nonTrovato(String text) {
        assertThat(MATCHER.match(text)).isEqualTo(Optional.empty());
    }

    @ParameterizedTest(name = "vuoto: «{0}»")
    @CsvSource(value = {"''", "'   '"})
    void testoVuoto(String text) {
        assertThat(MATCHER.match(text)).isEmpty();
    }
}
