package it.nexus.services.impl;

import java.time.Clock;
import java.time.LocalDate;
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
import it.nexus.domain.ContactRequestAttempt;
import it.nexus.domain.Zone;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.ContactAttemptDTO;
import it.nexus.domain.dto.ContactAttemptFormDTO;
import it.nexus.domain.dto.ContactRequestCloseDTO;
import it.nexus.domain.dto.ContactRequestDTO;
import it.nexus.domain.dto.ContactRequestDetailDTO;
import it.nexus.domain.dto.ContactRequestFormDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.ContactRequestStatus;
import it.nexus.domain.enumeration.NotificationType;
import it.nexus.domain.enumeration.Role;
import it.nexus.mapper.ReferenceMapper;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.BusinessCategoryRepository;
import it.nexus.repository.CompanyRepository;
import it.nexus.repository.ContactRequestAttemptRepository;
import it.nexus.repository.ContactRequestRepository;
import it.nexus.repository.ZoneRepository;
import it.nexus.services.AuditService;
import it.nexus.services.ContactRequestService;
import it.nexus.services.CurrentAppUserService;
import it.nexus.services.NotificationService;
import it.nexus.services.UserDisplayNameService;
import it.nexus.web.errors.ConflictException;
import it.nexus.web.errors.FieldValidationException;
import it.nexus.web.errors.NotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ContactRequestServiceImpl implements ContactRequestService {

    private final ContactRequestRepository repository;
    private final ContactRequestAttemptRepository attemptRepository;
    private final CompanyRepository companyRepository;
    private final NotificationService notificationService;
    private final UserDisplayNameService userDisplayNameService;
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
        ContactRequest request = find(id);
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

    @Override
    @Transactional(readOnly = true)
    public ContactRequestDetailDTO detail(String id) {
        AppUser me = currentAppUserService.getCurrentAppUser();
        ContactRequest request = find(id);
        // il Tutor vede solo le sue: quelle degli altri risultano inesistenti
        if (me.getRole() == Role.TUTOR && !request.getRequestedBy().getId().equals(me.getId())) {
            throw new NotFoundException("Richiesta di contatto non trovata");
        }
        return toDetail(request, me);
    }

    @Override
    public ContactRequestDetailDTO addAttempt(String id, ContactAttemptFormDTO form) {
        AppUser me = currentAppUserService.getCurrentAppUser();
        ContactRequest request = find(id);
        requireMine(request, me);
        if (form.date().isAfter(LocalDate.now(clock))) {
            throw new FieldValidationException("date", "La data non può essere futura");
        }
        String note = text(form.note());
        ContactRequestAttempt saved = attemptRepository.save(
                new ContactRequestAttempt(request, form.date(), form.outcome(), note));
        // la nota è testo libero: nell'audit solo data ed esito
        auditService.record(AuditEntityType.CONTACT_REQUEST, request.getId(), "ATTEMPT", AuditChanges.of()
                .value("attemptId", null, TsidMapper.toExternal(saved.getId()))
                .value("date", null, form.date().toString())
                .value("outcome", null, form.outcome().name()), null);
        return toDetail(request, me);
    }

    @Override
    public ContactRequestDetailDTO close(String id, ContactRequestCloseDTO form) {
        AppUser me = currentAppUserService.getCurrentAppUser();
        ContactRequest request = find(id);
        requireMine(request, me);
        if (request.getVersion() != form.version()) {
            throw new ConflictException("La richiesta di contatto n. %d è cambiata nel frattempo: ricarica la pagina",
                    request.getNumber());
        }
        // l'azienda dell'anagrafica, se l'operatore la collega: solo attiva
        Company company = form.companyId() == null || form.companyId().isBlank() ? null
                : Optional.ofNullable(TsidMapper.toInternal(form.companyId()))
                        .flatMap(companyRepository::findById)
                        .filter(Company::isActive)
                        .orElseThrow(() -> new FieldValidationException("companyId", "Azienda non valida"));
        request.close(form.outcome(), form.note().strip(), company, clock.instant());
        ContactRequest saved = repository.saveAndFlush(request);
        auditService.record(AuditEntityType.CONTACT_REQUEST, saved.getId(), "CLOSE", AuditChanges.of()
                .value("status", ContactRequestStatus.IN_CARICO.name(), ContactRequestStatus.CHIUSA.name())
                .value("outcome", null, form.outcome().name())
                .value("companyId", null, company == null ? null : TsidMapper.toExternal(company.getId())), null);
        notificationService.notify(saved.getRequestedBy(), NotificationType.CONTACT_REQUEST_CLOSED, saved);
        return toDetail(saved, me);
    }

    private ContactRequest find(String id) {
        return Optional.ofNullable(TsidMapper.toInternal(id))
                .flatMap(repository::findById)
                .orElseThrow(() -> new NotFoundException("Richiesta di contatto non trovata"));
    }

    /** Tentativi e chiusura solo da parte dell'operatore che ha la richiesta in carico (US-512). */
    private static void requireMine(ContactRequest request, AppUser me) {
        switch (request.getStatus()) {
            case APERTA -> throw new ConflictException(
                    "La richiesta di contatto n. %d non è ancora in carico: prima prendila in carico", request.getNumber());
            case CHIUSA -> throw new ConflictException("La richiesta di contatto n. %d è chiusa", request.getNumber());
            case IN_CARICO -> {
                if (!request.getAssignedOperator().getId().equals(me.getId())) {
                    throw new ConflictException("La richiesta di contatto n. %d è in carico a %s", request.getNumber(),
                            fullName(request.getAssignedOperator()));
                }
            }
        }
    }

    private ContactRequestDetailDTO toDetail(ContactRequest r, AppUser me) {
        List<ContactRequestAttempt> attempts =
                attemptRepository.findByContactRequestIdOrderByDateDescCreatedAtDescIdDesc(r.getId());
        var names = userDisplayNameService.displayNames(attempts.stream().map(ContactRequestAttempt::getCreatedBy)
                .toList());
        boolean assignedToMe = r.getStatus() == ContactRequestStatus.IN_CARICO && r.getAssignedOperator() != null
                && r.getAssignedOperator().getId().equals(me.getId());
        return new ContactRequestDetailDTO(
                toDto(r),
                r.getClosingNote(),
                r.getCompany() == null ? null : r.getCompany().getName(),
                attempts.stream().map(a -> new ContactAttemptDTO(TsidMapper.toExternal(a.getId()), a.getDate(),
                        a.getOutcome(), a.getNote(), names.get(a.getCreatedBy()), a.getCreatedAt())).toList(),
                assignedToMe);
    }

    private static String text(String value) {
        return value == null || value.isBlank() ? null : value.strip();
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
                r.getOutcome(),
                r.getClosedAt(),
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
