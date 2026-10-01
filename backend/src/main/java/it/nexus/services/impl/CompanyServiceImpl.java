package it.nexus.services.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.Company;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.CompanyDTO;
import it.nexus.domain.dto.CompanyFormDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.mapper.CompanyMapper;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.CompanyRepository;
import it.nexus.services.AuditService;
import it.nexus.services.CompanyService;
import it.nexus.web.errors.BadRequestException;
import it.nexus.web.errors.ConflictException;
import it.nexus.web.errors.FieldValidationException;
import it.nexus.web.errors.NotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private static final String DUPLICATE_VAT = "Esiste già un'azienda con questa partita IVA";

    private final CompanyRepository repository;
    private final AuditService auditService;
    private final CompanyMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<CompanyDTO> search(String search, boolean includeInactive) {
        String term = search == null || search.isBlank() ? null : search.strip();
        return repository.search(term, includeInactive).stream().map(mapper::toDto).toList();
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
        apply(company, dto);
        Company saved = repository.saveAndFlush(company);
        auditService.record(AuditEntityType.COMPANY, saved.getId(), "CREATE", AuditChanges.of()
                .value("name", null, saved.getName())
                .value("vatCode", null, saved.getVatCode())
                .value("legalAddress", null, saved.getLegalAddress())
                .value("contactPerson", null, saved.getContactPerson())
                .value("phone", null, saved.getPhone())
                .value("email", null, saved.getEmail()), null);
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

        AuditChanges changes = AuditChanges.of()
                .value("name", company.getName(), dto.name().strip())
                .value("vatCode", company.getVatCode(), vatCode)
                .value("legalAddress", company.getLegalAddress(), blankToNull(dto.legalAddress()))
                .value("contactPerson", company.getContactPerson(), blankToNull(dto.contactPerson()))
                .value("phone", company.getPhone(), blankToNull(dto.phone()))
                .value("email", company.getEmail(), blankToNull(dto.email()));
        company.setName(dto.name().strip());
        company.setVatCode(vatCode);
        apply(company, dto);
        Company saved = repository.saveAndFlush(company);
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
        company.setActive(active);
        Company saved = repository.saveAndFlush(company);
        auditService.record(AuditEntityType.COMPANY, saved.getId(), active ? "ACTIVATE" : "DEACTIVATE",
                AuditChanges.of().value("active", !active, active), null);
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

    private static void apply(Company company, CompanyFormDTO dto) {
        company.setLegalAddress(blankToNull(dto.legalAddress()));
        company.setContactPerson(blankToNull(dto.contactPerson()));
        company.setPhone(blankToNull(dto.phone()));
        company.setEmail(blankToNull(dto.email()));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
