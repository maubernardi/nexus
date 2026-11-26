package it.nexus.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.AbstractReferenceEntity;
import it.nexus.domain.BusinessCategory;
import it.nexus.domain.Company;
import it.nexus.domain.CompanyContact;
import it.nexus.domain.ContactRole;
import it.nexus.domain.JobSlot;
import it.nexus.domain.Municipality;
import it.nexus.domain.Zone;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.CompanyContactFormDTO;
import it.nexus.domain.dto.CompanyDTO;
import it.nexus.domain.dto.CompanyDirectoryItemDTO;
import it.nexus.domain.dto.CompanyDirectoryPageDTO;
import it.nexus.domain.dto.CompanyFormDTO;
import it.nexus.domain.dto.CompanyPageDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.mapper.CompanyMapper;
import it.nexus.mapper.MunicipalityMapper;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.BusinessCategoryRepository;
import it.nexus.repository.CompanyRepository;
import it.nexus.repository.ContactRoleRepository;
import it.nexus.repository.JobSlotRepository;
import it.nexus.repository.ZoneRepository;
import it.nexus.services.AuditService;
import it.nexus.services.BoardPostService;
import it.nexus.services.CompanyReviewService;
import it.nexus.services.CompanySearch;
import it.nexus.services.CompanyService;
import it.nexus.services.JobSlotAssignments;
import it.nexus.services.MunicipalityService;
import it.nexus.web.errors.BadRequestException;
import it.nexus.web.errors.ConflictException;
import it.nexus.web.errors.FieldValidationException;
import it.nexus.web.errors.NotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private static final String DUPLICATE_VAT = "Esiste già un'azienda con questa partita IVA";
    private static final String LEGAL_MUNICIPALITY = "legalMunicipalityId";
    private static final String BUSINESS_CATEGORY = "businessCategoryId";
    private static final String ZONE = "zoneId";
    /** Id di filtro che non corrisponde a nessuna voce: gli id sono positivi. */
    private static final long NO_MATCH = -1L;
    private static final String OPERATING_MUNICIPALITY = "operatingMunicipalityId";

    private final CompanyRepository repository;
    private final JobSlotRepository jobSlotRepository;
    private final BoardPostService boardPostService;
    private final AuditService auditService;
    private final CompanyMapper mapper;
    private final MunicipalityService municipalityService;
    private final BusinessCategoryRepository businessCategoryRepository;
    private final ZoneRepository zoneRepository;
    private final CompanyReviewService companyReviewService;
    private final ContactRoleRepository contactRoleRepository;
    private final EntityManager entityManager;
    private final JobSlotAssignments jobSlotAssignments;

    /** Referente normalizzato (US-510): confronto prima/dopo e righe da inserire. */
    private record Contact(String name, ContactRole role, String phone, String mobile, String email) {

        /** Per il confronto: il ruolo conta per l'id. */
        private List<Object> key() {
            return Arrays.asList(name, role == null ? null : role.getId(), phone, mobile, email);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyPageDTO search(CompanySearch filters, int page, int size) {
        requirePage(page, size);
        Integer minReliability = filters.minReliability();
        if (minReliability != null && (minReliability < 1 || minReliability > 4)) {
            throw new BadRequestException("Stelle minime: da 1 a 4");
        }
        Integer minProjectInterest = filters.minProjectInterest();
        if (minProjectInterest != null && (minProjectInterest < 1 || minProjectInterest > 2)) {
            throw new BadRequestException("Pollici minimi: 1 o 2");
        }
        var result = repository.search(term(filters.search()), filters.includeInactive(), filters.toComplete(),
                filterId(filters.businessCategoryId()), filterId(filters.zoneId()), minReliability, minProjectInterest,
                PageRequest.of(page, size));
        return new CompanyPageDTO(result.map(mapper::toDto).getContent(), result.getTotalElements(), page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyDirectoryPageDTO directory(String search, String businessCategoryId, String zoneId, int page,
            int size) {
        requirePage(page, size);
        var result = repository.directory(term(search), filterId(businessCategoryId), filterId(zoneId),
                PageRequest.of(page, size));
        Map<Long, CompanyReviewService.Summary> reviews = companyReviewService.summaries(
                result.getContent().stream().map(Company::getId).toList());
        return new CompanyDirectoryPageDTO(result.getContent().stream().map(c -> withReviews(c, reviews)).toList(),
                result.getTotalElements(), page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyDirectoryItemDTO directoryItem(String id) {
        Company company = find(id);
        if (!company.isActive()) {
            throw new NotFoundException("Azienda non trovata");
        }
        return withReviews(company, companyReviewService.summaries(List.of(company.getId())));
    }

    /** Elemento dell'elenco del Tutor con la media delle valutazioni dei tirocini (US-1104), se ce ne sono. */
    private CompanyDirectoryItemDTO withReviews(Company company, Map<Long, CompanyReviewService.Summary> reviews) {
        CompanyReviewService.Summary summary = reviews.get(company.getId());
        CompanyDirectoryItemDTO item = mapper.toDirectoryItem(company);
        return summary == null ? item : item.withReviews(summary.average(), summary.count());
    }

    private static void requirePage(int page, int size) {
        if (page < 0) {
            throw new BadRequestException("Pagina non valida: %d", page);
        }
        if (!PAGE_SIZES.contains(size)) {
            throw new BadRequestException("Aziende per pagina: una tra %s", PAGE_SIZES);
        }
    }

    private static String term(String search) {
        return search == null || search.isBlank() ? null : search.strip();
    }

    /**
     * Id interno di un filtro dell'elenco (categoria, zona): {@code null} se il filtro non c'è. Un id non valido diventa
     * {@link #NO_MATCH}, che non è di nessuna voce: nessuna azienda, come una ricerca senza risultati.
     */
    private static Long filterId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        Long internal = TsidMapper.toInternal(id.strip());
        return internal == null ? NO_MATCH : internal;
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyDTO getById(String id) {
        return mapper.toDto(find(id));
    }

    @Override
    public CompanyDTO create(CompanyFormDTO dto) {
        String vatCode = dto.vatCode().strip();
        if (repository.findByVatCode(vatCode).isPresent()) {
            throw new FieldValidationException("vatCode", DUPLICATE_VAT);
        }
        Company company = new Company(dto.name().strip(), vatCode);
        List<Contact> contacts = contacts(dto.contacts(), Set.of());
        apply(company, dto, municipalityService.forAddress(dto.legalMunicipalityId(), null, LEGAL_MUNICIPALITY),
                municipalityService.forAddress(dto.operatingMunicipalityId(), null, OPERATING_MUNICIPALITY),
                businessCategory(dto.businessCategoryId(), null));
        company.setZone(zone(dto.zoneId(), null));
        contacts.forEach(c -> company.addContact(c.name(), c.role(), c.phone(), c.mobile(), c.email()));
        Company saved = repository.saveAndFlush(company);
        auditService.record(AuditEntityType.COMPANY, saved.getId(), "CREATE", AuditChanges.of()
                .value("name", null, saved.getName())
                .value("vatCode", null, saved.getVatCode())
                .value("legalAddress", null, saved.getLegalAddress())
                .value("legalMunicipality", null, MunicipalityMapper.label(saved.getLegalMunicipality()))
                .value("operatingAddress", null, saved.getOperatingAddress())
                .value("operatingMunicipality", null, MunicipalityMapper.label(saved.getOperatingMunicipality()))
                .value("businessCategory", null, name(saved.getBusinessCategory()))
                .value("activity", null, saved.getActivity())
                .value("zone", null, name(saved.getZone()))
                .value("reliability", null, saved.getReliability())
                .value("projectInterest", null, saved.getProjectInterest())
                // recapiti e referenti: dati personali, nell'audit senza valori (EN-4, US-510)
                .redactedIfChanged("phone", null, saved.getPhone())
                .redactedIfChanged("mobile", null, saved.getMobile())
                .redactedIfChanged("fax", null, saved.getFax())
                .redactedIfChanged("email", null, saved.getEmail())
                .redactedIfChanged("secondaryEmail", null, saved.getSecondaryEmail())
                .redactedIfChanged("contacts", List.of(), keys(contacts)), null);
        return mapper.toDto(saved);
    }

    @Override
    public CompanyDTO update(String id, CompanyFormDTO dto) {
        if (dto.version() == null) {
            throw new BadRequestException("Versione mancante");
        }
        Company company = find(id);
        requireVersion(company, dto.version());
        String vatCode = dto.vatCode().strip();
        repository.findByVatCode(vatCode).filter(other -> !other.getId().equals(company.getId())).ifPresent(other -> {
            throw new FieldValidationException("vatCode", DUPLICATE_VAT);
        });

        Municipality municipality = municipalityService.forAddress(dto.legalMunicipalityId(),
                company.getLegalMunicipality(), LEGAL_MUNICIPALITY);
        Municipality operatingMunicipality = municipalityService.forAddress(dto.operatingMunicipalityId(),
                company.getOperatingMunicipality(), OPERATING_MUNICIPALITY);
        BusinessCategory category = businessCategory(dto.businessCategoryId(), company.getBusinessCategory());
        Zone zone = zone(dto.zoneId(), company.getZone());
        List<Object> contactsBefore = keys(company.getContacts().stream().map(CompanyServiceImpl::contact).toList());
        List<Contact> contacts = contacts(dto.contacts(), company.getContacts().stream()
                .map(CompanyContact::getRole).filter(Objects::nonNull).map(ContactRole::getId)
                .collect(Collectors.toSet()));
        AuditChanges changes = AuditChanges.of()
                .value("name", company.getName(), dto.name().strip())
                .value("vatCode", company.getVatCode(), vatCode)
                .value("legalAddress", company.getLegalAddress(), blankToNull(dto.legalAddress()))
                .value("legalMunicipality", MunicipalityMapper.label(company.getLegalMunicipality()),
                        MunicipalityMapper.label(municipality))
                .value("operatingAddress", company.getOperatingAddress(), blankToNull(dto.operatingAddress()))
                .value("operatingMunicipality", MunicipalityMapper.label(company.getOperatingMunicipality()),
                        MunicipalityMapper.label(operatingMunicipality))
                .value("businessCategory", name(company.getBusinessCategory()), name(category))
                .value("activity", company.getActivity(), blankToNull(dto.activity()))
                .value("zone", name(company.getZone()), name(zone))
                .value("reliability", company.getReliability(), dto.reliability())
                .value("projectInterest", company.getProjectInterest(), dto.projectInterest())
                .redactedIfChanged("phone", company.getPhone(), blankToNull(dto.phone()))
                .redactedIfChanged("mobile", company.getMobile(), blankToNull(dto.mobile()))
                .redactedIfChanged("fax", company.getFax(), blankToNull(dto.fax()))
                .redactedIfChanged("email", company.getEmail(), blankToNull(dto.email()))
                .redactedIfChanged("secondaryEmail", company.getSecondaryEmail(), blankToNull(dto.secondaryEmail()))
                .redactedIfChanged("contacts", contactsBefore, keys(contacts));
        company.setName(dto.name().strip());
        company.setVatCode(vatCode);
        apply(company, dto, municipality, operatingMunicipality, category);
        company.setZone(zone);
        boolean contactsChanged = !contactsBefore.equals(keys(contacts));
        if (contactsChanged) {
            replaceContacts(company, contacts);
        }
        Company saved = repository.saveAndFlush(company);
        if (contactsChanged) {
            // i referenti sono dal lato inverso: cambiarli non incrementa da solo la versione dell'azienda, che invece
            // deve cambiare perché un collega con la versione di prima riceva il 409
            entityManager.lock(saved, LockModeType.PESSIMISTIC_FORCE_INCREMENT);
        }
        if (!changes.asMap().isEmpty()) {
            auditService.record(AuditEntityType.COMPANY, saved.getId(), "UPDATE", changes, null);
        }
        return mapper.toDto(saved);
    }

    @Override
    public CompanyDTO setActive(String id, long expectedVersion, boolean active) {
        Company company = find(id);
        requireVersion(company, expectedVersion);
        if (company.isActive() == active) {
            return mapper.toDto(company);
        }
        if (!active) {
            // un'azienda con proposte in corso non deve sparire dalle ricerche (US-504)
            List<JobSlot> blocked = jobSlotRepository.findBlockedByCompany(company.getId());
            if (!blocked.isEmpty()) {
                throw new ConflictException("Non si può disattivare l'azienda: %s", blocked.stream()
                        .map(j -> "la mansione «%s» è assegnata alla segnalazione %s"
                                .formatted(j.getTitle(), jobSlotAssignments.describe(jobSlotAssignments.holders(j))))
                        .collect(Collectors.joining("; ")));
            }
        }
        company.setActive(active);
        Company saved = repository.saveAndFlush(company);
        auditService.record(AuditEntityType.COMPANY, saved.getId(), active ? "ACTIVATE" : "DEACTIVATE",
                AuditChanges.of().value("active", !active, active), null);
        if (!active) {
            boardPostService.archiveForUnavailableSlots(
                    jobSlotRepository.findByCompany(saved.getId()).stream().map(JobSlot::getId).toList(),
                    "Azienda disattivata");
        }
        return mapper.toDto(saved);
    }

    private Company find(String id) {
        return Optional.ofNullable(TsidMapper.toInternal(id))
                .flatMap(repository::findById)
                .orElseThrow(() -> new NotFoundException("Azienda non trovata"));
    }

    private static void requireVersion(Company company, long expectedVersion) {
        if (company.getVersion() != expectedVersion) {
            throw new ConflictException("L'azienda è stata modificata nel frattempo: ricarica la pagina e riprova");
        }
    }

    /** Voce attiva del catalogo, oppure quella che l'azienda ha già anche se disattivata (US-509); vuoto = nessuna. */
    private BusinessCategory businessCategory(String id, BusinessCategory current) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return Optional.ofNullable(TsidMapper.toInternal(id.strip()))
                .flatMap(businessCategoryRepository::findById)
                .filter(c -> c.isActive() || c.equals(current))
                .orElseThrow(() -> new FieldValidationException(BUSINESS_CATEGORY, "Categoria non valida"));
    }

    /** Zona attiva del catalogo, oppure quella che l'azienda ha già anche se disattivata (US-511); vuoto = nessuna. */
    private Zone zone(String id, Zone current) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return Optional.ofNullable(TsidMapper.toInternal(id.strip()))
                .flatMap(zoneRepository::findById)
                .filter(z -> z.isActive() || z.equals(current))
                .orElseThrow(() -> new FieldValidationException(ZONE, "Zona non valida"));
    }

    /**
     * Referenti del modulo, normalizzati e controllati (US-510): serve il nome o il ruolo; il ruolo è una voce attiva
     * oppure uno che un referente dell'azienda ha già ({@code currentRoles}). Errori sul campo del referente.
     */
    private List<Contact> contacts(List<CompanyContactFormDTO> rows, Set<Long> currentRoles) {
        if (rows == null) {
            return List.of();
        }
        List<Contact> contacts = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            CompanyContactFormDTO row = rows.get(i);
            if (row == null) {
                throw new BadRequestException("Referente mancante");
            }
            String name = blankToNull(row.name());
            ContactRole role = contactRole(row.roleId(), currentRoles, i);
            if (name == null && role == null) {
                throw new FieldValidationException("contacts[%d].name".formatted(i),
                        "Inserisci il nome o scegli il ruolo");
            }
            contacts.add(new Contact(name, role, blankToNull(row.phone()), blankToNull(row.mobile()),
                    blankToNull(row.email())));
        }
        return contacts;
    }

    private ContactRole contactRole(String id, Set<Long> currentRoles, int index) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return Optional.ofNullable(TsidMapper.toInternal(id.strip()))
                .flatMap(contactRoleRepository::findById)
                .filter(r -> r.isActive() || currentRoles.contains(r.getId()))
                .orElseThrow(() -> new FieldValidationException("contacts[%d].roleId".formatted(index),
                        "Ruolo non valido"));
    }

    private static Contact contact(CompanyContact c) {
        return new Contact(c.getName(), c.getRole(), c.getPhone(), c.getMobile(), c.getEmail());
    }

    private static List<Object> keys(List<Contact> contacts) {
        return contacts.stream().<Object>map(Contact::key).toList();
    }

    /**
     * Referenti sostituiti in blocco: prima si cancellano i vecchi con un flush, perché Hibernate inserisce prima di
     * cancellare e una posizione ripetuta violerebbe {@code uq_company_contact_position}.
     */
    private void replaceContacts(Company company, List<Contact> contacts) {
        if (!company.getContacts().isEmpty()) {
            company.getContacts().clear();
            repository.flush();
        }
        contacts.forEach(c -> company.addContact(c.name(), c.role(), c.phone(), c.mobile(), c.email()));
    }

    private static String name(AbstractReferenceEntity item) {
        return item == null ? null : item.getName();
    }

    private static void apply(Company company, CompanyFormDTO dto, Municipality legalMunicipality,
            Municipality operatingMunicipality, BusinessCategory businessCategory) {
        company.setLegalAddress(blankToNull(dto.legalAddress()));
        company.setLegalMunicipality(legalMunicipality);
        company.setOperatingAddress(blankToNull(dto.operatingAddress()));
        company.setOperatingMunicipality(operatingMunicipality);
        company.setBusinessCategory(businessCategory);
        company.setActivity(blankToNull(dto.activity()));
        company.setPhone(blankToNull(dto.phone()));
        company.setMobile(blankToNull(dto.mobile()));
        company.setFax(blankToNull(dto.fax()));
        company.setEmail(blankToNull(dto.email()));
        company.setSecondaryEmail(blankToNull(dto.secondaryEmail()));
        company.setReliability(dto.reliability());
        company.setProjectInterest(dto.projectInterest());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
