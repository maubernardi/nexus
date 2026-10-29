package it.nexus.services;

import it.nexus.domain.enumeration.InternshipDocumentKind;

/**
 * Documenti del tirocinio compilati dai modelli Word (Convenzione US-1004, Progetto Formativo US-1005): solo per il Tutor della segnalazione, dall'appuntamento
 * fissato in poi, come la scheda. Un dato mancante resta una riga vuota da completare a mano.
 */
public interface InternshipDocumentService {

    /** File generato: nome per il download e contenuto .docx. */
    record GeneratedDocument(String filename, byte[] content) {
    }

    GeneratedDocument generate(String ticketId, InternshipDocumentKind kind);
}
