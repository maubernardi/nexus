package it.nexus.services.impl;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
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
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.CareAgencyDTO;
import it.nexus.domain.dto.CareAgencyFormDTO;
import it.nexus.domain.dto.CompetenceSiteDTO;
import it.nexus.domain.dto.ProjectInternshipTypesDTO;
import it.nexus.domain.dto.ReferenceItemDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.mapper.ReferenceMapper;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.CareAgencyRepository;
import it.nexus.repository.CompetenceSiteRepository;
import it.nexus.repository.InternshipTypeRepository;
import it.nexus.repository.ProjectRepository;
import it.nexus.services.AuditService;
import it.nexus.services.DocumentCatalogService;
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

    private static CareAgencyDTO toDto(CareAgency a) {
        return new CareAgencyDTO(TsidMapper.toExternal(a.getId()), a.getCode(), a.getName(), a.isActive(), a.getKind(),
                a.getFiscalCode(), a.getLegalAddress(), a.getRepresentativeName(), a.getRepresentativeRole(),
                a.getRepresentativeBirthPlace(), a.getRepresentativeBirthDate(), a.getReferentName(),
                a.getReferentPhone(), a.getReferentEmail(), a.getVersion());
    }
}
