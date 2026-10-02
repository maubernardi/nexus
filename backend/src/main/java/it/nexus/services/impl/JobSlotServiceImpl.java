package it.nexus.services.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.AbstractTsidEntity;
import it.nexus.domain.Company;
import it.nexus.domain.JobCategory;
import it.nexus.domain.JobSlot;
import it.nexus.domain.Zone;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.JobSlotDTO;
import it.nexus.domain.dto.JobSlotFormDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.JobSlotStatus;
import it.nexus.mapper.JobSlotMapper;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.CompanyRepository;
import it.nexus.repository.JobCategoryRepository;
import it.nexus.repository.JobSlotRepository;
import it.nexus.repository.ZoneRepository;
import it.nexus.services.AuditService;
import it.nexus.services.JobSlotService;
import it.nexus.web.errors.BadRequestException;
import it.nexus.web.errors.ConflictException;
import it.nexus.web.errors.FieldValidationException;
import it.nexus.web.errors.NotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class JobSlotServiceImpl implements JobSlotService {

    private final JobSlotRepository repository;
    private final CompanyRepository companyRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final ZoneRepository zoneRepository;
    private final AuditService auditService;
    private final JobSlotMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<JobSlotDTO> listByCompany(String companyId) {
        Company company = company(companyId);
        return repository.findByCompany(company.getId()).stream().map(mapper::toDto).toList();
    }

    @Override
    public JobSlotDTO create(String companyId, JobSlotFormDTO dto) {
        Company company = company(companyId);
        if (!company.isActive()) {
            throw new ConflictException("L'azienda è disattivata: riattivala per aggiungere mansioni");
        }
        JobSlot slot = new JobSlot(company, activeCategory(dto.jobCategoryId(), null), activeZone(dto.zoneId(), null),
                dto.title().strip());
        slot.setDescription(blankToNull(dto.description()));
        JobSlot saved = repository.saveAndFlush(slot);
        auditService.record(AuditEntityType.JOB_SLOT, saved.getId(), "CREATE", AuditChanges.of()
                .value("companyId", null, external(company))
                .value("title", null, saved.getTitle())
                .value("jobCategoryId", null, external(saved.getJobCategory()))
                .value("zoneId", null, external(saved.getZone()))
                .value("description", null, saved.getDescription()), null);
        return mapper.toDto(saved);
    }

    @Override
    public JobSlotDTO update(String id, JobSlotFormDTO dto) {
        if (dto.version() == null) {
            throw new BadRequestException("Versione mancante");
        }
        JobSlot slot = find(id);
        requireVersion(slot, dto.version());
        // una voce ormai disattivata resta valida se non cambia
        JobCategory category = activeCategory(dto.jobCategoryId(), slot.getJobCategory());
        Zone zone = activeZone(dto.zoneId(), slot.getZone());
        AuditChanges changes = AuditChanges.of()
                .value("title", slot.getTitle(), dto.title().strip())
                .value("jobCategoryId", external(slot.getJobCategory()), external(category))
                .value("zoneId", external(slot.getZone()), external(zone))
                .value("description", slot.getDescription(), blankToNull(dto.description()));
        slot.setTitle(dto.title().strip());
        slot.setJobCategory(category);
        slot.setZone(zone);
        slot.setDescription(blankToNull(dto.description()));
        JobSlot saved = repository.saveAndFlush(slot);
        if (!changes.asMap().isEmpty()) {
            auditService.record(AuditEntityType.JOB_SLOT, saved.getId(), "UPDATE", changes, null);
        }
        return mapper.toDto(saved);
    }

    @Override
    public JobSlotDTO setActive(String id, long expectedVersion, boolean active) {
        JobSlot slot = find(id);
        requireVersion(slot, expectedVersion);
        if (slot.isActive() == active) {
            return mapper.toDto(slot);
        }
        if (!active && slot.getStatus() == JobSlotStatus.BLOCCATA) {
            throw new ConflictException("La mansione è bloccata dalla segnalazione n. %d: non si può ritirare",
                    slot.getBlockedByTicket().getNumber());
        }
        if (active && !slot.getCompany().isActive()) {
            throw new ConflictException("L'azienda è disattivata: riattivala prima di rimettere a disposizione la mansione");
        }
        slot.setActive(active);
        JobSlot saved = repository.saveAndFlush(slot);
        auditService.record(AuditEntityType.JOB_SLOT, saved.getId(), active ? "ACTIVATE" : "DEACTIVATE",
                AuditChanges.of().value("active", !active, active), null);
        return mapper.toDto(saved);
    }

    private Company company(String companyId) {
        return Optional.ofNullable(TsidMapper.toInternal(companyId))
                .flatMap(companyRepository::findById)
                .orElseThrow(() -> new NotFoundException("Azienda non trovata"));
    }

    private JobSlot find(String id) {
        return Optional.ofNullable(TsidMapper.toInternal(id))
                .flatMap(repository::findById)
                .orElseThrow(() -> new NotFoundException("Mansione non trovata"));
    }

    private JobCategory activeCategory(String id, JobCategory current) {
        return Optional.ofNullable(TsidMapper.toInternal(id))
                .flatMap(jobCategoryRepository::findById)
                .filter(c -> c.isActive() || c.equals(current))
                .orElseThrow(() -> new FieldValidationException("jobCategoryId", "Tipologia non valida"));
    }

    private Zone activeZone(String id, Zone current) {
        return Optional.ofNullable(TsidMapper.toInternal(id))
                .flatMap(zoneRepository::findById)
                .filter(z -> z.isActive() || z.equals(current))
                .orElseThrow(() -> new FieldValidationException("zoneId", "Zona non valida"));
    }

    private static void requireVersion(JobSlot slot, long expectedVersion) {
        if (slot.getVersion() != expectedVersion) {
            throw new ConflictException("La mansione è stata modificata nel frattempo: ricarica la pagina e riprova");
        }
    }

    private static String external(AbstractTsidEntity entity) {
        return entity == null ? null : TsidMapper.toExternal(entity.getId());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
