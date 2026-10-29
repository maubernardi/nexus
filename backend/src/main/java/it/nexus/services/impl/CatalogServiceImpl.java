package it.nexus.services.impl;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.AbstractReferenceEntity;
import it.nexus.domain.InternshipType;
import it.nexus.domain.JobCategory;
import it.nexus.domain.ReferenceCodes;
import it.nexus.domain.Zone;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.CatalogFormDTO;
import it.nexus.domain.dto.CatalogItemDTO;
import it.nexus.domain.enumeration.CatalogKind;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.InternshipTypeRepository;
import it.nexus.repository.JobCategoryRepository;
import it.nexus.repository.ZoneRepository;
import it.nexus.services.AuditService;
import it.nexus.services.CatalogService;
import it.nexus.web.errors.ConflictException;
import it.nexus.web.errors.FieldValidationException;
import it.nexus.web.errors.NotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    private final ZoneRepository zoneRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final InternshipTypeRepository internshipTypeRepository;
    private final AuditService auditService;

    /** Le operazioni che servono su un catalogo: i due repository hanno metodi omonimi ma tipi diversi. */
    private record Store(
            Supplier<List<? extends AbstractReferenceEntity>> all,
            Function<Long, Optional<? extends AbstractReferenceEntity>> byId,
            Function<String, Optional<? extends AbstractReferenceEntity>> activeByName,
            Predicate<String> codeTaken,
            BiFunction<String, String, AbstractReferenceEntity> newItem,
            UnaryOperator<AbstractReferenceEntity> save) {
    }

    private Store store(CatalogKind kind) {
        return switch (kind) {
            case ZONES -> new Store(zoneRepository::findAllByOrderByActiveDescNameAsc, zoneRepository::findById,
                    zoneRepository::findFirstByNameIgnoreCaseAndActiveTrue, zoneRepository::existsByCode,
                    Zone::new, e -> zoneRepository.saveAndFlush((Zone) e));
            case JOB_CATEGORIES -> new Store(jobCategoryRepository::findAllByOrderByActiveDescNameAsc,
                    jobCategoryRepository::findById, jobCategoryRepository::findFirstByNameIgnoreCaseAndActiveTrue,
                    jobCategoryRepository::existsByCode, JobCategory::new,
                    e -> jobCategoryRepository.saveAndFlush((JobCategory) e));
            case INTERNSHIP_TYPES -> new Store(internshipTypeRepository::findAllByOrderByActiveDescNameAsc,
                    internshipTypeRepository::findById, internshipTypeRepository::findFirstByNameIgnoreCaseAndActiveTrue,
                    internshipTypeRepository::existsByCode, InternshipType::new,
                    e -> internshipTypeRepository.saveAndFlush((InternshipType) e));
        };
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogItemDTO> list(CatalogKind kind) {
        return store(kind).all().get().stream().map(CatalogServiceImpl::toDto).toList();
    }

    @Override
    public CatalogItemDTO create(CatalogKind kind, CatalogFormDTO dto) {
        Store store = store(kind);
        String name = dto.name().strip();
        requireNameFree(store, name, null);
        String code = ReferenceCodes.fromName(name, kind.fallbackCode(), store.codeTaken());
        AbstractReferenceEntity saved = store.save().apply(store.newItem().apply(code, name));
        auditService.record(kind.auditType(), saved.getId(), "CREATE", AuditChanges.of()
                .value("code", null, saved.getCode())
                .value("name", null, saved.getName()), null);
        return toDto(saved);
    }

    @Override
    public CatalogItemDTO rename(CatalogKind kind, String id, CatalogFormDTO dto) {
        Store store = store(kind);
        AbstractReferenceEntity item = find(store, id);
        String name = dto.name().strip();
        if (item.isActive()) {
            requireNameFree(store, name, item.getId());
        }
        String before = item.getName();
        item.setName(name);
        AbstractReferenceEntity saved = store.save().apply(item);
        if (!before.equals(name)) {
            auditService.record(kind.auditType(), saved.getId(), "UPDATE",
                    AuditChanges.of().value("name", before, name), null);
        }
        return toDto(saved);
    }

    @Override
    public CatalogItemDTO setActive(CatalogKind kind, String id, boolean active) {
        Store store = store(kind);
        AbstractReferenceEntity item = find(store, id);
        if (item.isActive() == active) {
            return toDto(item);
        }
        if (active) {
            store.activeByName().apply(item.getName()).ifPresent(other -> {
                throw new ConflictException("«%s» è già attiva: rinomina questa voce prima di riattivarla",
                        other.getName());
            });
        }
        item.setActive(active);
        AbstractReferenceEntity saved = store.save().apply(item);
        auditService.record(kind.auditType(), saved.getId(), active ? "ACTIVATE" : "DEACTIVATE",
                AuditChanges.of().value("active", !active, active), null);
        return toDto(saved);
    }

    private static void requireNameFree(Store store, String name, Long except) {
        store.activeByName().apply(name)
                .filter(other -> !other.getId().equals(except))
                .ifPresent(other -> {
                    throw new FieldValidationException("name", "«%s» è già nel catalogo".formatted(other.getName()));
                });
    }

    private static AbstractReferenceEntity find(Store store, String id) {
        return Optional.ofNullable(TsidMapper.toInternal(id))
                .flatMap(store.byId())
                .map(AbstractReferenceEntity.class::cast)
                .orElseThrow(() -> new NotFoundException("Voce non trovata"));
    }

    private static CatalogItemDTO toDto(AbstractReferenceEntity e) {
        return new CatalogItemDTO(TsidMapper.toExternal(e.getId()), e.getCode(), e.getName(), e.isActive());
    }
}
