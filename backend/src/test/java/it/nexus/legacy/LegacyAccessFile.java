package it.nexus.legacy;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.healthmarketscience.jackcess.ColumnBuilder;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.DateTimeType;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.TableBuilder;

/**
 * File Access costruito nel test (US-1309): le tabelle e le colonne del database attuale che l'importazione legge, con
 * dati inventati sul modello di quelli veri (comuni con la sigla, email come collegamenti, numeri con un testo).
 */
final class LegacyAccessFile implements AutoCloseable {

    private final Database db;
    private final Table companies;
    private final Table calls;

    private LegacyAccessFile(Path file) throws IOException {
        db = new DatabaseBuilder(file.toFile()).setFileFormat(Database.FileFormat.V2000).create();
        db.setDateTimeType(DateTimeType.LOCAL_DATE_TIME);
        companies = new TableBuilder("Contatti")
                .addColumn(new ColumnBuilder("IDContatto", DataType.LONG))
                .addColumn(text("NomeSocietà", 50))
                .addColumn(text("Nominativo_1", 50))
                .addColumn(text("Nominativo_2", 50))
                .addColumn(text("Indirizzo_1", 255))
                .addColumn(text("Sede Locale", 50))
                .addColumn(text("Indirizzo_2", 20))
                .addColumn(text("Sede Legale", 20))
                .addColumn(text("Paese", 50))
                .addColumn(new ColumnBuilder("IDTipoContatto_2", DataType.LONG))
                .addColumn(text("TelefonoUfficio", 30))
                .addColumn(text("TelefonoCellulare", 30))
                .addColumn(text("NumeroFax", 30))
                .addColumn(new ColumnBuilder("IndirizzoPostaElettronica_1", DataType.MEMO))
                .addColumn(new ColumnBuilder("Data_um_4", DataType.SHORT_DATE_TIME))
                .addColumn(new ColumnBuilder("IDTipoContatto_1", DataType.LONG))
                .addColumn(new ColumnBuilder("IndirizzoPostaElettronica_2", DataType.MEMO))
                .addColumn(new ColumnBuilder("Note", DataType.MEMO))
                .addColumn(new ColumnBuilder("data registrazione", DataType.SHORT_DATE_TIME))
                .addColumn(new ColumnBuilder("Id_nuova_cat", DataType.LONG))
                .toTable(db);
        calls = new TableBuilder("Chiamate")
                .addColumn(new ColumnBuilder("IDChiamata", DataType.LONG))
                .addColumn(new ColumnBuilder("IDContatto", DataType.LONG))
                .addColumn(new ColumnBuilder("DataChiamata", DataType.SHORT_DATE_TIME))
                .addColumn(new ColumnBuilder("OraChiamata", DataType.SHORT_DATE_TIME))
                .addColumn(text("Oggetto", 255))
                .addColumn(new ColumnBuilder("IDEsito", DataType.LONG))
                .addColumn(new ColumnBuilder("Data_esito", DataType.SHORT_DATE_TIME))
                .addColumn(new ColumnBuilder("Note", DataType.MEMO))
                .addColumn(new ColumnBuilder("Evasa", DataType.BOOLEAN))
                .addColumn(new ColumnBuilder("Stage", DataType.BOOLEAN))
                .addColumn(new ColumnBuilder("disp_assunz", DataType.BOOLEAN))
                .addColumn(new ColumnBuilder("bors_lav", DataType.BOOLEAN))
                .addColumn(new ColumnBuilder("IST", DataType.BOOLEAN))
                .addColumn(new ColumnBuilder("tirocinio", DataType.BOOLEAN))
                .addColumn(new ColumnBuilder("giova_si", DataType.BOOLEAN))
                .addColumn(new ColumnBuilder("data_um_8", DataType.SHORT_DATE_TIME))
                .addColumn(new ColumnBuilder("Id_progetto", DataType.LONG))
                .addColumn(new ColumnBuilder("Id_elenco2", DataType.LONG))
                .toTable(db);
        Table outcomes = new TableBuilder("Esiti").addColumn(new ColumnBuilder("IDEsito", DataType.LONG))
                .addColumn(text("Esito", 50)).addColumn(text("ESI", 50)).toTable(db);
        outcomes.addRow(1, "Data disponibilità", null);
        outcomes.addRow(2, "Richiamare", "Richiamare in data:");
        Table projects = new TableBuilder("Progetti").addColumn(new ColumnBuilder("Id_progetto", DataType.LONG))
                .addColumn(text("Progetto", 50)).toTable(db);
        projects.addRow(1, "POLIS VECCHIO");
        projects.addRow(26, "GOL");
        Table operators = new TableBuilder("Assistenti").addColumn(new ColumnBuilder("Id_elenco", DataType.LONG))
                .addColumn(text("Cognome", 255)).addColumn(text("Nome", 255)).toTable(db);
        operators.addRow(5, "Verdi", "Paolo");
    }

    static LegacyAccessFile create(Path file) throws IOException {
        return new LegacyAccessFile(file);
    }

    private static ColumnBuilder text(String name, int length) {
        return new ColumnBuilder(name, DataType.TEXT).setLengthInUnits(length);
    }

    /** Azienda con i campi indicati (nome di colonna di Access → valore); gli altri vuoti. */
    LegacyAccessFile company(int number, Map<String, Object> fields) throws IOException {
        Map<String, Object> row = new HashMap<>(fields);
        row.put("IDContatto", number);
        companies.addRowFromMap(row);
        return this;
    }

    LegacyAccessFile call(int number, int companyNumber, Map<String, Object> fields) throws IOException {
        Map<String, Object> row = new HashMap<>(fields);
        row.put("IDChiamata", number);
        row.put("IDContatto", companyNumber);
        row.putIfAbsent("Id_progetto", 1);
        row.putIfAbsent("Id_elenco2", 5);
        calls.addRowFromMap(row);
        return this;
    }

    static LocalDateTime at(int year, int month, int day, int hour, int minute) {
        return LocalDateTime.of(year, month, day, hour, minute);
    }

    @Override
    public void close() throws IOException {
        db.close();
    }
}
