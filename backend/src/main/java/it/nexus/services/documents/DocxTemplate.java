package it.nexus.services.documents;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.impl.xb.xmlschema.SpaceAttribute;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;

/**
 * Compila un modello Word (.docx) sostituendo i segnaposto {@code {NOME}} (US-1004). Cerca i segnaposto nel testo
 * intero di ogni paragrafo, perché in Word un segnaposto può essere spezzato in più run, soprattutto nei modelli
 * modificati a mano; poi cambia solo il testo dei segnaposto, così la formattazione dei modelli reali resta com'è
 * (EN-8). Copre corpo, tabelle (anche annidate), intestazioni e piè di pagina; un {@code \n} nel valore diventa un a
 * capo. I segnaposto senza valore restano com'erano, così un errore del modello si vede nel documento.
 */
public final class DocxTemplate {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([A-Z0-9_]+)}");

    private static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
    private static final QName BREAK = new QName(W, "br");
    private static final QName TEXT = new QName(W, "t");
    private static final QName SPACE = new QName("http://www.w3.org/XML/1998/namespace", "space");

    private DocxTemplate() {
    }

    /** Il modello compilato, come file .docx. */
    public static byte[] fill(InputStream template, Map<String, String> values) throws IOException {
        try (XWPFDocument document = new XWPFDocument(template); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            forEachParagraph(document, paragraph -> replace(paragraph, values));
            document.write(out);
            return out.toByteArray();
        }
    }

    /** I segnaposto presenti nel modello, per controllare i modelli forniti. */
    public static Set<String> placeholders(InputStream template) throws IOException {
        Set<String> found = new TreeSet<>();
        try (XWPFDocument document = new XWPFDocument(template)) {
            forEachParagraph(document, paragraph -> {
                Matcher matcher = PLACEHOLDER.matcher(paragraph.getText());
                while (matcher.find()) {
                    found.add(matcher.group(1));
                }
            });
        }
        return found;
    }

    /** Il testo del documento, paragrafo per paragrafo (tabelle, intestazioni e piè di pagina compresi). */
    public static String text(InputStream document) throws IOException {
        StringBuilder text = new StringBuilder();
        try (XWPFDocument doc = new XWPFDocument(document)) {
            forEachParagraph(doc, paragraph -> text.append(paragraph.getText()).append('\n'));
        }
        return text.toString();
    }

    private static void forEachParagraph(XWPFDocument document, Consumer<XWPFParagraph> action) {
        forEach(document.getBodyElements(), action);
        document.getHeaderList().forEach(header -> forEach(header.getBodyElements(), action));
        document.getFooterList().forEach(footer -> forEach(footer.getBodyElements(), action));
    }

    private static void forEach(List<IBodyElement> elements, Consumer<XWPFParagraph> action) {
        for (IBodyElement element : elements) {
            if (element instanceof XWPFParagraph paragraph) {
                action.accept(paragraph);
            } else if (element instanceof XWPFTable table) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        forEach(cell.getBodyElements(), action);
                    }
                }
            }
        }
    }

    /**
     * Sostituisce i segnaposto nei soli nodi di testo che li contengono: il resto del paragrafo (grassetti, tabulazioni,
     * simboli, campi) resta com'è. Un segnaposto spezzato in più run prende la formattazione del run in cui comincia.
     * Le sostituzioni vanno dall'ultima alla prima, così le posizioni di quelle precedenti non cambiano.
     */
    private static void replace(XWPFParagraph paragraph, Map<String, String> values) {
        List<CTText> nodes = new ArrayList<>();
        for (XWPFRun run : paragraph.getRuns()) {
            nodes.addAll(run.getCTR().getTList());
        }
        String[] texts = new String[nodes.size()];
        int[] offsets = new int[nodes.size()];
        int[] ends = new int[nodes.size()];
        StringBuilder all = new StringBuilder();
        for (int i = 0; i < nodes.size(); i++) {
            texts[i] = nodes.get(i).getStringValue();
            offsets[i] = all.length();
            all.append(texts[i]);
            ends[i] = all.length();
        }
        List<MatchResult> matches = PLACEHOLDER.matcher(all).results()
                .filter(m -> values.containsKey(m.group(1)))
                .toList();
        if (matches.isEmpty()) {
            return;
        }
        boolean[] changed = new boolean[nodes.size()];
        for (MatchResult m : matches.reversed()) {
            int first = nodeAt(offsets, ends, m.start());
            int last = nodeAt(offsets, ends, m.end() - 1);
            String tail = texts[last].substring(m.end() - offsets[last]);
            texts[first] = texts[first].substring(0, m.start() - offsets[first]) + values.get(m.group(1))
                    + (first == last ? tail : "");
            changed[first] = true;
            for (int i = first + 1; i <= last; i++) {
                texts[i] = i == last ? tail : "";
                changed[i] = true;
            }
        }
        for (int i = 0; i < nodes.size(); i++) {
            if (changed[i]) {
                write(nodes.get(i), texts[i]);
            }
        }
    }

    /** Il nodo che contiene la posizione nel testo originale del paragrafo (i nodi vuoti non contengono nulla). */
    private static int nodeAt(int[] offsets, int[] ends, int position) {
        for (int i = 0; i < offsets.length; i++) {
            if (offsets[i] <= position && position < ends[i]) {
                return i;
            }
        }
        throw new IllegalStateException("posizione fuori dal paragrafo");
    }

    /** Scrive il testo nel nodo; ogni {@code \n} diventa un a capo ({@code w:br}) nello stesso run. */
    private static void write(CTText node, String text) {
        String[] lines = text.split("\n", -1);
        node.setStringValue(lines[0]);
        node.setSpace(SpaceAttribute.Space.PRESERVE);
        if (lines.length == 1) {
            return;
        }
        try (XmlCursor cursor = node.newCursor()) {
            cursor.toEndToken();
            cursor.toNextToken();
            for (int i = 1; i < lines.length; i++) {
                cursor.beginElement(BREAK);
                cursor.toNextToken();
                cursor.beginElement(TEXT);
                cursor.insertAttributeWithValue(SPACE, "preserve");
                cursor.insertChars(lines[i]);
                cursor.toNextToken();
            }
        }
    }
}
