package it.nexus.services.documents;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.junit.jupiter.api.Test;

class DocxTemplateTest {

    /** Un modello come lo scriverebbe Word: segnaposto spezzato in più run, tabella e piè di pagina. */
    private static byte[] template() throws IOException {
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFParagraph p = doc.createParagraph();
            XWPFRun bold = p.createRun();
            bold.setBold(true);
            bold.setText("Tirocinante: {NOME_");
            p.createRun().setText("TIROCINANTE}, CF {CODICE_FISCALE}.");
            XWPFTable table = doc.createTable(1, 2);
            table.getRow(0).getCell(0).setText("Griglia");
            table.getRow(0).getCell(1).setText("{GRIGLIA}");
            doc.createParagraph().createRun().setText("Resta {SCONOSCIUTO}; prezzo 5$ e \\ barra.");
            XWPFHeaderFooterPolicy policy = doc.createHeaderFooterPolicy();
            policy.createFooter(XWPFHeaderFooterPolicy.DEFAULT).createParagraph().createRun()
                    .setText("Segnalazione n. {NUMERO}");
            doc.write(out);
            return out.toByteArray();
        }
    }

    @Test
    void segnapostiAncheSpezzati_tabellePiediDiPaginaEACapo() throws IOException {
        byte[] filled = DocxTemplate.fill(new ByteArrayInputStream(template()), Map.of(
                "NOME_TIROCINANTE", "Mario Rossi",
                "CODICE_FISCALE", "RSSMRA85T10A562S",
                "GRIGLIA", "Lunedì: 09:00–13:00 (4 ore)\nMartedì: 14:00–18:00 (4 ore)",
                "NUMERO", "34"));

        String text = DocxTemplate.text(new ByteArrayInputStream(filled));
        assertThat(text)
                .contains("Tirocinante: Mario Rossi, CF RSSMRA85T10A562S.")
                .contains("Lunedì: 09:00–13:00 (4 ore)\nMartedì: 14:00–18:00 (4 ore)")
                .contains("Segnalazione n. 34")
                // senza valore resta com'è; i caratteri speciali del valore e del testo non si perdono
                .contains("Resta {SCONOSCIUTO}; prezzo 5$ e \\ barra.");
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(filled))) {
            // i run restano: il segnaposto spezzato prende la formattazione del run in cui comincia
            List<XWPFRun> runs = doc.getParagraphs().getFirst().getRuns();
            assertThat(runs).hasSize(2);
            assertThat(runs.get(0).text()).isEqualTo("Tirocinante: Mario Rossi");
            assertThat(runs.get(0).isBold()).isTrue();
            assertThat(runs.get(1).text()).isEqualTo(", CF RSSMRA85T10A562S.");
            assertThat(runs.get(1).isBold()).isFalse();
            // l'a capo resta nel run del segnaposto
            XWPFParagraph cell = doc.getTables().getFirst().getRow(0).getCell(1).getParagraphs().getFirst();
            assertThat(cell.getRuns()).hasSize(1);
            assertThat(cell.getRuns().getFirst().getCTR().getBrList()).hasSize(1);
        }
    }

    /** Come nei modelli reali (EN-8): tabulazione, simbolo e formattazioni diverse nello stesso paragrafo. */
    @Test
    void formattazioneDelParagrafoConservata() throws IOException {
        byte[] template;
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFParagraph p = doc.createParagraph();
            XWPFRun label = p.createRun();
            label.addTab();
            label.setBold(true);
            label.setText("Lunedì ");
            XWPFRun box = p.createRun();
            box.setText("{GIORNO_LUNEDI}");
            XWPFRun sym = p.createRun();
            sym.getCTR().addNewSym().setChar(new byte[] {(byte) 0xF0, (byte) 0xA8});
            XWPFRun split = p.createRun();
            split.setItalic(true);
            split.setText(" {A}-{B} e {C");
            p.createRun().setText("_");
            p.createRun().setText("D} fine");
            doc.write(out);
            template = out.toByteArray();
        }

        byte[] filled = DocxTemplate.fill(new ByteArrayInputStream(template),
                Map.of("GIORNO_LUNEDI", "☒", "A", "uno", "B", "due", "C_D", "tre"));

        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(filled))) {
            List<XWPFRun> runs = doc.getParagraphs().getFirst().getRuns();
            assertThat(runs).hasSize(6);
            assertThat(runs.get(0).getCTR().getTabList()).hasSize(1);
            assertThat(runs.get(0).isBold()).isTrue();
            assertThat(runs.get(1).text()).isEqualTo("☒");
            assertThat(runs.get(2).getCTR().getSymList()).hasSize(1);
            assertThat(runs.get(3).text()).isEqualTo(" uno-due e tre");
            assertThat(runs.get(3).isItalic()).isTrue();
            assertThat(runs.get(4).text()).isEmpty();
            assertThat(runs.get(5).text()).isEqualTo(" fine");
        }
    }

    @Test
    void segnapostiDelModello() throws IOException {
        assertThat(DocxTemplate.placeholders(new ByteArrayInputStream(template())))
                .containsExactly("CODICE_FISCALE", "GRIGLIA", "NOME_TIROCINANTE", "NUMERO", "SCONOSCIUTO");
    }
}
