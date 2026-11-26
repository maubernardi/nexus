package it.nexus.services.impl;

import java.time.ZoneId;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.ClosureReport;
import it.nexus.domain.dto.CompanyReviewDTO;
import it.nexus.domain.dto.CompanyReviewsDTO;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.ClosureReportRepository;
import it.nexus.repository.CompanyRepository;
import it.nexus.services.CompanyReviewService;
import it.nexus.services.UserDisplayNameService;
import it.nexus.web.errors.BadRequestException;
import it.nexus.web.errors.NotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompanyReviewServiceImpl implements CompanyReviewService {

    private static final ZoneId ROME = ZoneId.of("Europe/Rome");

    private final ClosureReportRepository reports;
    private final CompanyRepository companies;
    private final UserDisplayNameService userDisplayNameService;

    @Override
    public CompanyReviewsDTO reviews(String companyId, int page) {
        if (page < 0) {
            throw new BadRequestException("Pagina non valida: %d", page);
        }
        Long id = TsidMapper.toInternal(companyId);
        if (id == null || !companies.existsById(id)) {
            throw new NotFoundException("Azienda non trovata");
        }
        var result = reports.findCompanyReviews(id, PageRequest.of(page, PAGE_SIZE));
        Map<String, String> names = userDisplayNameService.displayNames(
                result.getContent().stream().map(ClosureReport::getCreatedBy).distinct().toList());
        List<CompanyReviewDTO> reviews = result.getContent().stream().map(r -> new CompanyReviewDTO(
                // i report con la valutazione hanno sempre la fine effettiva; per prudenza, la data dell'invio
                r.getActualEndDate() != null ? r.getActualEndDate() : r.getCreatedAt().atZone(ROME).toLocalDate(),
                r.companyReviewAverage(),
                r.getCompanyReviewNote(),
                names.getOrDefault(r.getCreatedBy(), r.getCreatedBy())))
                .toList();
        Summary summary = summaries(List.of(id)).get(id);
        return new CompanyReviewsDTO(summary == null ? null : summary.average(), reviews, result.getTotalElements(),
                page, PAGE_SIZE);
    }

    @Override
    public Map<Long, Summary> summaries(Collection<Long> companyIds) {
        Map<Long, Summary> summaries = new HashMap<>();
        if (companyIds.isEmpty()) {
            return summaries;
        }
        for (Object[] row : reports.companyReviewSummaries(companyIds)) {
            summaries.put((Long) row[0], new Summary(((Number) row[1]).doubleValue(), ((Number) row[2]).longValue()));
        }
        return summaries;
    }
}
