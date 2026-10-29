package it.nexus.services.documents;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;

/**
 * Compila un modello Word (.docx) sostituendo i segnaposto {@code {NOME}} (US-1004). Lavora sul testo intero di ogni
 * paragrafo: in Word un segnaposto può essere spezzato in più run, soprattutto nei modelli modificati a mano. Il
 * paragrafo che contiene segnaposto viene riscritto in un solo run con la formattazione del primo. Copre corpo, tabelle
 * (anche annidate), intestazioni e piè di pagina; un {@code \n} nel valore diventa un a capo. I segnaposto senza valore
 * restano com'erano, così un errore del modello si vede nel documento.
 */
public final class DocxTemplate {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([A-Z0-9_]+)}");

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

    private static void replace(XWPFParagraph paragraph, Map<String, String> values) {
        String text = paragraph.getText();
        String replaced = PLACEHOLDER.matcher(text)
                .replaceAll(m -> Matcher.quoteReplacement(values.getOrDefault(m.group(1), m.group())));
        List<XWPFRun> runs = paragraph.getRuns();
        if (replaced.equals(text) || runs.isEmpty()) {
            return;
        }
        // un run nuovo con la formattazione del primo, poi via i vecchi (dall'ultimo, gli indici non cambiano)
        XWPFRun first = runs.get(0);
        XWPFRun run = paragraph.insertNewRun(0);
        if (first.getCTR().isSetRPr()) {
            run.getCTR().setRPr((CTRPr) first.getCTR().getRPr().copy());
        }
        for (int i = paragraph.getRuns().size() - 1; i > 0; i--) {
            paragraph.removeRun(i);
        }
        String[] lines = replaced.split("\n", -1);
        run.setText(lines[0]);
        for (int i = 1; i < lines.length; i++) {
            run.addBreak();
            run.setText(lines[i]);
        }
    }
}
