package it.nexus.services;

import java.util.List;

import it.nexus.domain.dto.DocumentTemplateChoiceDTO;
import it.nexus.domain.enumeration.InternshipDocumentKind;

/**
 * Documenti del tirocinio compilati dai modelli Word (Convenzione US-1004, Progetto Formativo US-1005): solo per il Tutor della segnalazione, dall'appuntamento
 * fissato in poi, come la scheda. Un dato mancante resta una riga vuota da completare a mano.
 */
public interface InternshipDocumentService {

    /** File generato: nome per il download e contenuto .docx. */
    record GeneratedDocument(String filename, byte[] content) {
    }

    /**
     * Il documento compilato con il modello caricato che vale per la segnalazione (US-1010), quello scelto dal Tutor se
     * ce n'è più di uno ({@code templateId}), oppure il modello generico.
     */
    GeneratedDocument generate(String ticketId, InternshipDocumentKind kind, String templateId);

    /** I modelli caricati validi per il documento della segnalazione: più di uno = il Tutor sceglie. */
    List<DocumentTemplateChoiceDTO> templates(String ticketId, InternshipDocumentKind kind);
}
