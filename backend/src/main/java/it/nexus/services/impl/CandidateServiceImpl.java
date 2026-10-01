package it.nexus.services.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.AppUser;
import it.nexus.domain.Candidate;
import it.nexus.domain.CandidateLanguage;
import it.nexus.domain.Zone;
import it.nexus.domain.dto.CandidateCreateDTO;
import it.nexus.domain.dto.CandidateDTO;
import it.nexus.domain.dto.CandidateLanguageDTO;
import it.nexus.domain.dto.CandidateSummaryDTO;
import it.nexus.domain.enumeration.LicenseType;
import it.nexus.mapper.CandidateMapper;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.CandidateRepository;
import it.nexus.repository.ZoneRepository;
import it.nexus.services.CandidateService;
import it.nexus.services.CurrentAppUserService;
import it.nexus.web.errors.FieldValidationException;
import it.nexus.web.errors.NotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CandidateServiceImpl implements CandidateService {

    private final CandidateRepository candidateRepository;
    private final ZoneRepository zoneRepository;
    private final CurrentAppUserService currentAppUserService;
    private final CandidateMapper mapper;

    @Override
    public CandidateDTO register(CandidateCreateDTO dto) {
        Zone zone = activeZone(dto.residenceZoneId());
        Set<String> seen = new HashSet<>();
        for (CandidateLanguageDTO language : dto.languages()) {
            if (!seen.add(language.language())) {
                throw new FieldValidationException("languages", "Ogni lingua può comparire una sola volta");
            }
        }

        Candidate candidate = new Candidate(currentAppUserService.getCurrentAppUser());
        candidate.setFirstName(dto.firstName().strip());
        candidate.setLastName(dto.lastName().strip());
        candidate.setBirthYear(dto.birthYear());
        candidate.setGender(dto.gender());
        candidate.setNationality(dto.nationality());
        candidate.setCitizenship(dto.citizenship());
        candidate.setResidenceZone(zone);
        candidate.setLicenseTypes(dto.licenseTypes().stream().sorted().toArray(LicenseType[]::new));
        candidate.setHasVehicle(dto.hasVehicle());
        candidate.setTransportMode(dto.transportMode());
        candidate.setHasLaw68(dto.hasLaw68());
        candidate.setEducationLevel(dto.educationLevel());
        candidate.setConstraints(blankToNull(dto.constraints()));
        dto.languages().forEach(l -> candidate.addLanguage(new CandidateLanguage(l.language(), l.level())));

        return mapper.toDto(candidateRepository.save(candidate));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidateSummaryDTO> listMine() {
        AppUser me = currentAppUserService.getCurrentAppUser();
        return candidateRepository.findByOwnerTutorIdOrderByLastNameAscFirstNameAsc(me.getId()).stream()
                .map(mapper::toSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CandidateDTO getById(String id) {
        // stessa risposta per inesistente e non autorizzato: non si rivela l'esistenza del candidato
        return Optional.ofNullable(TsidMapper.toInternal(id))
                .flatMap(candidateRepository::findById)
                .filter(this::canSee)
                .map(mapper::toDto)
                .orElseThrow(() -> new NotFoundException("Candidato non trovato"));
    }

    private boolean canSee(Candidate candidate) {
        return currentAppUserService.hasGlobalVisibility()
                || candidate.getOwnerTutor().getId().equals(currentAppUserService.getCurrentAppUser().getId());
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
