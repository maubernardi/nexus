package it.nexus.services.impl;

import java.time.Clock;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.AbstractReferenceEntity;
import it.nexus.domain.AppUser;
import it.nexus.domain.BusinessCategory;
import it.nexus.domain.Company;
import it.nexus.domain.ContactRequest;
import it.nexus.domain.Zone;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.ContactRequestDTO;
import it.nexus.domain.dto.ContactRequestFormDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.ContactRequestStatus;
import it.nexus.mapper.ReferenceMapper;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.BusinessCategoryRepository;
import it.nexus.repository.ContactRequestRepository;
import it.nexus.repository.ZoneRepository;
import it.nexus.services.AuditService;
import it.nexus.services.ContactRequestService;
import it.nexus.services.CurrentAppUserService;
import it.nexus.web.errors.ConflictException;
import it.nexus.web.errors.FieldValidationException;
import it.nexus.web.errors.NotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ContactRequestServiceImpl implements ContactRequestService {

    private final ContactRequestRepository repository;
    private final ZoneRepository zoneRepository;
    private final BusinessCategoryRepository businessCategoryRepository;
    private final CurrentAppUserService currentAppUserService;
    private final AuditService auditService;
    private final ReferenceMapper referenceMapper;
    private final Clock clock;

    @Override
    public ContactRequestDTO create(ContactRequestFormDTO form) {
        AppUser tutor = currentAppUserService.getCurrentAppUser();
        // solo aziende non ancora in anagrafica (PO, 27/11): i doppioni li evitano i nomi simili prima dell'invio
        String name = form.companyName() == null || form.companyName().isBlank() ? null : form.companyName().strip();
        if (name == null) {
            throw new FieldValidationException("companyName", "Scrivi il nome dell'azienda");
        }
        Zone zone = activeEntry(form.zoneId(), zoneRepository, "zoneId", "Scegli la zona");
        BusinessCategory category = form.businessCategoryId() == null || form.businessCategoryId().isBlank() ? null
                : activeEntry(form.businessCategoryId(), businessCategoryRepository, "businessCategoryId", null);
        ContactRequest saved = repository.saveAndFlush(ContactRequest.forNewCompany(tutor, name, zone, category));
        auditService.record(AuditEntityType.CONTACT_REQUEST, saved.getId(), "CREATE", AuditChanges.of()
                .value("companyName", null, name)
                .value("zone", null, zone.getCode())
                .value("businessCategory", null, category == null ? null : category.getCode())
                .value("status", null, ContactRequestStatus.APERTA.name()), null);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactRequestDTO> mine() {
        return repository.findByRequestedByIdOrderByCreatedAtDescIdDesc(currentAppUserService.getCurrentAppUser().getId())
                .stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactRequestDTO> queue() {
        return repository.queue(
                EnumSet.of(ContactRequestStatus.APERTA, ContactRequestStatus.IN_CARICO))
                .stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long openCount() {
        return repository.countByStatus(ContactRequestStatus.APERTA);
    }

    @Override
    public ContactRequestDTO take(String id, long expectedVersion) {
        ContactRequest request = Optional.ofNullable(TsidMapper.toInternal(id))
                .flatMap(repository::findById)
                .orElseThrow(() -> new NotFoundException("Richiesta di contatto non trovata"));
        if (request.getStatus() != ContactRequestStatus.APERTA) {
            AppUser operator = request.getAssignedOperator();
            throw new ConflictException("La richiesta di contatto n. %d è già in carico a %s", request.getNumber(),
                    operator == null ? "un altro operatore" : fullName(operator));
        }
        if (request.getVersion() != expectedVersion) {
            throw new ConflictException("La richiesta di contatto n. %d è cambiata nel frattempo: ricarica la pagina",
                    request.getNumber());
        }
        AppUser me = currentAppUserService.getCurrentAppUser();
        request.take(me, clock.instant());
        ContactRequest saved = repository.saveAndFlush(request);
        auditService.record(AuditEntityType.CONTACT_REQUEST, saved.getId(), "TAKE", AuditChanges.of()
                .value("status", ContactRequestStatus.APERTA.name(), ContactRequestStatus.IN_CARICO.name())
                .value("assignedOperator", null, me.getUsername()), null);
        return toDto(saved);
    }

    private ContactRequestDTO toDto(ContactRequest r) {
        Company company = r.getCompany();
        return new ContactRequestDTO(
                TsidMapper.toExternal(r.getId()),
                r.getNumber(),
                r.getCreatedAt(),
                fullName(r.getRequestedBy()),
                company == null ? null : TsidMapper.toExternal(company.getId()),
                r.getCompanyName(),
                referenceMapper.toDto(r.getZone()),
                referenceMapper.toDto(r.getBusinessCategory()),
                r.getStatus(),
                r.getAssignedOperator() == null ? null : fullName(r.getAssignedOperator()),
                r.getTakenAt(),
                r.getVersion());
    }

    /** Voce attiva del catalogo: le voci disattivate non si scelgono più (US-109). */
    private static <T extends AbstractReferenceEntity> T activeEntry(String id, JpaRepository<T, Long> repository,
            String field, String missing) {
        if (id == null || id.isBlank()) {
            throw new FieldValidationException(field, missing);
        }
        return Optional.ofNullable(TsidMapper.toInternal(id))
                .flatMap(repository::findById)
                .filter(T::isActive)
                .orElseThrow(() -> new FieldValidationException(field, "Voce non valida"));
    }

    private static String fullName(AppUser user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}
