package it.nexus.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import it.nexus.domain.Beneficiary;
import it.nexus.domain.InternshipType;
import it.nexus.domain.Project;
import it.nexus.domain.dto.ReferenceItemDTO;
import it.nexus.mapper.ReferenceMapper;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.InternshipTypeRepository;
import it.nexus.web.errors.FieldValidationException;
import lombok.RequiredArgsConstructor;

/**
 * Regole del tipo di tirocinio, in un posto solo (US-115, US-206, US-605): per la creazione della segnalazione, la
 * correzione del Tutor e la scheda. Errori sul campo {@code internshipTypeId}.
 */
@Component
@RequiredArgsConstructor
public class InternshipTypeRules {

    public static final String FIELD = "internshipTypeId";

    /** Come chiamare i dati mancanti per l'inclusione nel messaggio d'errore (US-206). */
    private static final Map<String, String> INCLUSION_LABELS = Map.of(
            "careAgency", "l'ente che lo ha in carico",
            "caseManagerName", "il nome del case manager",
            "caseManagerEmail", "la mail del case manager",
            "caseManagerPhone", "il telefono del case manager");

    private final InternshipTypeRepository internshipTypeRepository;
    private final ReferenceMapper referenceMapper;

    /**
     * Il tipo scelto: attivo e ammesso dal progetto, oppure quello già scelto anche se nel frattempo disattivato o non più
     * ammesso; «Di inclusione» solo con ente e case manager del beneficiario completi. Vuoto = nessuno.
     */
    public InternshipType choose(String id, InternshipType current, Project project, Beneficiary beneficiary) {
        if (id == null || id.isBlank()) {
            return null;
        }
        InternshipType type = Optional.ofNullable(TsidMapper.toInternal(id.strip()))
                .flatMap(internshipTypeRepository::findById)
                .filter(t -> t.isActive() || t.equals(current))
                .orElseThrow(() -> new FieldValidationException(FIELD, "Tipo di tirocinio non valido"));
        if (!type.equals(current) && !allowed(project, type)) {
            throw new FieldValidationException(FIELD,
                    "Il tipo «%s» non è ammesso dal progetto %s".formatted(type.getName(), project.getName()));
        }
        if (type.isInclusion() && !type.equals(current)) {
            List<String> missing = inclusionMissing(beneficiary);
            if (!missing.isEmpty()) {
                throw new FieldValidationException(FIELD,
                        "Per un tirocinio di inclusione completa nella scheda del beneficiario: "
                                + missing.stream().map(INCLUSION_LABELS::get).collect(Collectors.joining(", ")));
            }
        }
        return type;
    }

    /** I tipi che il Tutor può scegliere: attivi e ammessi dal progetto, più quello già scelto. */
    public List<ReferenceItemDTO> choosable(Project project, InternshipType current) {
        List<InternshipType> types = new ArrayList<>(internshipTypeRepository.findByActiveTrueOrderByNameAsc().stream()
                .filter(t -> allowed(project, t))
                .toList());
        if (current != null && !types.contains(current)) {
            types.add(current);
        }
        return types.stream().map(referenceMapper::toDto).toList();
    }

    /** Cosa manca al beneficiario per un tirocinio di inclusione (US-206), nell'ordine del modulo. */
    public static List<String> inclusionMissing(Beneficiary b) {
        List<String> missing = new ArrayList<>();
        if (b.getCareAgency() == null) {
            missing.add("careAgency");
        }
        if (b.getCaseManagerName() == null) {
            missing.add("caseManagerName");
        }
        if (b.getCaseManagerEmail() == null) {
            missing.add("caseManagerEmail");
        }
        if (b.getCaseManagerPhone() == null) {
            missing.add("caseManagerPhone");
        }
        return missing;
    }

    /** Il tipo vale per il progetto (US-309: correzione del progetto della segnalazione). */
    public static boolean allowedFor(Project project, InternshipType type) {
        return allowed(project, type);
    }

    /** Un progetto senza tipi indicati li ammette tutti (fino al censimento dei progetti). */
    private static boolean allowed(Project project, InternshipType type) {
        return project.getAllowedInternshipTypes().isEmpty() || project.getAllowedInternshipTypes().contains(type);
    }
}
