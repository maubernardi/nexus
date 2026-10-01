package it.nexus.services.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.AppUser;
import it.nexus.domain.Beneficiary;
import it.nexus.domain.BeneficiaryLanguage;
import it.nexus.domain.Ticket;
import it.nexus.domain.Zone;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.BeneficiaryCreateDTO;
import it.nexus.domain.dto.BeneficiaryDTO;
import it.nexus.domain.dto.BeneficiaryLanguageDTO;
import it.nexus.domain.dto.BeneficiarySummaryDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.LicenseType;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.mapper.BeneficiaryMapper;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.BeneficiaryRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.ZoneRepository;
import it.nexus.services.AuditService;
import it.nexus.services.BeneficiaryService;
import it.nexus.services.CurrentAppUserService;
import it.nexus.web.errors.FieldValidationException;
import it.nexus.web.errors.NotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class BeneficiaryServiceImpl implements BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final TicketRepository ticketRepository;
    private final AuditService auditService;
    private final ZoneRepository zoneRepository;
    private final CurrentAppUserService currentAppUserService;
    private final BeneficiaryMapper mapper;

    @Override
    public BeneficiaryDTO register(BeneficiaryCreateDTO dto) {
        Zone zone = activeZone(dto.residenceZoneId());
        Set<String> seen = new HashSet<>();
        for (BeneficiaryLanguageDTO language : dto.languages()) {
            if (!seen.add(language.language())) {
                throw new FieldValidationException("languages", "Ogni lingua può comparire una sola volta");
            }
        }

        Beneficiary beneficiary = new Beneficiary(currentAppUserService.getCurrentAppUser());
        beneficiary.setFirstName(dto.firstName().strip());
        beneficiary.setLastName(dto.lastName().strip());
        beneficiary.setBirthYear(dto.birthYear());
        beneficiary.setGender(dto.gender());
        beneficiary.setNationality(dto.nationality());
        beneficiary.setCitizenship(dto.citizenship());
        beneficiary.setResidenceZone(zone);
        beneficiary.setLicenseTypes(dto.licenseTypes().stream().sorted().toArray(LicenseType[]::new));
        beneficiary.setHasVehicle(dto.hasVehicle());
        beneficiary.setTransportMode(dto.transportMode());
        beneficiary.setHasLaw68(dto.hasLaw68());
        beneficiary.setEducationLevel(dto.educationLevel());
        beneficiary.setConstraints(blankToNull(dto.constraints()));
        dto.languages().forEach(l -> beneficiary.addLanguage(new BeneficiaryLanguage(l.language(), l.level())));

        Beneficiary saved = beneficiaryRepository.save(beneficiary);
        // dati personali: nell'audit solo l'azione, mai i valori (GDPR)
        auditService.record(AuditEntityType.BENEFICIARY, saved.getId(), "CREATE", AuditChanges.none(), null);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiarySummaryDTO> listMine() {
        AppUser me = currentAppUserService.getCurrentAppUser();
        List<Beneficiary> mine = beneficiaryRepository.findByOwnerTutorIdOrderByLastNameAscFirstNameAsc(me.getId());
        Map<Long, Long> openTickets = ticketRepository
                .findByBeneficiaryIdInAndStatusNot(mine.stream().map(Beneficiary::getId).toList(), TicketStatus.CLOSED)
                .stream()
                .collect(Collectors.toMap(t -> t.getBeneficiary().getId(), Ticket::getNumber, (a, b) -> a));
        return mine.stream()
                .map(b -> mapper.toSummary(b).withOpenTicketNumber(openTickets.get(b.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BeneficiaryDTO getById(String id) {
        // stessa risposta per inesistente e non autorizzato: non si rivela l'esistenza del beneficiario
        return Optional.ofNullable(TsidMapper.toInternal(id))
                .flatMap(beneficiaryRepository::findById)
                .filter(this::canSee)
                .map(mapper::toDto)
                .orElseThrow(() -> new NotFoundException("Beneficiario non trovato"));
    }

    private boolean canSee(Beneficiary beneficiary) {
        return currentAppUserService.hasGlobalVisibility()
                || beneficiary.getOwnerTutor().getId().equals(currentAppUserService.getCurrentAppUser().getId());
    }

    private Zone activeZone(String zoneId) {
        return Optional.ofNullable(TsidMapper.toInternal(zoneId))
                .flatMap(zoneRepository::findById)
                .filter(Zone::isActive)
                .orElseThrow(() -> new FieldValidationException("residenceZoneId", "Zona non valida"));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
