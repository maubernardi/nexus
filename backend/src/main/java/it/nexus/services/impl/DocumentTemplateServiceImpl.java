package it.nexus.services.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.CareAgency;
import it.nexus.domain.DocumentTemplate;
import it.nexus.domain.InternshipType;
import it.nexus.domain.Project;
import it.nexus.domain.StoredFile;
import it.nexus.domain.StoredFileContent;
import it.nexus.domain.Ticket;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.DocumentTemplateActiveDTO;
import it.nexus.domain.dto.DocumentTemplateDTO;
import it.nexus.domain.dto.DocumentTemplateFormDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.InternshipDocumentKind;
import it.nexus.mapper.ReferenceMapper;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.CareAgencyRepository;
import it.nexus.repository.DocumentTemplateRepository;
import it.nexus.repository.InternshipTypeRepository;
import it.nexus.repository.ProjectRepository;
import it.nexus.repository.StoredFileContentRepository;
import it.nexus.repository.StoredFileRepository;
import it.nexus.services.AuditService;
import it.nexus.services.DocumentTemplateService;
import it.nexus.services.DownloadedFile;
import it.nexus.services.documents.DocumentPlaceholders;
import it.nexus.services.documents.DocxTemplate;
import it.nexus.web.errors.ConflictException;
import it.nexus.web.errors.FieldValidationException;
import it.nexus.web.errors.NotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentTemplateServiceImpl implements DocumentTemplateService {

    /** Come il CV e il vincolo ck_stored_file_content_size. */
    static final int MAX_BYTES = 5 * 1024 * 1024;
    static final String DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    /** Un .docx è un archivio ZIP: il tipo si riconosce dal contenuto, non dal nome. */
    private static final byte[] ZIP_MAGIC = {'P', 'K', 3, 4};
    private static final String NOT_DOCX = "Il file deve essere un documento Word .docx: un .doc del vecchio formato "
            + "va salvato come .docx";

    private final DocumentTemplateRepository templates;
    private final ProjectRepository projects;
    private final InternshipTypeRepository internshipTypes;
    private final CareAgencyRepository careAgencies;
    private final StoredFileRepository files;
    private final StoredFileContentRepository contents;
    private final AuditService auditService;
    private final ReferenceMapper referenceMapper;

    @Override
    @Transactional(readOnly = true)
    public List<DocumentTemplateDTO> list() {
        return templates.findAllByOrderByKindAscNameAsc().stream().map(this::toDto).toList();
    }

    @Override
    public List<String> placeholders() {
        return DocumentPlaceholders.KNOWN;
    }

    @Override
    public DocumentTemplateDTO upload(DocumentTemplateFormDTO form, String fileName, byte[] content) {
        InternshipDocumentKind kind = Optional.ofNullable(form.document())
                .flatMap(d -> Arrays.stream(InternshipDocumentKind.values()).filter(k -> k.name().equals(d)).findFirst())
                .orElseThrow(() -> new FieldValidationException("document", "Scegli il documento"));
        Project project = find(form.projectId(), projects)
                .orElseThrow(() -> new FieldValidationException("projectId", "Scegli il progetto"));
        InternshipType type = find(form.internshipTypeId(), internshipTypes)
                .orElseThrow(() -> new FieldValidationException("internshipTypeId", "Scegli il tipo di tirocinio"));
        // stessa regola della scheda: nessun tipo indicato per il progetto = tutti ammessi (US-115)
        if (!project.getAllowedInternshipTypes().isEmpty() && !project.getAllowedInternshipTypes().contains(type)) {
            throw new FieldValidationException("internshipTypeId",
                    "Il progetto %s non ammette il tipo %s".formatted(project.getName(), type.getName()));
        }
        CareAgency agency = null;
        if (form.careAgencyId() != null && !form.careAgencyId().isBlank()) {
            agency = find(form.careAgencyId(), careAgencies)
                    .orElseThrow(() -> new FieldValidationException("careAgencyId", "Ente non valido"));
        }
        String name = form.name() == null ? "" : form.name().strip();
        if (name.isEmpty()) {
            throw new FieldValidationException("name", "Inserisci il nome del modello");
        }
        if (name.length() > 200) {
            throw new FieldValidationException("name", "Massimo 200 caratteri");
        }
        checkDocx(content);
        DocumentTemplate saved = templates.saveAndFlush(
                new DocumentTemplate(kind, project, type, agency, name, store(fileName, content)));
        auditService.record(AuditEntityType.DOCUMENT_TEMPLATE, saved.getId(), "TEMPLATE_UPLOADED", AuditChanges.of()
                .value("document", null, kind.name())
                .value("project", null, project.getCode())
                .value("internshipType", null, type.getCode())
                .value("careAgency", null, agency == null ? null : agency.getCode())
                .value("name", null, name)
                .value("sizeBytes", null, content.length), null);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadedFile file(String id) {
        DocumentTemplate template = template(id);
        return new DownloadedFile(template.getFile().getOriginalName(), DOCX, content(template));
    }

    @Override
    public DocumentTemplateDTO replaceFile(String id, Long version, String fileName, byte[] content) {
        DocumentTemplate template = template(id);
        checkVersion(template, version);
        checkDocx(content);
        StoredFile old = template.getFile();
        long oldSize = old.getSizeBytes();
        template.setFile(store(fileName, content));
        DocumentTemplate saved = templates.saveAndFlush(template);
        // i documenti già scaricati non sono in NEXUS: il file vecchio non serve più
        contents.deleteById(old.getId());
        files.delete(old);
        files.flush();
        auditService.record(AuditEntityType.DOCUMENT_TEMPLATE, saved.getId(), "TEMPLATE_FILE_REPLACED",
                AuditChanges.of().value("sizeBytes", oldSize, content.length), null);
        return toDto(saved);
    }

    @Override
    public DocumentTemplateDTO setActive(String id, DocumentTemplateActiveDTO dto) {
        DocumentTemplate template = template(id);
        checkVersion(template, dto.version());
        if (template.isActive() == dto.active()) {
            return toDto(template);
        }
        template.setActive(dto.active());
        DocumentTemplate saved = templates.saveAndFlush(template);
        auditService.record(AuditEntityType.DOCUMENT_TEMPLATE, saved.getId(),
                dto.active() ? "TEMPLATE_ACTIVATED" : "TEMPLATE_DEACTIVATED",
                AuditChanges.of().value("active", !dto.active(), dto.active()), null);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentTemplate> candidates(InternshipDocumentKind kind, Ticket ticket, Long internshipTypeId) {
        if (internshipTypeId == null) {
            return List.of();
        }
        List<DocumentTemplate> all = templates.findByKindAndProjectIdAndInternshipTypeIdAndActiveTrueOrderByNameAsc(
                kind, ticket.getProject().getId(), internshipTypeId);
        CareAgency agency = ticket.getBeneficiary().getCareAgency();
        // per id: l'ente del modello e quello del beneficiario arrivano spesso come proxy di Hibernate
        List<DocumentTemplate> forAgency = agency == null ? List.of() : all.stream()
                .filter(t -> t.getCareAgency() != null && t.getCareAgency().getId().equals(agency.getId()))
                .toList();
        return forAgency.isEmpty() ? all.stream().filter(t -> t.getCareAgency() == null).toList() : forAgency;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] content(DocumentTemplate template) {
        return contents.findById(template.getFile().getId())
                .orElseThrow(() -> new NotFoundException("File del modello non trovato"))
                .getContent();
    }

    /**
     * Un .docx leggibile, al massimo 5 MB, con i soli segnaposto che NEXUS sa compilare: altrimenti l'errore sul file,
     * con l'elenco dei segnaposto sconosciuti.
     */
    private static void checkDocx(byte[] content) {
        if (content == null || content.length == 0) {
            throw new FieldValidationException("file", "Scegli il file del modello");
        }
        if (content.length > MAX_BYTES) {
            throw new FieldValidationException("file", "Il modello supera i 5 MB");
        }
        if (content.length < ZIP_MAGIC.length || !Arrays.equals(Arrays.copyOf(content, ZIP_MAGIC.length), ZIP_MAGIC)) {
            throw new FieldValidationException("file", NOT_DOCX);
        }
        Set<String> found;
        try (InputStream in = new ByteArrayInputStream(content)) {
            found = DocxTemplate.placeholders(in);
        } catch (IOException | RuntimeException e) {
            // Apache POI segnala i file non Word con eccezioni non controllate
            throw new FieldValidationException("file", NOT_DOCX);
        }
        List<String> unknown = found.stream().filter(p -> !DocumentPlaceholders.KNOWN.contains(p)).toList();
        if (!unknown.isEmpty()) {
            throw new FieldValidationException("file", "Il modello usa segnaposto che NEXUS non conosce: "
                    + unknown.stream().map(p -> "{" + p + "}").collect(Collectors.joining(", ")));
        }
    }

    private StoredFile store(String fileName, byte[] content) {
        StoredFile file = files.save(new StoredFile(docxName(fileName), DOCX, content.length, sha256(content),
                "db:" + UUID.randomUUID()));
        contents.save(new StoredFileContent(file, content));
        return file;
    }

    private DocumentTemplate template(String id) {
        return find(id, templates).orElseThrow(() -> new NotFoundException("Modello non trovato"));
    }

    private static void checkVersion(DocumentTemplate template, Long version) {
        if (version == null || template.getVersion() != version) {
            throw new ConflictException("Il modello è stato modificato nel frattempo: ricarica la pagina e riprova");
        }
    }

    private static <T> Optional<T> find(String id, JpaRepository<T, Long> repository) {
        return id == null || id.isBlank() ? Optional.empty()
                : Optional.ofNullable(TsidMapper.toInternal(id)).flatMap(repository::findById);
    }

    private DocumentTemplateDTO toDto(DocumentTemplate t) {
        return new DocumentTemplateDTO(TsidMapper.toExternal(t.getId()), t.getKind().name(),
                referenceMapper.toDto(t.getProject()), referenceMapper.toDto(t.getInternshipType()),
                referenceMapper.toDto(t.getCareAgency()), t.getName(), t.getFile().getOriginalName(),
                t.getFile().getSizeBytes(), t.isActive(), t.getCreatedAt(), t.getUpdatedAt(), t.getVersion());
    }

    /** Nome del file come caricato, senza percorso né caratteri di controllo, con l'estensione .docx. */
    private static String docxName(String fileName) {
        String name = fileName == null ? "" : fileName.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}\"]", "").strip();
        if (name.isEmpty()) {
            name = "modello.docx";
        }
        if (!name.toLowerCase(Locale.ROOT).endsWith(".docx")) {
            name += ".docx";
        }
        return name.length() > 200 ? name.substring(name.length() - 200) : name;
    }

    private static String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
