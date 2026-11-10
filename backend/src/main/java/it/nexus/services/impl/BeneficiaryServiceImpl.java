package it.nexus.services.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.AppUser;
import it.nexus.domain.Beneficiary;
import it.nexus.domain.BeneficiaryLanguage;
import it.nexus.domain.CareAgency;
import it.nexus.domain.Internship;
import it.nexus.domain.StoredFile;
import it.nexus.domain.StoredFileContent;
import it.nexus.domain.Ticket;
import it.nexus.domain.Zone;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.BeneficiaryDTO;
import it.nexus.domain.dto.BeneficiaryFormDTO;
import it.nexus.domain.dto.BeneficiaryLanguageDTO;
import it.nexus.domain.dto.BeneficiarySummaryDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.LanguageLevel;
import it.nexus.domain.enumeration.LicenseType;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.mapper.BeneficiaryMapper;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.BeneficiaryRepository;
import it.nexus.repository.CareAgencyRepository;
import it.nexus.repository.InternshipRepository;
import it.nexus.repository.StoredFileContentRepository;
import it.nexus.repository.StoredFileRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.UserProjectRepository;
import it.nexus.repository.ZoneRepository;
import it.nexus.services.AuditService;
import it.nexus.services.BeneficiaryService;
import it.nexus.services.CurrentAppUserService;
import it.nexus.services.DownloadedFile;
import it.nexus.web.errors.BadRequestException;
import it.nexus.web.errors.FieldValidationException;
import it.nexus.web.errors.NotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class BeneficiaryServiceImpl implements BeneficiaryService {

    /** CV: solo PDF fino a 5 MB (US-205), come il vincolo ck_stored_file_content_size. */
    static final int CV_MAX_BYTES = 5 * 1024 * 1024;
    private static final String PDF = "application/pdf";
    private static final byte[] PDF_MAGIC = "%PDF-".getBytes(StandardCharsets.US_ASCII);

    private final BeneficiaryRepository beneficiaryRepository;
    private final TicketRepository ticketRepository;
    private final UserProjectRepository userProjectRepository;
    private final AuditService auditService;
    private final ZoneRepository zoneRepository;
    private final CurrentAppUserService currentAppUserService;
    private final BeneficiaryMapper mapper;
    private final StoredFileRepository storedFileRepository;
    private final StoredFileContentRepository storedFileContentRepository;
    private final InternshipRepository internshipRepository;
    private final CareAgencyRepository careAgencyRepository;

    @Override
    public BeneficiaryDTO register(BeneficiaryFormDTO dto) {
        Zone zone = activeZone(dto.residenceZoneId());
        requireDistinctLanguages(dto);

        Beneficiary beneficiary = new Beneficiary(currentAppUserService.getCurrentAppUser());
        apply(beneficiary, dto, zone, careAgency(dto.careAgencyId(), null));

        Beneficiary saved = beneficiaryRepository.save(beneficiary);
        // dati personali: nell'audit solo l'azione, mai i valori (GDPR)
        auditService.record(AuditEntityType.BENEFICIARY, saved.getId(), "CREATE", AuditChanges.none(), null);
        return toDto(saved, true);
    }

    @Override
    public BeneficiaryDTO update(String id, BeneficiaryFormDTO dto) {
        Beneficiary beneficiary = findVisible(id);
        if (!isOwner(beneficiary)) {
            throw new AccessDeniedException("Solo il Tutor proprietario può modificare il beneficiario");
        }
        if (beneficiary.getAnonymizedAt() != null) {
            throw new BadRequestException("Il beneficiario è anonimizzato: i suoi dati non si modificano più");
        }
        // nuova attività: non è più da anonimizzare (US-1303)
        beneficiary.clearAnonymizationDue();
        Zone zone = beneficiary.getResidenceZone().getId().equals(TsidMapper.toInternal(dto.residenceZoneId()))
                // la zona già assegnata resta valida anche se nel frattempo è stata disattivata
                ? beneficiary.getResidenceZone()
                : activeZone(dto.residenceZoneId());
        requireDistinctLanguages(dto);
        CareAgency agency = careAgency(dto.careAgencyId(), beneficiary.getCareAgency());
        requireCareRules(beneficiary, dto, agency);

        Map<String, Object> before = snapshot(beneficiary);
        apply(beneficiary, dto, zone, agency);
        Map<String, Object> after = snapshot(beneficiary);
        // dati personali: nell'audit solo quali campi sono cambiati, mai i valori (GDPR)
        AuditChanges changes = AuditChanges.of();
        before.forEach((field, value) -> changes.redactedIfChanged(field, value, after.get(field)));

        Beneficiary saved = beneficiaryRepository.saveAndFlush(beneficiary);
        if (!changes.asMap().isEmpty()) {
            auditService.record(AuditEntityType.BENEFICIARY, saved.getId(), "UPDATE", changes, null);
        }
        return toDto(saved, true);
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
        Beneficiary beneficiary = findVisible(id);
        // un beneficiario anonimizzato non si modifica più (US-1303)
        return toDto(beneficiary, isOwner(beneficiary) && beneficiary.getAnonymizedAt() == null);
    }

    /** Stessa risposta per inesistente e non autorizzato: non si rivela l'esistenza del beneficiario. */
    @Override
    public BeneficiaryDTO uploadCv(String id, String fileName, byte[] content) {
        Beneficiary beneficiary = ownForCv(id);
        if (content == null || content.length == 0) {
            throw new FieldValidationException("file", "Scegli il file del CV");
        }
        if (content.length > CV_MAX_BYTES) {
            throw new FieldValidationException("file", "Il CV supera i 5 MB");
        }
        // il tipo si riconosce dal contenuto, non dal nome o da quello che dichiara il browser
        if (!isPdf(content)) {
            throw new FieldValidationException("file", "Il CV deve essere un file PDF");
        }
        boolean replaced = beneficiary.getCvFile() != null;
        removeCvFile(beneficiary);
        StoredFile file = storedFileRepository.save(new StoredFile(cvName(fileName), PDF, content.length,
                sha256(content), "db:" + UUID.randomUUID()));
        storedFileContentRepository.save(new StoredFileContent(file, content));
        beneficiary.setCvFile(file);
        Beneficiary saved = beneficiaryRepository.saveAndFlush(beneficiary);
        // il nome del file può contenere il nome della persona: nell'audit solo la dimensione
        auditService.record(AuditEntityType.BENEFICIARY, saved.getId(), replaced ? "CV_REPLACED" : "CV_UPLOADED",
                AuditChanges.of().value("cvSizeBytes", null, content.length), null);
        return toDto(saved, true);
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadedFile downloadCv(String id) {
        Beneficiary beneficiary = findVisible(id);
        StoredFile file = Optional.ofNullable(beneficiary.getCvFile())
                .orElseThrow(() -> new NotFoundException("Il beneficiario non ha un CV"));
        byte[] content = storedFileContentRepository.findById(file.getId())
                .orElseThrow(() -> new NotFoundException("Il beneficiario non ha un CV")).getContent();
        return new DownloadedFile(file.getOriginalName(), file.getContentType(), content);
    }

    @Override
    public BeneficiaryDTO deleteCv(String id) {
        Beneficiary beneficiary = ownForCv(id);
        if (beneficiary.getCvFile() == null) {
            return toDto(beneficiary, true);
        }
        removeCvFile(beneficiary);
        Beneficiary saved = beneficiaryRepository.saveAndFlush(beneficiary);
        auditService.record(AuditEntityType.BENEFICIARY, saved.getId(), "CV_REMOVED", AuditChanges.none(), null);
        return toDto(saved, true);
    }

    /** Beneficiario del Tutor corrente, non anonimizzato: solo lui gestisce il CV (come la scheda, US-203). */
    private Beneficiary ownForCv(String id) {
        Beneficiary beneficiary = findVisible(id);
        if (!isOwner(beneficiary)) {
            throw new AccessDeniedException("Solo il Tutor proprietario gestisce il CV del beneficiario");
        }
        if (beneficiary.getAnonymizedAt() != null) {
            throw new BadRequestException("Il beneficiario è anonimizzato: non ha più un CV");
        }
        return beneficiary;
    }

    /** Toglie il CV dal beneficiario e cancella il file (il contenuto se ne va con il file). */
    private void removeCvFile(Beneficiary beneficiary) {
        StoredFile old = beneficiary.getCvFile();
        if (old != null) {
            beneficiary.setCvFile(null);
            beneficiaryRepository.flush();
            // prima il contenuto, che può essere ancora nella sessione, poi il file
            storedFileContentRepository.deleteById(old.getId());
            storedFileRepository.delete(old);
            storedFileRepository.flush();
        }
    }

    private static boolean isPdf(byte[] content) {
        return content.length >= PDF_MAGIC.length && Arrays.equals(Arrays.copyOf(content, PDF_MAGIC.length), PDF_MAGIC);
    }

    /** Nome del file come lo ha caricato il Tutor, senza percorso e senza caratteri di controllo. */
    private static String cvName(String fileName) {
        String name = fileName == null ? "" : fileName.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}\"]", "").strip();
        if (name.isEmpty()) {
            name = "CV.pdf";
        }
        if (!name.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            name += ".pdf";
        }
        return name.length() > 200 ? name.substring(name.length() - 200) : name;
    }

    private static String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Scheda con i blocchi dovuti al tirocinio aperto (US-206). */
    private BeneficiaryDTO toDto(Beneficiary beneficiary, boolean editable) {
        Optional<Internship> open = openInternship(beneficiary);
        return mapper.toDto(beneficiary, editable, open.isPresent(), open.map(BeneficiaryServiceImpl::isInclusion)
                .orElse(false));
    }

    /** Tirocinio aperto: la scheda del tirocinio di una segnalazione non ancora chiusa (PO, 09/11). */
    private Optional<Internship> openInternship(Beneficiary beneficiary) {
        if (beneficiary.getId() == null) {
            return Optional.empty();
        }
        return internshipRepository.findByBeneficiaryId(beneficiary.getId()).stream()
                .filter(i -> i.getTicket().getStatus() != TicketStatus.CLOSED)
                .findFirst();
    }

    private static boolean isInclusion(Internship internship) {
        return internship.getInternshipType() != null && internship.getInternshipType().isInclusion();
    }

    /**
     * Con un tirocinio aperto l'ente non cambia; con un tirocinio di inclusione aperto nome, mail e telefono del case
     * manager restano (US-206).
     */
    private void requireCareRules(Beneficiary beneficiary, BeneficiaryFormDTO dto, CareAgency agency) {
        Optional<Internship> open = openInternship(beneficiary);
        if (open.isEmpty()) {
            return;
        }
        if (!Objects.equals(agency, beneficiary.getCareAgency())) {
            throw new FieldValidationException("careAgencyId",
                    "L'ente non si può cambiare mentre il beneficiario ha un tirocinio aperto");
        }
        if (isInclusion(open.get())) {
            String required = "Obbligatorio durante il tirocinio di inclusione";
            if (blankToNull(dto.caseManagerName()) == null) {
                throw new FieldValidationException("caseManagerName", required);
            }
            if (blankToNull(dto.caseManagerEmail()) == null) {
                throw new FieldValidationException("caseManagerEmail", required);
            }
            if (blankToNull(dto.caseManagerPhone()) == null) {
                throw new FieldValidationException("caseManagerPhone", required);
            }
        }
    }

    /** Ente attivo del catalogo, oppure quello già indicato anche se nel frattempo disattivato. */
    private CareAgency careAgency(String id, CareAgency current) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return Optional.ofNullable(TsidMapper.toInternal(id))
                .flatMap(careAgencyRepository::findById)
                .filter(a -> a.isActive() || a.equals(current))
                .orElseThrow(() -> new FieldValidationException("careAgencyId", "Ente non valido"));
    }

    private Beneficiary findVisible(String id) {
        return Optional.ofNullable(TsidMapper.toInternal(id))
                .flatMap(beneficiaryRepository::findById)
                .filter(this::canSee)
                .orElseThrow(() -> new NotFoundException("Beneficiario non trovato"));
    }

    private boolean canSee(Beneficiary beneficiary) {
        if (currentAppUserService.hasGlobalVisibility() || isOwner(beneficiary)) {
            return true;
        }
        // Tutor dei progetti in cui il beneficiario ha segnalazioni: dentro il progetto i dati sono condivisi (Q11)
        List<Long> myProjects = userProjectRepository.findByIdUserId(currentAppUserService.getCurrentAppUser().getId())
                .stream()
                .map(up -> up.getId().getProjectId())
                .toList();
        return !myProjects.isEmpty() && ticketRepository.existsByBeneficiaryIdAndProjectIdIn(beneficiary.getId(), myProjects);
    }

    private boolean isOwner(Beneficiary beneficiary) {
        return beneficiary.getOwnerTutor().getId().equals(currentAppUserService.getCurrentAppUser().getId());
    }

    private static void requireDistinctLanguages(BeneficiaryFormDTO dto) {
        Set<String> seen = new HashSet<>();
        for (BeneficiaryLanguageDTO language : dto.languages()) {
            if (!seen.add(language.language())) {
                throw new FieldValidationException("languages", "Ogni lingua può comparire una sola volta");
            }
        }
    }

    private static void apply(Beneficiary beneficiary, BeneficiaryFormDTO dto, Zone zone, CareAgency agency) {
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
        applyLanguages(beneficiary, dto.languages());
        beneficiary.setEmail(blankToNull(dto.email()));
        beneficiary.setPhone(blankToNull(dto.phone()));
        beneficiary.setCareAgency(agency);
        beneficiary.setCaseManagerName(blankToNull(dto.caseManagerName()));
        beneficiary.setCaseManagerOffice(blankToNull(dto.caseManagerOffice()));
        beneficiary.setCaseManagerPhone(blankToNull(dto.caseManagerPhone()));
        beneficiary.setCaseManagerEmail(blankToNull(dto.caseManagerEmail()));
    }

    /**
     * Aggiorna le lingue sul posto: togliere e reinserire la stessa lingua violerebbe il vincolo di unicità, perché
     * Hibernate esegue gli inserimenti prima delle cancellazioni.
     */
    private static void applyLanguages(Beneficiary beneficiary, List<BeneficiaryLanguageDTO> wanted) {
        Map<String, LanguageLevel> levels = new LinkedHashMap<>();
        wanted.forEach(l -> levels.put(l.language(), l.level()));
        beneficiary.getLanguages().removeIf(l -> !levels.containsKey(l.getLanguage()));
        beneficiary.getLanguages().forEach(l -> l.setLevel(levels.remove(l.getLanguage())));
        levels.forEach((language, level) -> beneficiary.addLanguage(new BeneficiaryLanguage(language, level)));
    }

    /** Valori confrontabili dei campi, per sapere quali sono cambiati (non finiscono mai nell'audit). */
    private static Map<String, Object> snapshot(Beneficiary b) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("firstName", b.getFirstName());
        values.put("lastName", b.getLastName());
        values.put("birthYear", b.getBirthYear());
        values.put("gender", b.getGender());
        values.put("nationality", b.getNationality());
        values.put("citizenship", b.getCitizenship());
        values.put("residenceZone", b.getResidenceZone().getId());
        values.put("licenseTypes", List.of(b.getLicenseTypes()));
        values.put("hasVehicle", b.isHasVehicle());
        values.put("transportMode", b.getTransportMode());
        values.put("hasLaw68", b.isHasLaw68());
        values.put("educationLevel", b.getEducationLevel());
        values.put("constraints", b.getConstraints());
        values.put("languages", b.getLanguages().stream()
                .collect(Collectors.toMap(BeneficiaryLanguage::getLanguage, BeneficiaryLanguage::getLevel)));
        values.put("email", b.getEmail());
        values.put("phone", b.getPhone());
        values.put("careAgency", b.getCareAgency() == null ? null : b.getCareAgency().getId());
        values.put("caseManagerName", b.getCaseManagerName());
        values.put("caseManagerOffice", b.getCaseManagerOffice());
        values.put("caseManagerPhone", b.getCaseManagerPhone());
        values.put("caseManagerEmail", b.getCaseManagerEmail());
        return values;
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
