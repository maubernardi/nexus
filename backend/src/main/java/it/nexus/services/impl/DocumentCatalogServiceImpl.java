package it.nexus.services.impl;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.AbstractReferenceEntity;
import it.nexus.domain.CareAgency;
import it.nexus.domain.CompetenceSite;
import it.nexus.domain.InternshipType;
import it.nexus.domain.Project;
import it.nexus.domain.ProjectPromoter;
import it.nexus.domain.ProjectVariable;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.CareAgencyDTO;
import it.nexus.domain.dto.CareAgencyFormDTO;
import it.nexus.domain.dto.CompetenceSiteDTO;
import it.nexus.domain.dto.ProjectDocumentsDTO;
import it.nexus.domain.dto.ProjectDocumentsFormDTO;
import it.nexus.domain.dto.ProjectInternshipTypesDTO;
import it.nexus.domain.dto.ProjectPromoterDTO;
import it.nexus.domain.dto.ProjectVariableDTO;
import it.nexus.domain.dto.ReferenceItemDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.mapper.ReferenceMapper;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.CareAgencyRepository;
import it.nexus.repository.CompetenceSiteRepository;
import it.nexus.repository.InternshipTypeRepository;
import it.nexus.repository.ProjectRepository;
import it.nexus.repository.ProjectVariableRepository;
import it.nexus.services.AuditService;
import it.nexus.services.DocumentCatalogService;
import it.nexus.services.DocumentTemplateService;
import it.nexus.services.documents.DocumentPlaceholders;
import it.nexus.web.errors.ConflictException;
import it.nexus.web.errors.FieldValidationException;
import it.nexus.web.errors.NotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentCatalogServiceImpl implements DocumentCatalogService {

    private static final Comparator<AbstractReferenceEntity> BY_NAME =
            Comparator.comparing(e -> e.getName().toLowerCase(Locale.ITALIAN));

    private final CompetenceSiteRepository competenceSites;
    private final ProjectRepository projects;
    private final InternshipTypeRepository internshipTypes;
    private final CareAgencyRepository careAgencies;
    private final ReferenceMapper referenceMapper;
    private final AuditService auditService;
    private final ProjectVariableRepository variables;
    private final DocumentTemplateService documentTemplates;

    @Override
    @Transactional(readOnly = true)
    public List<CompetenceSiteDTO> competenceSites() {
        return competenceSites.findAllByOrderByActiveDescNameAsc().stream().map(this::toDto).toList();
    }

    @Override
    public CompetenceSiteDTO setCompetenceSiteProjects(String siteId, List<String> projectIds) {
        CompetenceSite site = find(competenceSites, siteId, "Sede di competenza non trovata");
        Set<Project> chosen = choose(projects, projectIds, site.getProjects(), Project::isActive, "projectIds",
                "Progetto non valido");
        List<String> before = codes(site.getProjects());
        site.getProjects().retainAll(chosen);
        site.getProjects().addAll(chosen);
        List<String> after = codes(site.getProjects());
        if (!before.equals(after)) {
            auditService.record(AuditEntityType.COMPETENCE_SITE, site.getId(), "UPDATE",
                    AuditChanges.of().value("projects", before, after), null);
        }
        return toDto(competenceSites.saveAndFlush(site));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectInternshipTypesDTO> projectInternshipTypes() {
        return projects.findAllByOrderByActiveDescNameAsc().stream().map(this::toDto).toList();
    }

    @Override
    public ProjectInternshipTypesDTO setProjectInternshipTypes(String projectId, List<String> internshipTypeIds) {
        Project project = find(projects, projectId, "Progetto non trovato");
        Set<InternshipType> chosen = choose(internshipTypes, internshipTypeIds, project.getAllowedInternshipTypes(),
                InternshipType::isActive, "internshipTypeIds", "Tipo di tirocinio non valido");
        List<String> before = codes(project.getAllowedInternshipTypes());
        project.getAllowedInternshipTypes().retainAll(chosen);
        project.getAllowedInternshipTypes().addAll(chosen);
        List<String> after = codes(project.getAllowedInternshipTypes());
        if (!before.equals(after)) {
            auditService.record(AuditEntityType.PROJECT, project.getId(), "UPDATE",
                    AuditChanges.of().value("internshipTypes", before, after), null);
        }
        return toDto(projects.saveAndFlush(project));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDocumentsDTO> projectDocuments() {
        return projects.findAllByOrderByActiveDescNameAsc().stream().map(this::toDocumentsDto).toList();
    }

    @Override
    public ProjectDocumentsDTO updateProjectDocuments(String projectId, ProjectDocumentsFormDTO form) {
        Project project = find(projects, projectId, "Progetto non trovato");
        String notice = blankToNull(form.noticeReference());
        String header = blankToNull(form.headerLine());
        // il testo predefinito non si salva: resta quello del codice anche se un giorno cambia
        if (Project.DEFAULT_NOTICE_REFERENCE.equals(notice)) {
            notice = null;
        }
        AuditChanges changes = AuditChanges.of()
                .value("noticeReference", project.getNoticeReference(), notice)
                .value("headerLine", project.getHeaderLine(), header);
        project.setNoticeReference(notice);
        project.setHeaderLine(header);
        if (form.promoter() != null) {
            ProjectPromoter promoter = promoter(form.promoter());
            changes = changes.merge(promoterChanges(project.getPromoter(), promoter));
            project.setPromoter(promoter);
        }
        if (form.variables() != null) {
            changes = changes.merge(saveVariables(project, form.variables()));
        }
        Project saved = projects.saveAndFlush(project);
        if (!changes.asMap().isEmpty()) {
            auditService.record(AuditEntityType.PROJECT, saved.getId(), "UPDATE", changes, null);
        }
        return toDocumentsDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CareAgencyDTO> careAgencies() {
        return careAgencies.findAllByOrderByActiveDescNameAsc().stream().map(DocumentCatalogServiceImpl::toDto)
                .toList();
    }

    @Override
    public CareAgencyDTO updateCareAgency(String id, CareAgencyFormDTO form) {
        CareAgency agency = find(careAgencies, id, "Ente non trovato");
        if (agency.getVersion() != form.version()) {
            throw new ConflictException("L'ente è stato modificato nel frattempo: ricarica la pagina e riprova");
        }
        String fiscalCode = blankToNull(form.fiscalCode());
        // i dati dell'ente si vedono nell'audit; quelli delle persone no (come i referenti delle aziende, EN-4)
        AuditChanges changes = AuditChanges.of()
                .value("kind", agency.getKind(), form.kind())
                .value("fiscalCode", agency.getFiscalCode(),
                        fiscalCode == null ? null : fiscalCode.toUpperCase(Locale.ROOT))
                .value("legalAddress", agency.getLegalAddress(), blankToNull(form.legalAddress()))
                .redactedIfChanged("representativeName", agency.getRepresentativeName(),
                        blankToNull(form.representativeName()))
                .value("representativeRole", agency.getRepresentativeRole(), blankToNull(form.representativeRole()))
                .redactedIfChanged("representativeBirthPlace", agency.getRepresentativeBirthPlace(),
                        blankToNull(form.representativeBirthPlace()))
                .redactedIfChanged("representativeBirthDate", agency.getRepresentativeBirthDate(),
                        form.representativeBirthDate())
                .redactedIfChanged("referentName", agency.getReferentName(), blankToNull(form.referentName()))
                .redactedIfChanged("referentPhone", agency.getReferentPhone(), blankToNull(form.referentPhone()))
                .redactedIfChanged("referentEmail", agency.getReferentEmail(), blankToNull(form.referentEmail()));
        agency.setKind(form.kind());
        agency.setFiscalCode(fiscalCode == null ? null : fiscalCode.toUpperCase(Locale.ROOT));
        agency.setLegalAddress(blankToNull(form.legalAddress()));
        agency.setRepresentativeName(blankToNull(form.representativeName()));
        agency.setRepresentativeRole(blankToNull(form.representativeRole()));
        agency.setRepresentativeBirthPlace(blankToNull(form.representativeBirthPlace()));
        agency.setRepresentativeBirthDate(form.representativeBirthDate());
        agency.setReferentName(blankToNull(form.referentName()));
        agency.setReferentPhone(blankToNull(form.referentPhone()));
        agency.setReferentEmail(blankToNull(form.referentEmail()));
        CareAgency saved = careAgencies.saveAndFlush(agency);
        if (!changes.asMap().isEmpty()) {
            auditService.record(AuditEntityType.CARE_AGENCY, saved.getId(), "UPDATE", changes, null);
        }
        return toDto(saved);
    }

    private static <T> T find(JpaRepository<T, Long> repository, String id, String notFound) {
        return Optional.ofNullable(TsidMapper.toInternal(id)).flatMap(repository::findById)
                .orElseThrow(() -> new NotFoundException(notFound));
    }

    /** Le voci scelte: nuove solo se attive; quelle già collegate restano anche se nel frattempo disattivate. */
    private static <T extends AbstractReferenceEntity> Set<T> choose(JpaRepository<T, Long> repository,
            List<String> ids, Collection<T> current, Function<T, Boolean> active, String field, String invalid) {
        Set<T> chosen = new HashSet<>();
        for (String id : new LinkedHashSet<>(ids)) {
            T item = Optional.ofNullable(TsidMapper.toInternal(id)).flatMap(repository::findById)
                    .filter(i -> active.apply(i) || current.contains(i))
                    .orElseThrow(() -> new FieldValidationException(field, invalid));
            chosen.add(item);
        }
        return chosen;
    }

    private static List<String> codes(Collection<? extends AbstractReferenceEntity> items) {
        return items.stream().map(AbstractReferenceEntity::getCode).sorted().toList();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private List<ReferenceItemDTO> sorted(Collection<? extends AbstractReferenceEntity> items) {
        return items.stream().sorted(BY_NAME).map(referenceMapper::toDto).filter(Objects::nonNull).toList();
    }

    private CompetenceSiteDTO toDto(CompetenceSite site) {
        return new CompetenceSiteDTO(TsidMapper.toExternal(site.getId()), site.getCode(), site.getName(),
                site.isActive(), sorted(site.getProjects()));
    }

    private ProjectInternshipTypesDTO toDto(Project project) {
        return new ProjectInternshipTypesDTO(referenceMapper.toDto(project), project.isActive(),
                sorted(project.getAllowedInternshipTypes()));
    }

    private ProjectDocumentsDTO toDocumentsDto(Project project) {
        ProjectPromoter p = project.getPromoter();
        ProjectPromoterDTO promoter = new ProjectPromoterDTO(p.name(), p.address(), p.fiscalCode(), p.representative(),
                p.representativeRole(), p.representativeBirthPlace(), p.representativeBirthDate(), p.phone(),
                p.email(), p.pec(), p.inailPosition(), p.rcInsurer(), p.rcPolicy(), p.rcExpiry());
        List<ProjectVariableDTO> vars = variables.findByProjectOrderByNameAsc(project).stream()
                .map(v -> new ProjectVariableDTO(v.getName(), v.getValue(), v.getDescription()))
                .toList();
        return new ProjectDocumentsDTO(referenceMapper.toDto(project), project.isActive(),
                project.effectiveNoticeReference(), project.getNoticeReference() == null, project.getHeaderLine(),
                promoter, vars);
    }

    /** Il promotore dal modulo: testi senza spazi ai lati, vuoti = assenti (US-1013). */
    private static ProjectPromoter promoter(ProjectPromoterDTO f) {
        return new ProjectPromoter(blankToNull(f.name()), blankToNull(f.address()), blankToNull(f.fiscalCode()),
                blankToNull(f.representative()), blankToNull(f.representativeRole()),
                blankToNull(f.representativeBirthPlace()), f.representativeBirthDate(), blankToNull(f.phone()),
                blankToNull(f.email()), blankToNull(f.pec()), blankToNull(f.inailPosition()),
                blankToNull(f.rcInsurer()), blankToNull(f.rcPolicy()), f.rcExpiry());
    }

    /** Nome, luogo e data di nascita del legale rappresentante sono dati personali: solo che sono cambiati (D6). */
    private static AuditChanges promoterChanges(ProjectPromoter b, ProjectPromoter a) {
        return AuditChanges.of()
                .value("promoter.name", b.name(), a.name())
                .value("promoter.address", b.address(), a.address())
                .value("promoter.fiscalCode", b.fiscalCode(), a.fiscalCode())
                .redactedIfChanged("promoter.representative", b.representative(), a.representative())
                .value("promoter.representativeRole", b.representativeRole(), a.representativeRole())
                .redactedIfChanged("promoter.representativeBirthPlace", b.representativeBirthPlace(),
                        a.representativeBirthPlace())
                .redactedIfChanged("promoter.representativeBirthDate", b.representativeBirthDate(),
                        a.representativeBirthDate())
                .value("promoter.phone", b.phone(), a.phone())
                .value("promoter.email", b.email(), a.email())
                .value("promoter.pec", b.pec(), a.pec())
                .value("promoter.inailPosition", b.inailPosition(), a.inailPosition())
                .value("promoter.rcInsurer", b.rcInsurer(), a.rcInsurer())
                .value("promoter.rcPolicy", b.rcPolicy(), a.rcPolicy())
                .value("promoter.rcExpiry", b.rcExpiry(), a.rcExpiry());
    }

    /**
     * Le variabili del progetto diventano quelle del modulo (D3): nuove, aggiornate, tolte. Un nome di NEXUS o ripetuto
     * è rifiutato; una variabile usata da un modello del progetto non si toglie.
     */
    private AuditChanges saveVariables(Project project, List<ProjectVariableDTO> form) {
        Map<String, ProjectVariableDTO> wanted = new LinkedHashMap<>();
        for (int i = 0; i < form.size(); i++) {
            ProjectVariableDTO v = form.get(i);
            String name = v.name().strip();
            String field = "variables[%d].name".formatted(i);
            if (DocumentPlaceholders.KNOWN.contains(name)) {
                throw new FieldValidationException(field, "%s è già un segnaposto di NEXUS: scegli un altro nome"
                        .formatted(name));
            }
            if (wanted.putIfAbsent(name, v) != null) {
                throw new FieldValidationException(field, "La variabile %s c'è già".formatted(name));
            }
        }
        AuditChanges changes = AuditChanges.of();
        Map<String, ProjectVariable> saved = new LinkedHashMap<>();
        variables.findByProjectOrderByNameAsc(project).forEach(v -> saved.put(v.getName(), v));
        for (ProjectVariable old : saved.values()) {
            if (wanted.containsKey(old.getName())) {
                continue;
            }
            List<String> users = documentTemplates.templatesUsing(project, old.getName());
            if (!users.isEmpty()) {
                throw new FieldValidationException("variables",
                        "La variabile %s è usata dal modello «%s»: toglila prima dal modello"
                                .formatted(old.getName(), String.join("», «", users)));
            }
            changes = changes.value("variable:" + old.getName(), old.getValue(), null);
            variables.delete(old);
        }
        for (ProjectVariableDTO v : wanted.values()) {
            String name = v.name().strip();
            String value = v.value().strip();
            String description = blankToNull(v.description());
            ProjectVariable old = saved.get(name);
            if (old == null) {
                variables.save(new ProjectVariable(project, name, value, description));
                changes = changes.value("variable:" + name, null, value);
            } else {
                changes = changes.value("variable:" + name, old.getValue(), value)
                        .value("variable:" + name + ".description", old.getDescription(), description);
                old.setValue(value);
                old.setDescription(description);
            }
        }
        variables.flush();
        return changes;
    }

    private static CareAgencyDTO toDto(CareAgency a) {
        return new CareAgencyDTO(TsidMapper.toExternal(a.getId()), a.getCode(), a.getName(), a.isActive(), a.getKind(),
                a.getFiscalCode(), a.getLegalAddress(), a.getRepresentativeName(), a.getRepresentativeRole(),
                a.getRepresentativeBirthPlace(), a.getRepresentativeBirthDate(), a.getReferentName(),
                a.getReferentPhone(), a.getReferentEmail(), a.getVersion());
    }
}
