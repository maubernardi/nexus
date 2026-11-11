package it.nexus.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

/** Modelli Word finti per i test (US-1010): i modelli reali restano fuori dal repository. */
public final class TestDocx {

    private TestDocx() {
    }

    /** Un .docx con un paragrafo per testo. */
    public static byte[] of(String... paragraphs) {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            for (String paragraph : paragraphs) {
                document.createParagraph().createRun().setText(paragraph);
            }
            document.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
