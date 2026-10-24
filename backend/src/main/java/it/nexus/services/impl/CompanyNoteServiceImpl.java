package it.nexus.services.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.AppUser;
import it.nexus.domain.Company;
import it.nexus.domain.CompanyNote;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.CompanyNoteDTO;
import it.nexus.domain.dto.CompanyNoteFormDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.CompanyNoteRepository;
import it.nexus.repository.CompanyRepository;
import it.nexus.services.AuditService;
import it.nexus.services.CompanyNoteService;
import it.nexus.services.CurrentAppUserService;
import it.nexus.web.errors.NotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CompanyNoteServiceImpl implements CompanyNoteService {

    private final CompanyNoteRepository noteRepository;
    private final CompanyRepository companyRepository;
    private final AppUserRepository appUserRepository;
    private final AuditService auditService;
    private final CurrentAppUserService currentAppUserService;

    @Override
    @Transactional(readOnly = true)
    public List<CompanyNoteDTO> list(String companyId) {
        List<CompanyNote> notes = noteRepository.findByCompanyIdOrderByCreatedAtDescIdDesc(find(companyId).getId());
        Map<String, String> names = new HashMap<>();
        appUserRepository.findByUsernameIn(new HashSet<>(notes.stream().map(CompanyNote::getCreatedBy).toList()))
                .forEach(u -> names.put(u.getUsername(), fullName(u)));
        return notes.stream()
                .map(n -> toDto(n, names.getOrDefault(n.getCreatedBy(), "Sistema")))
                .toList();
    }

    @Override
    public CompanyNoteDTO add(String companyId, CompanyNoteFormDTO dto) {
        Company company = find(companyId);
        CompanyNote saved = noteRepository.save(new CompanyNote(company, dto.text().strip()));
        // il testo può contenere dati personali: nell'audit solo l'azione (come EN-4)
        auditService.record(AuditEntityType.COMPANY, company.getId(), "NOTE_ADD", AuditChanges.none(), null);
        return toDto(saved, fullName(currentAppUserService.getCurrentAppUser()));
    }

    private Company find(String companyId) {
        return Optional.ofNullable(TsidMapper.toInternal(companyId))
                .flatMap(companyRepository::findById)
                .orElseThrow(() -> new NotFoundException("Azienda non trovata"));
    }

    private static CompanyNoteDTO toDto(CompanyNote note, String authorName) {
        return new CompanyNoteDTO(TsidMapper.toExternal(note.getId()), note.getText(), note.getCreatedAt(), authorName);
    }

    private static String fullName(AppUser user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}
