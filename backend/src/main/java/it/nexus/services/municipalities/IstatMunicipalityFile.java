package it.nexus.services.municipalities;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import it.nexus.web.errors.BadRequestException;

/**
 * Lettura del file ISTAT «Elenco dei comuni italiani» (US-114): CSV con il punto e virgola, in Latin-1 (com'è
 * pubblicato) o UTF-8, con l'intestazione su più righe tra virgolette. Le colonne si riconoscono dal nome, non dalla
 * posizione; i valori si controllano riga per riga. Un problema diventa un {@link BadRequestException} con il motivo,
 * che l'ADMIN legge.
 */
public final class IstatMunicipalityFile {

    /** Una riga del file: un comune con la sua provincia. */
    public record Row(String cadastralCode, String istatCode, String name, String provinceCode, String provinceName,
            String provinceAbbreviation) {
    }

    private static final Pattern CADASTRAL = Pattern.compile("[A-Z]\\d{3}");
    private static final Pattern ISTAT = Pattern.compile("\\d{6}");
    private static final Pattern PROVINCE_CODE = Pattern.compile("\\d{3}");
    private static final Pattern ABBREVIATION = Pattern.compile("[A-Z]{2}");

    private static final String CADASTRAL_COLUMN = "codice catastale del comune";
    private static final String ISTAT_COLUMN = "codice comune formato alfanumerico";
    private static final String NAME_COLUMN = "denominazione in italiano";
    private static final String PROVINCE_CODE_COLUMN = "codice dell'unita territoriale sovracomunale";
    private static final String PROVINCE_NAME_COLUMN = "denominazione dell'unita territoriale sovracomunale";
    private static final String ABBREVIATION_COLUMN = "sigla automobilistica";

    private IstatMunicipalityFile() {
    }

    /** Legge il file; {@code minRows} è il numero minimo di comuni perché l'elenco sia considerato completo. */
    public static List<Row> read(byte[] content, int minRows) {
        List<List<String>> records = records(decode(content));
        if (records.isEmpty()) {
            throw new BadRequestException("Il file è vuoto");
        }
        Map<String, Integer> columns = columns(records.getFirst());
        List<Row> rows = new ArrayList<>();
        Set<String> cadastralCodes = new HashSet<>();
        Map<String, Row> provinces = new HashMap<>();
        for (int i = 1; i < records.size(); i++) {
            List<String> fields = records.get(i);
            if (fields.stream().allMatch(String::isBlank)) {
                continue;
            }
            int line = i + 1;
            Row row = new Row(
                    value(fields, columns, CADASTRAL_COLUMN, CADASTRAL, line).toUpperCase(Locale.ROOT),
                    value(fields, columns, ISTAT_COLUMN, ISTAT, line),
                    value(fields, columns, NAME_COLUMN, null, line),
                    value(fields, columns, PROVINCE_CODE_COLUMN, PROVINCE_CODE, line),
                    value(fields, columns, PROVINCE_NAME_COLUMN, null, line),
                    value(fields, columns, ABBREVIATION_COLUMN, ABBREVIATION, line));
            if (!cadastralCodes.add(row.cadastralCode())) {
                throw new BadRequestException("Riga %d: il codice catastale %s compare due volte", line,
                        row.cadastralCode());
            }
            Row sameProvince = provinces.putIfAbsent(row.provinceCode(), row);
            if (sameProvince != null && (!sameProvince.provinceName().equals(row.provinceName())
                    || !sameProvince.provinceAbbreviation().equals(row.provinceAbbreviation()))) {
                throw new BadRequestException("Riga %d: la provincia con codice %s ha nome o sigla diversi", line,
                        row.provinceCode());
            }
            rows.add(row);
        }
        if (rows.size() < minRows) {
            throw new BadRequestException(
                    "Il file contiene %d comuni: non sembra l'elenco completo dell'ISTAT (ne servono almeno %d)",
                    rows.size(), minRows);
        }
        return rows;
    }

    /** UTF-8 se il file lo è davvero, altrimenti Latin-1 (la codifica del file pubblicato dall'ISTAT). */
    private static String decode(byte[] content) {
        int start = content.length >= 3 && (content[0] & 0xFF) == 0xEF && (content[1] & 0xFF) == 0xBB
                && (content[2] & 0xFF) == 0xBF ? 3 : 0;
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(content, start, content.length - start))
                    .toString();
        } catch (CharacterCodingException e) {
            return new String(content, StandardCharsets.ISO_8859_1);
        }
    }

    /** CSV con {@code ;}: campi tra virgolette con a capo e virgolette raddoppiate; righe separate da LF o CRLF. */
    static List<List<String>> records(String text) {
        List<List<String>> records = new ArrayList<>();
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (quoted) {
                if (c == '"' && i + 1 < text.length() && text.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else if (c == '"') {
                    quoted = false;
                } else {
                    field.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ';') {
                fields.add(field.toString());
                field.setLength(0);
            } else if (c == '\n' || c == '\r') {
                if (c == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    i++;
                }
                fields.add(field.toString());
                field.setLength(0);
                records.add(fields);
                fields = new ArrayList<>();
            } else {
                field.append(c);
            }
        }
        if (!field.isEmpty() || !fields.isEmpty()) {
            fields.add(field.toString());
            records.add(fields);
        }
        return records;
    }

    private static Map<String, Integer> columns(List<String> header) {
        Map<String, Integer> found = new HashMap<>();
        for (String wanted : List.of(CADASTRAL_COLUMN, ISTAT_COLUMN, NAME_COLUMN, PROVINCE_CODE_COLUMN,
                PROVINCE_NAME_COLUMN, ABBREVIATION_COLUMN)) {
            for (int i = 0; i < header.size(); i++) {
                if (normalize(header.get(i)).startsWith(wanted)) {
                    found.put(wanted, i);
                    break;
                }
            }
            if (!found.containsKey(wanted)) {
                throw new BadRequestException(
                        "Il file non è l'elenco dei comuni dell'ISTAT: manca la colonna «%s»", wanted);
            }
        }
        return found;
    }

    /** Minuscolo, senza accenti e con gli spazi (anche gli a capo) ridotti a uno. */
    private static String normalize(String text) {
        String plain = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return plain.replace('’', '\'').replaceAll("\\s+", " ").strip().toLowerCase(Locale.ROOT);
    }

    private static String value(List<String> fields, Map<String, Integer> columns, String column, Pattern format,
            int line) {
        int index = columns.get(column);
        String value = index < fields.size() ? fields.get(index).strip() : "";
        if (value.isEmpty()) {
            throw new BadRequestException("Riga %d: manca il valore della colonna «%s»", line, column);
        }
        if (format != null && !format.matcher(value.toUpperCase(Locale.ROOT)).matches()) {
            throw new BadRequestException("Riga %d: il valore «%s» della colonna «%s» non è valido", line, value,
                    column);
        }
        return value;
    }
}
