package it.nexus.support;

import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

/**
 * File ISTAT «Elenco dei comuni italiani» per i test (US-114): stessa intestazione del file vero (su più righe, tra
 * virgolette) e righe con le 26 colonne, in Latin-1 come il file pubblicato.
 */
public final class IstatFiles {

    public static final String HEADER = """
            Codice Regione;"Codice dell'Unità territoriale sovracomunale\s
            (valida a fini statistici)";Codice Provincia (Storico)(1);Progressivo del Comune (2);Codice Comune formato \
            alfanumerico;Denominazione (Italiana e straniera);Denominazione in italiano;Denominazione altra lingua;Codice \
            Ripartizione Geografica;Ripartizione geografica;Denominazione Regione;"Denominazione dell'Unità territoriale \
            sovracomunale\s
            (valida a fini statistici)";Tipologia di Unità territoriale sovracomunale ;Flag Comune capoluogo di \
            provincia/città metropolitana/libero consorzio;Sigla automobilistica;Codice Comune formato numerico;Codice \
            Comune numerico con 110 province (dal 2010 al 2016);Codice Comune numerico con 107 province (dal 2006 al \
            2009);Codice Comune numerico con 103 province (dal 1995 al 2005);Codice Catastale del comune;Codice NUTS1 \
            2021;Codice NUTS2 2021 (3) ;Codice NUTS3 2021;Codice NUTS1 2024;Codice NUTS2 2024 (3) ;Codice NUTS3 2024""";

    private IstatFiles() {
    }

    /** Una riga: codice catastale, codice ISTAT, nome, codice, nome e sigla della provincia. */
    public static String row(String cadastral, String istat, String name, String provinceCode, String provinceName,
            String abbreviation) {
        StringJoiner j = new StringJoiner(";");
        j.add("09").add(provinceCode).add(istat.substring(0, 3)).add(istat.substring(3)).add(istat).add(name)
                .add(name).add("").add("3").add("Centro").add("Toscana").add(provinceName).add("3").add("0")
                .add(abbreviation).add(String.valueOf(Integer.parseInt(istat))).add("").add("").add("").add(cadastral)
                .add("ITI").add("ITI1").add("ITI14").add("ITI").add("ITI1").add("ITI14");
        return j.toString();
    }

    public static String firenze() {
        return row("D612", "048017", "Firenze", "248", "Firenze", "FI");
    }

    public static String sesto() {
        return row("I684", "048043", "Sesto Fiorentino", "248", "Firenze", "FI");
    }

    public static String fiesole() {
        return row("D575", "048015", "Fiesole", "248", "Firenze", "FI");
    }

    public static String prato() {
        return row("G999", "100005", "Prato", "100", "Prato", "PO");
    }

    /** Il file con le righe indicate, CRLF e Latin-1 come quello dell'ISTAT. */
    public static byte[] file(String... rows) {
        return (HEADER + "\r\n" + String.join("\r\n", rows) + "\r\n").getBytes(StandardCharsets.ISO_8859_1);
    }
}
