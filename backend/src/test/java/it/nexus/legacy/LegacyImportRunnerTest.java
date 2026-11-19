package it.nexus.legacy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Nel log dell'importazione (US-1309) l'errore ha solo il punto del codice, non il messaggio. */
class LegacyImportRunnerTest {

    @Test
    void puntoDelNostroCodice() {
        RuntimeException error = new IllegalStateException("testo con dati del file");
        error.setStackTrace(new StackTraceElement[] {
                new StackTraceElement("com.healthmarketscience.jackcess.Row", "getInt", "Row.java", 10),
                new StackTraceElement("it.nexus.legacy.AccessReader", "read", "AccessReader.java", 63)});
        assertThat(LegacyImportRunner.where(error)).isEqualTo("it.nexus.legacy.AccessReader:63");
    }

    @Test
    void altrimentiIlPrimoPunto() {
        RuntimeException error = new IllegalStateException();
        error.setStackTrace(new StackTraceElement[] {
                new StackTraceElement("com.healthmarketscience.jackcess.Row", "getInt", "Row.java", 10)});
        assertThat(LegacyImportRunner.where(error)).isEqualTo("com.healthmarketscience.jackcess.Row:10");
    }
}
