package it.nexus.services.documents;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
            XWPFParagraph first = doc.getParagraphs().getFirst();
            // un solo run, con la formattazione del primo
            assertThat(first.getRuns()).hasSize(1);
            assertThat(first.getRuns().getFirst().isBold()).isTrue();
        }
    }

    @Test
    void segnapostiDelModello() throws IOException {
        assertThat(DocxTemplate.placeholders(new ByteArrayInputStream(template())))
                .containsExactly("CODICE_FISCALE", "GRIGLIA", "NOME_TIROCINANTE", "NUMERO", "SCONOSCIUTO");
    }
}
