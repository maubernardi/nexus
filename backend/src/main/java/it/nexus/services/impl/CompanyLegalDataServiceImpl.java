package it.nexus.services.impl;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.Company;
import it.nexus.domain.CompanyAtecoCode;
import it.nexus.domain.CompanyLegalRepresentative;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.AtecoCodeDTO;
import it.nexus.domain.dto.AtecoCodeFormDTO;
import it.nexus.domain.dto.LegalRepresentativeDTO;
import it.nexus.domain.dto.LegalRepresentativeFormDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.CompanyAtecoCodeRepository;
import it.nexus.repository.CompanyLegalRepresentativeRepository;
import it.nexus.repository.CompanyRepository;
import it.nexus.services.AuditService;
import it.nexus.services.CompanyLegalDataService;
import it.nexus.web.errors.ConflictException;
import it.nexus.web.errors.FieldValidationException;
import it.nexus.web.errors.NotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CompanyLegalDataServiceImpl implements CompanyLegalDataService {

    private final CompanyRepository companyRepository;
    private final CompanyLegalRepresentativeRepository legalRepresentativeRepository;
    private final CompanyAtecoCodeRepository atecoCodeRepository;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public Optional<LegalRepresentativeDTO> legalRepresentative(String companyId) {
        return legalRepresentativeRepository.findByCompanyId(find(companyId).getId())
                .map(CompanyLegalDataServiceImpl::toDto);
    }

    @Override
    public LegalRepresentativeDTO saveLegalRepresentative(String companyId, LegalRepresentativeFormDTO dto) {
        Company company = find(companyId);
        Optional<CompanyLegalRepresentative> existing = legalRepresentativeRepository.findByCompanyId(company.getId());
        if (existing.isPresent() != (dto.version() != null)
                || existing.filter(r -> r.getVersion() != dto.version()).isPresent()) {
            throw new ConflictException(
                    "Il legale rappresentante è stato modificato nel frattempo: ricarica la pagina e riprova");
        }
        CompanyLegalRepresentative rep = existing.orElseGet(() -> new CompanyLegalRepresentative(company));
        String fiscalCode = blankToNull(dto.fiscalCode());
        fiscalCode = fiscalCode == null ? null : fiscalCode.toUpperCase(Locale.ROOT);
        // dati personali: nell'audit solo quali campi sono cambiati, senza i valori
        AuditChanges changes = AuditChanges.of()
                .redactedIfChanged("firstName", rep.getFirstName(), dto.firstName().strip())
                .redactedIfChanged("lastName", rep.getLastName(), dto.lastName().strip())
                .redactedIfChanged("fiscalCode", rep.getFiscalCode(), fiscalCode)
                .redactedIfChanged("birthDate", rep.getBirthDate(), dto.birthDate())
                .redactedIfChanged("birthPlace", rep.getBirthPlace(), blankToNull(dto.birthPlace()))
                .redactedIfChanged("residence", rep.getResidence(), blankToNull(dto.residence()));
        rep.setFirstName(dto.firstName().strip());
        rep.setLastName(dto.lastName().strip());
        rep.setFiscalCode(fiscalCode);
        rep.setBirthDate(dto.birthDate());
        rep.setBirthPlace(blankToNull(dto.birthPlace()));
        rep.setResidence(blankToNull(dto.residence()));
        CompanyLegalRepresentative saved = legalRepresentativeRepository.saveAndFlush(rep);
        if (!changes.asMap().isEmpty()) {
            auditService.record(AuditEntityType.COMPANY, company.getId(), "LEGAL_REPRESENTATIVE", changes, null);
        }
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AtecoCodeDTO> atecoCodes(String companyId) {
        return atecoCodeRepository.findByCompanyIdOrderByCodeAsc(find(companyId).getId()).stream()
                .map(CompanyLegalDataServiceImpl::toDto)
                .toList();
    }

    @Override
    public AtecoCodeDTO addAtecoCode(String companyId, AtecoCodeFormDTO dto) {
        Company company = find(companyId);
        String code = dto.code().strip();
        if (atecoCodeRepository.findByCompanyIdAndCode(company.getId(), code).isPresent()) {
            throw new FieldValidationException("code", "L'azienda ha già il codice %s".formatted(code));
        }
        CompanyAtecoCode saved = atecoCodeRepository.saveAndFlush(
                new CompanyAtecoCode(company, code, blankToNull(dto.description())));
        auditService.record(AuditEntityType.COMPANY, company.getId(), "ATECO_ADD",
                AuditChanges.of().value("atecoCode", null, code), null);
        return toDto(saved);
    }

    @Override
    public void removeAtecoCode(String companyId, String atecoCodeId) {
        Company company = find(companyId);
        CompanyAtecoCode ateco = Optional.ofNullable(TsidMapper.toInternal(atecoCodeId))
                .flatMap(atecoCodeRepository::findById)
                .filter(a -> a.getCompany().getId().equals(company.getId()))
                .orElseThrow(() -> new NotFoundException("Codice ATECO non trovato"));
        atecoCodeRepository.delete(ateco);
        auditService.record(AuditEntityType.COMPANY, company.getId(), "ATECO_REMOVE",
                AuditChanges.of().value("atecoCode", ateco.getCode(), null), null);
    }

    private Company find(String companyId) {
        return Optional.ofNullable(TsidMapper.toInternal(companyId))
                .flatMap(companyRepository::findById)
                .orElseThrow(() -> new NotFoundException("Azienda non trovata"));
    }

    private static LegalRepresentativeDTO toDto(CompanyLegalRepresentative r) {
        return new LegalRepresentativeDTO(r.getFirstName(), r.getLastName(), r.getFiscalCode(), r.getBirthDate(),
                r.getBirthPlace(), r.getResidence(), r.getVersion());
    }

    private static AtecoCodeDTO toDto(CompanyAtecoCode a) {
        return new AtecoCodeDTO(TsidMapper.toExternal(a.getId()), a.getCode(), a.getDescription());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
