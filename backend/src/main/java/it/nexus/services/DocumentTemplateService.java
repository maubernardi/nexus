package it.nexus.services;

import java.util.List;

import it.nexus.domain.DocumentTemplate;
import it.nexus.domain.Project;
import it.nexus.domain.Ticket;
import it.nexus.domain.dto.DocumentTemplateActiveDTO;
import it.nexus.domain.dto.DocumentPlaceholderDTO;
import it.nexus.domain.dto.DocumentTemplateDTO;
import it.nexus.domain.dto.DocumentTemplateFormDTO;
import it.nexus.domain.enumeration.InternshipDocumentKind;

/**
 * Modelli Word dei documenti caricati dall'ADMIN (US-1010): caricamento con il controllo del file e dei segnaposto,
 * sostituzione del file, disattivazione, audit; e la scelta dei modelli validi per una segnalazione.
 */
public interface DocumentTemplateService {

    List<DocumentTemplateDTO> list();

    /**
     * Segnaposto che i modelli possono usare, con gruppo e descrizione (US-1011): quelli di NEXUS, poi un gruppo per le
     * variabili di ogni progetto (US-1013).
     */
    List<DocumentPlaceholderDTO> placeholders();

    DocumentTemplateDTO upload(DocumentTemplateFormDTO form, String fileName, byte[] content);

    DownloadedFile file(String id);

    DocumentTemplateDTO replaceFile(String id, Long version, String fileName, byte[] content);

    DocumentTemplateDTO setActive(String id, DocumentTemplateActiveDTO dto);

    /**
     * Modelli attivi validi per il documento della segnalazione, in ordine di nome: stesso progetto e tipo, con l'ente
     * del beneficiario (se ce ne sono) oppure senza ente. Vuoto senza tipo di tirocinio.
     */
    List<DocumentTemplate> candidates(InternshipDocumentKind kind, Ticket ticket, Long internshipTypeId);

    /** Contenuto .docx del modello. */
    byte[] content(DocumentTemplate template);

    /** Nomi dei modelli attivi del progetto che usano il segnaposto (US-1013). */
    List<String> templatesUsing(Project project, String placeholder);
}
