package it.nexus.services.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.config.TimeConfig;
import it.nexus.domain.ClosureReport;
import it.nexus.domain.DocumentTemplate;
import it.nexus.domain.ProjectVariable;
import it.nexus.domain.Ticket;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.DocumentTemplateChoiceDTO;
import it.nexus.domain.dto.ExtensionDTO;
import it.nexus.domain.dto.InternshipDTO;
import it.nexus.domain.dto.TrainingProjectDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.InternshipDocumentKind;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.ClosureReportRepository;
import it.nexus.repository.ProjectVariableRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.services.AuditService;
import it.nexus.services.DocumentTemplateService;
import it.nexus.services.InternshipDocumentService;
import it.nexus.services.InternshipService;
import it.nexus.services.documents.DocumentValues;
import it.nexus.services.documents.DocxTemplate;
import it.nexus.web.errors.ConflictException;
import it.nexus.web.errors.FieldValidationException;
import it.nexus.web.errors.NotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class InternshipDocumentServiceImpl implements InternshipDocumentService {

    /** Dato mancante: una riga da completare a mano, come su un modulo cartaceo. */
    public static final String BLANK = DocumentValues.BLANK;

    private final InternshipService internshipService;
    private final TicketRepository ticketRepository;
    private final ClosureReportRepository closureReportRepository;
    private final AuditService auditService;
    private final DocumentTemplateService documentTemplateService;
    private final ProjectVariableRepository projectVariables;
    private final Clock clock;

    @Override
    public GeneratedDocument generate(String ticketId, InternshipDocumentKind kind, String templateId,
            Integer extension, Integer project) {
        // accesso e stato come la scheda: solo il Tutor della segnalazione, dall'appuntamento fissato in poi
        InternshipDTO scheda = internshipService.get(ticketId);
        Ticket ticket = ticket(ticketId);
        ClosureReport report = closureReportRepository.findByTicketId(ticket.getId()).orElse(null);
        if (kind == InternshipDocumentKind.RELAZIONE_FINALE
                && (ticket.getStatus() != TicketStatus.CLOSED || report == null)) {
            throw new ConflictException("La relazione finale si scarica a tirocinio chiuso");
        }
        // la relazione finale riguarda tutto il tirocinio: proroga e progetto non contano (D1)
        boolean partial = kind != InternshipDocumentKind.RELAZIONE_FINALE;
        ExtensionDTO ext = partial ? extension(scheda, extension) : null;
        TrainingProjectDTO trainingProject = kind == InternshipDocumentKind.PROGETTO_FORMATIVO
                ? project(ext == null ? scheda.trainingProjects() : ext.trainingProjects(), project) : null;
        DocumentValues.Convention convention = ext == null ? DocumentValues.Convention.initial(scheda)
                : DocumentValues.Convention.of(ext);
        DocumentTemplate chosen = choose(kind, ticket, scheda, templateId);
        byte[] content;
        try (InputStream template = chosen == null ? fallback(kind, scheda)
                : new ByteArrayInputStream(documentTemplateService.content(chosen))) {
            Map<String, String> variables = projectVariables.findByProjectOrderByNameAsc(ticket.getProject()).stream()
                    .collect(Collectors.toMap(ProjectVariable::getName, ProjectVariable::getValue));
            content = DocxTemplate.fill(template, DocumentValues.of(new DocumentValues.Sources(ticket, scheda,
                    convention, trainingProject, report, variables, today())));
        } catch (IOException e) {
            throw new UncheckedIOException("Modello " + kind + " non leggibile", e);
        }
        auditService.record(AuditEntityType.TICKET, ticket.getId(), "DOCUMENT_GENERATED", AuditChanges.of()
                .value("document", null, kind.name())
                .value("extension", null, ext == null ? null : ext.number())
                .value("project", null, trainingProject == null ? null : trainingProject.number())
                .value("template", null, chosen == null ? "modello generico" : chosen.getName()), null);
        return new GeneratedDocument(filename(kind, ticket, ext, trainingProject), content);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentTemplateChoiceDTO> templates(String ticketId, InternshipDocumentKind kind) {
        InternshipDTO scheda = internshipService.get(ticketId);
        return documentTemplateService.candidates(kind, ticket(ticketId), internshipTypeId(scheda)).stream()
                .map(t -> new DocumentTemplateChoiceDTO(TsidMapper.toExternal(t.getId()), t.getName()))
                .toList();
    }

    private Ticket ticket(String ticketId) {
        return ticketRepository.findById(TsidMapper.toInternal(ticketId))
                .orElseThrow(() -> new NotFoundException("Segnalazione non trovata"));
    }

    private LocalDate today() {
        return LocalDate.ofInstant(clock.instant(), TimeConfig.ZONE);
    }

    /** La proroga del documento (D1); nessun numero = la convenzione iniziale. */
    private static ExtensionDTO extension(InternshipDTO scheda, Integer number) {
        if (number == null) {
            return null;
        }
        return scheda.extensions().stream()
                .filter(e -> e.number() == number)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Proroga non trovata"));
    }

    /** Il progetto formativo del documento nella sua convenzione; nessun numero = il documento senza progetto. */
    private static TrainingProjectDTO project(List<TrainingProjectDTO> projects, Integer number) {
        if (number == null) {
            return null;
        }
        return projects.stream()
                .filter(p -> p.number() == number)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Progetto formativo non trovato"));
    }

    /**
     * Il nome del file: «Convenzione-proroga-1-segnalazione-12.docx»,
     * «Progetto-formativo-proroga-1-progetto-2-segnalazione-12.docx», «Progetto-formativo-2-segnalazione-12.docx».
     */
    private static String filename(InternshipDocumentKind kind, Ticket ticket, ExtensionDTO ext,
            TrainingProjectDTO project) {
        StringBuilder name = new StringBuilder(kind.filePrefix());
        if (ext != null) {
            name.append("-proroga-").append(ext.number());
            if (project != null) {
                name.append("-progetto");
            }
        }
        if (project != null) {
            name.append('-').append(project.number());
        }
        return name.append("-segnalazione-").append(ticket.getNumber()).append(".docx").toString();
    }

    /**
     * Il modello caricato da usare (US-1010, D4): quello scelto tra i candidati, l'unico candidato, oppure nessuno (il
     * modello generico). Con più candidati il Tutor deve scegliere.
     */
    private DocumentTemplate choose(InternshipDocumentKind kind, Ticket ticket, InternshipDTO scheda,
            String templateId) {
        List<DocumentTemplate> candidates = documentTemplateService.candidates(kind, ticket, internshipTypeId(scheda));
        if (templateId != null && !templateId.isBlank()) {
            return candidates.stream()
                    .filter(t -> TsidMapper.toExternal(t.getId()).equals(templateId))
                    .findFirst()
                    .orElseThrow(() -> new FieldValidationException("template",
                            "Il modello scelto non vale per questo documento: ricarica la pagina"));
        }
        if (candidates.size() > 1) {
            throw new FieldValidationException("template", "Scegli il modello");
        }
        return candidates.isEmpty() ? null : candidates.getFirst();
    }

    private static Long internshipTypeId(InternshipDTO scheda) {
        return scheda.internshipType() == null ? null : TsidMapper.toInternal(scheda.internshipType().id());
    }

    /**
     * Il modello generico nel codice: quello del tipo di tirocinio oppure, per i tipi senza modello (aggiunti
     * dall'ADMIN), quello predefinito.
     */
    private static InputStream fallback(InternshipDocumentKind kind, InternshipDTO scheda) throws IOException {
        String folder = "templates/documents/" + kind.slug() + "/";
        if (scheda.internshipType() != null) {
            ClassPathResource own = new ClassPathResource(folder + scheda.internshipType().code() + ".docx");
            if (own.exists()) {
                return own.getInputStream();
            }
        }
        return new ClassPathResource(folder + "default.docx").getInputStream();
    }
}
