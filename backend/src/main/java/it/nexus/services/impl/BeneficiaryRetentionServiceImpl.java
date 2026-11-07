package it.nexus.services.impl;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.config.ApplicationProperties;
import it.nexus.domain.AppUser;
import it.nexus.domain.Beneficiary;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.BeneficiaryRetentionItemDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.RetentionStatus;
import it.nexus.domain.enumeration.Role;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.BeneficiaryRepository;
import it.nexus.repository.InternshipRepository;
import it.nexus.services.AuditService;
import it.nexus.services.BeneficiaryRetentionService;
import it.nexus.services.CurrentAppUserService;
import it.nexus.web.errors.BadRequestException;
import it.nexus.web.errors.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BeneficiaryRetentionServiceImpl implements BeneficiaryRetentionService {

    private static final ZoneId ROME = ZoneId.of("Europe/Rome");

    private final BeneficiaryRepository beneficiaryRepository;
    private final InternshipRepository internshipRepository;
    private final CurrentAppUserService currentAppUserService;
    private final AuditService auditService;
    private final ApplicationProperties applicationProperties;

    @Override
    public int review(Instant now) {
        // prima chi ha avuto attività dopo il segno: i 2 anni ripartono
        List<Long> active = beneficiaryRepository.findIdsActiveAfterDue();
        if (!active.isEmpty()) {
            beneficiaryRepository.setAnonymizationDue(active, null);
            active.forEach(id -> auditService.record(AuditEntityType.BENEFICIARY, id, "ANONYMIZATION_CANCELLED",
                    AuditChanges.of().value("retention", RetentionStatus.TO_ANONYMIZE.name(), null), null));
        }
        Instant threshold = now.atZone(ROME).minus(applicationProperties.getRetention().getPeriod()).toInstant();
        List<Long> inactive = beneficiaryRepository.findIdsInactiveSince(threshold);
        if (!inactive.isEmpty()) {
            beneficiaryRepository.setAnonymizationDue(inactive, now);
            inactive.forEach(id -> auditService.record(AuditEntityType.BENEFICIARY, id, "ANONYMIZATION_DUE",
                    AuditChanges.of().value("retention", null, RetentionStatus.TO_ANONYMIZE.name()), null));
        }
        if (active.size() + inactive.size() > 0) {
            log.info("Conservazione dei dati: {} beneficiari da anonimizzare, {} non più", inactive.size(), active.size());
        }
        return inactive.size();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiaryRetentionItemDTO> listAll(boolean onlyToAnonymize) {
        return beneficiaryRepository.findAllByOrderByLastNameAscFirstNameAsc().stream()
                .filter(b -> !onlyToAnonymize || b.getRetention() == RetentionStatus.TO_ANONYMIZE)
                .map(b -> new BeneficiaryRetentionItemDTO(TsidMapper.toExternal(b.getId()), b.getFirstName(),
                        b.getLastName(), b.getBirthYear(), b.getResidenceZone().getName(),
                        b.getOwnerTutor().getFirstName() + " " + b.getOwnerTutor().getLastName(), b.getRetention(),
                        b.getAnonymizationDueSince(), b.getKeptReason()))
                .toList();
    }

    @Override
    public void anonymize(String id) {
        Beneficiary beneficiary = toDecide(id);
        beneficiary.anonymize(Instant.now());
        internshipRepository.findByBeneficiaryId(beneficiary.getId()).forEach(i -> i.anonymize());
        beneficiaryRepository.saveAndFlush(beneficiary);
        // dati personali: nell'audit solo che sono stati cancellati, mai i valori (GDPR)
        auditService.record(AuditEntityType.BENEFICIARY, beneficiary.getId(), "ANONYMIZE", AuditChanges.of()
                .value("retention", RetentionStatus.TO_ANONYMIZE.name(), RetentionStatus.ANONYMIZED.name())
                .redacted("firstName").redacted("lastName").redacted("constraints").redacted("internships"), null);
        log.info("Beneficiario {} anonimizzato", beneficiary.getId());
    }

    @Override
    public void keep(String id, String reason) {
        Beneficiary beneficiary = toDecide(id);
        String text = reason.strip();
        beneficiary.keep(text);
        beneficiaryRepository.saveAndFlush(beneficiary);
        auditService.record(AuditEntityType.BENEFICIARY, beneficiary.getId(), "KEEP", AuditChanges.of()
                .value("retention", RetentionStatus.TO_ANONYMIZE.name(), RetentionStatus.KEPT.name()), text);
        log.info("Dati del beneficiario {} tenuti per sempre", beneficiary.getId());
    }

    /** Beneficiario da anonimizzare, su cui decide l'ADMIN o il suo Tutor. */
    private Beneficiary toDecide(String id) {
        Beneficiary beneficiary = Optional.ofNullable(TsidMapper.toInternal(id)).flatMap(beneficiaryRepository::findById)
                .orElseThrow(() -> new NotFoundException("Beneficiario non trovato"));
        AppUser me = currentAppUserService.getCurrentAppUser();
        if (me.getRole() != Role.ADMIN && !beneficiary.getOwnerTutor().getId().equals(me.getId())) {
            throw new AccessDeniedException("Decide l'ADMIN o il Tutor del beneficiario");
        }
        if (beneficiary.getRetention() != RetentionStatus.TO_ANONYMIZE) {
            throw new BadRequestException("Il beneficiario non è da anonimizzare");
        }
        return beneficiary;
    }
}
