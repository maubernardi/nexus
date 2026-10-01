package it.nexus.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.dto.ReferenceItemDTO;
import it.nexus.mapper.ReferenceMapper;
import it.nexus.repository.JobCategoryRepository;
import it.nexus.repository.ZoneRepository;
import it.nexus.services.ReferenceDataService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReferenceDataServiceImpl implements ReferenceDataService {

    private final ZoneRepository zoneRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final ReferenceMapper mapper;

    @Override
    public List<ReferenceItemDTO> activeZones() {
        return zoneRepository.findByActiveTrueOrderByNameAsc().stream().map(mapper::toDto).toList();
    }

    @Override
    public List<ReferenceItemDTO> activeJobCategories() {
        return jobCategoryRepository.findByActiveTrueOrderByNameAsc().stream().map(mapper::toDto).toList();
    }
}
