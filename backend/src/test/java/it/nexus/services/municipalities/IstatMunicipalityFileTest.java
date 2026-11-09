package it.nexus.services.municipalities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import it.nexus.services.municipalities.IstatMunicipalityFile.Row;
import it.nexus.support.IstatFiles;
import it.nexus.web.errors.BadRequestException;

/** Lettura del file ISTAT (US-114): formato vero, colonne per nome, controlli riga per riga. */
class IstatMunicipalityFileTest {

    @Test
    void ilFileVeroDellIstat() throws IOException {
        byte[] content;
        try (InputStream in = new ClassPathResource("seed/istat/Elenco-comuni-italiani.csv").getInputStream()) {
            content = in.readAllBytes();
        }
        List<Row> rows = IstatMunicipalityFile.read(content, 7000);
        assertThat(rows).hasSizeGreaterThan(7800);
        assertThat(rows).contains(new Row("I684", "048043", "Sesto Fiorentino", "248", "Firenze", "FI"));
        // Latin-1 letto bene: le lettere accentate restano tali
        assertThat(rows).extracting(Row::name).contains("Agliè", "Forlì");
    }

    @Test
    void ancheInUtf8ConIlBom() {
        byte[] latin = IstatFiles.file(IstatFiles.firenze(), IstatFiles.sesto(), IstatFiles.prato());
        String text = new String(latin, StandardCharsets.ISO_8859_1);
        byte[] utf8 = ("﻿" + text).getBytes(StandardCharsets.UTF_8);
        assertThat(IstatMunicipalityFile.read(utf8, 3)).extracting(Row::provinceName)
                .containsExactly("Firenze", "Firenze", "Prato");
    }

    @Test
    void unFileQualunque() {
        byte[] other = "nome;cognome\r\nMario;Rossi\r\n".getBytes(StandardCharsets.UTF_8);
        assertThatThrownBy(() -> IstatMunicipalityFile.read(other, 1))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("non è l'elenco dei comuni dell'ISTAT");
    }

    @Test
    void troppoPochiComuni() {
        byte[] file = IstatFiles.file(IstatFiles.firenze());
        assertThatThrownBy(() -> IstatMunicipalityFile.read(file, 3))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("non sembra l'elenco completo");
    }

    @Test
    void codiceCatastaleRipetuto() {
        byte[] file = IstatFiles.file(IstatFiles.firenze(), IstatFiles.firenze(), IstatFiles.prato());
        assertThatThrownBy(() -> IstatMunicipalityFile.read(file, 1))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Riga 3: il codice catastale D612 compare due volte");
    }

    @Test
    void valoreNonValido() {
        byte[] file = IstatFiles.file(IstatFiles.row("D612", "48017", "Firenze", "248", "Firenze", "FI"));
        assertThatThrownBy(() -> IstatMunicipalityFile.read(file, 1))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Riga 2: il valore «48017»");
    }

    @Test
    void provinciaConDueNomi() {
        byte[] file = IstatFiles.file(IstatFiles.firenze(),
                IstatFiles.row("I684", "048043", "Sesto Fiorentino", "248", "Città di Firenze", "FI"));
        assertThatThrownBy(() -> IstatMunicipalityFile.read(file, 1))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Riga 3: la provincia con codice 248 ha nome o sigla diversi");
    }

    @Test
    void virgoletteECampiVuoti() {
        assertThat(IstatMunicipalityFile.records("a;\"b;\"\"c\"\"\";\r\nd\n"))
                .containsExactly(List.of("a", "b;\"c\"", ""), List.of("d"));
    }
}
