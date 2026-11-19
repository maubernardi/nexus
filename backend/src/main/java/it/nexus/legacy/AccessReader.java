package it.nexus.legacy;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.DateTimeType;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.crypt.CryptCodecProvider;

/**
 * Legge dal file Access del database attuale (US-1309), in sola lettura, le aziende («Contatti»), le chiamate e i testi
 * dei loro codici (esiti, progetti, operatori). Le date restano locali, come in Access.
 */
final class AccessReader {

    record LegacyCompany(int number, String name, String legalAddress, String legalMunicipality,
            String operatingAddress, String operatingMunicipality, String phone, String mobile, String fax,
            String email, String secondaryEmail, String contact1, Integer role1, String contact2, Integer role2,
            Integer category, String activity, String note, LocalDateTime registeredAt, LocalDateTime updatedAt) {
    }

    record LegacyCall(Integer number, Integer companyNumber, LocalDateTime date, LocalDateTime time, String subject,
            Integer outcome, LocalDateTime outcomeAt, String notes, boolean handled, boolean internship,
            boolean stage, boolean ist, boolean workGrant, boolean hiring, boolean giovanisi, Integer project,
            Integer operator, LocalDateTime updatedAt) {
    }

    record LegacyData(List<LegacyCompany> companies, List<LegacyCall> calls, Map<Integer, String> outcomes,
            Map<Integer, String> projects, Map<Integer, String> operators) {
    }

    private AccessReader() {
    }

    static LegacyData read(Path file) throws IOException {
        // il file attuale è «codificato» da Access (RC4, senza password): lo decodifica jackcess-encrypt
        try (Database db = new DatabaseBuilder(file.toFile()).setReadOnly(true)
                .setCodecProvider(new CryptCodecProvider()).open()) {
            db.setDateTimeType(DateTimeType.LOCAL_DATE_TIME);
            List<LegacyCompany> companies = new ArrayList<>();
            for (Row r : db.getTable("Contatti")) {
                companies.add(new LegacyCompany(r.getInt("IDContatto"), r.getString("NomeSocietà"),
                        r.getString("Indirizzo_1"), r.getString("Sede Legale"), r.getString("Indirizzo_2"),
                        r.getString("Sede Locale"), r.getString("TelefonoUfficio"), r.getString("TelefonoCellulare"),
                        r.getString("NumeroFax"), r.getString("IndirizzoPostaElettronica_1"),
                        r.getString("IndirizzoPostaElettronica_2"), r.getString("Nominativo_1"),
                        r.getInt("IDTipoContatto_1"), r.getString("Nominativo_2"), r.getInt("IDTipoContatto_2"),
                        r.getInt("Id_nuova_cat"), r.getString("Paese"), r.getString("Note"),
                        r.getLocalDateTime("data registrazione"), r.getLocalDateTime("Data_um_4")));
            }
            List<LegacyCall> calls = new ArrayList<>();
            for (Row r : db.getTable("Chiamate")) {
                calls.add(new LegacyCall(r.getInt("IDChiamata"), r.getInt("IDContatto"),
                        r.getLocalDateTime("DataChiamata"), r.getLocalDateTime("OraChiamata"), r.getString("Oggetto"),
                        r.getInt("IDEsito"), r.getLocalDateTime("Data_esito"), r.getString("Note"),
                        flag(r, "Evasa"), flag(r, "tirocinio"), flag(r, "Stage"), flag(r, "IST"),
                        flag(r, "bors_lav"), flag(r, "disp_assunz"), flag(r, "giova_si"), r.getInt("Id_progetto"),
                        r.getInt("Id_elenco2"), r.getLocalDateTime("data_um_8")));
            }
            return new LegacyData(companies, calls, labels(db.getTable("Esiti"), "IDEsito", "Esito"),
                    labels(db.getTable("Progetti"), "Id_progetto", "Progetto"), operators(db.getTable("Assistenti")));
        }
    }

    private static boolean flag(Row row, String column) {
        return Boolean.TRUE.equals(row.getBoolean(column));
    }

    private static Map<Integer, String> labels(Table table, String id, String label) {
        Map<Integer, String> labels = new HashMap<>();
        for (Row r : table) {
            String text = LegacyText.clean(r.getString(label));
            if (r.getInt(id) != null && text != null) {
                labels.put(r.getInt(id), text);
            }
        }
        return labels;
    }

    /** Operatori delle chiamate: «Cognome Nome». */
    private static Map<Integer, String> operators(Table table) {
        Map<Integer, String> operators = new HashMap<>();
        for (Row r : table) {
            String name = Stream.of(r.getString("Cognome"), r.getString("Nome")).map(LegacyText::clean)
                    .filter(s -> s != null).collect(Collectors.joining(" "));
            if (r.getInt("Id_elenco") != null && !name.isEmpty()) {
                operators.put(r.getInt("Id_elenco"), name);
            }
        }
        return operators;
    }
}
