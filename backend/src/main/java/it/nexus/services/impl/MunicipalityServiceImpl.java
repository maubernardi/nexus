package it.nexus.services.impl;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.config.ApplicationProperties;
import it.nexus.config.TimeConfig;
import it.nexus.domain.Municipality;
import it.nexus.domain.MunicipalityImport;
import it.nexus.domain.Province;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.MunicipalityChangesDTO;
import it.nexus.domain.dto.MunicipalityDTO;
import it.nexus.domain.dto.MunicipalityImportPreviewDTO;
import it.nexus.domain.dto.MunicipalityStatusDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.mapper.MunicipalityMapper;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.MunicipalityImportRepository;
import it.nexus.repository.MunicipalityRepository;
import it.nexus.repository.ProvinceRepository;
import it.nexus.services.AuditService;
import it.nexus.services.MunicipalityService;
import it.nexus.services.UserDisplayNameService;
import it.nexus.services.municipalities.IstatMunicipalityFile;
import it.nexus.services.municipalities.IstatMunicipalityFile.Row;
import it.nexus.web.errors.BadRequestException;
import it.nexus.web.errors.FieldValidationException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class MunicipalityServiceImpl implements MunicipalityService {

    /** Esempi per gruppo nell'anteprima: bastano a capire che file è; i conteggi dicono il resto. */
    static final int EXAMPLES = 100;
    /** Il frontend (MunicipalityField) avvisa quando l'elenco arriva a questo limite: cambiarli insieme. */
    private static final int SEARCH_LIMIT = 20;
    private static final int HISTORY_LIMIT = 50;
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALIAN);

    private final ProvinceRepository provinces;
    private final MunicipalityRepository municipalities;
    private final MunicipalityImportRepository imports;
    private final AuditService auditService;
    private final UserDisplayNameService displayNames;
    private final ApplicationProperties properties;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public MunicipalityStatusDTO status() {
        MunicipalityStatusDTO.LastImport last = imports.findFirstByOrderByValidFromDesc()
                .map(i -> new MunicipalityStatusDTO.LastImport(i.getValidFrom(), i.getCreatedAt(),
                        displayNames.displayNames(Set.of(i.getCreatedBy())).get(i.getCreatedBy())))
                .orElse(null);
        return new MunicipalityStatusDTO(municipalities.countByValidToIsNull(), provinces.countByValidToIsNull(), last);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MunicipalityDTO> search(String text, boolean history) {
        String q = text == null ? "" : text.strip().toLowerCase(Locale.ITALIAN);
        if (q.length() < 2) {
            return List.of();
        }
        String escaped = q.replace("!", "!!").replace("%", "!%").replace("_", "!_");
        return municipalities.search(escaped, history, PageRequest.of(0, history ? HISTORY_LIMIT : SEARCH_LIMIT))
                .stream()
                .map(MunicipalityMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MunicipalityImportPreviewDTO preview(byte[] file) {
        return compare(read(file)).toDto();
    }

    @Override
    public MunicipalityImportPreviewDTO importFile(byte[] file, LocalDate validFrom) {
        if (validFrom == null) {
            throw new BadRequestException("Indica la data da cui vale l'elenco");
        }
        if (validFrom.isAfter(LocalDate.now(clock.withZone(TimeConfig.ZONE)))) {
            throw new BadRequestException("La data da cui vale l'elenco non può essere nel futuro");
        }
        imports.findFirstByOrderByValidFromDesc()
                .filter(last -> !validFrom.isAfter(last.getValidFrom()))
                .ifPresent(last -> {
                    throw new BadRequestException(
                            "La data deve essere successiva a quella dell'ultimo caricamento (%s)",
                            DAY.format(last.getValidFrom()));
                });
        Comparison comparison = compare(read(file));
        comparison.apply(validFrom);
        MunicipalityImportPreviewDTO result = comparison.toDto();
        MunicipalityImport saved = imports.save(new MunicipalityImport(validFrom, result.municipalities().added(),
                result.municipalities().removed(), result.municipalities().changed(), result.provinces().added(),
                result.provinces().removed(), result.provinces().changed()));
        auditService.record(AuditEntityType.MUNICIPALITY_IMPORT, saved.getId(), "IMPORT", AuditChanges.of()
                .value("validFrom", null, validFrom.toString())
                .value("municipalitiesAdded", null, result.municipalities().added())
                .value("municipalitiesRemoved", null, result.municipalities().removed())
                .value("municipalitiesChanged", null, result.municipalities().changed())
                .value("provincesAdded", null, result.provinces().added())
                .value("provincesRemoved", null, result.provinces().removed())
                .value("provincesChanged", null, result.provinces().changed()), null);
        return result;
    }

    private List<Row> read(byte[] file) {
        if (file == null || file.length == 0) {
            throw new BadRequestException("Scegli il file dell'ISTAT");
        }
        return IstatMunicipalityFile.read(file, properties.getMunicipalities().getMinRows());
    }

    private Comparison compare(List<Row> rows) {
        return new Comparison(rows, index(provinces.findByValidToIsNull(), Province::getCode),
                index(municipalities.findByValidToIsNull(), Municipality::getCadastralCode));
    }

    @Override
    public Municipality forAddress(String id, Municipality current, String field) {
        if (id == null || id.isBlank()) {
            return null;
        }
        Municipality chosen = Optional.ofNullable(TsidMapper.toInternal(id))
                .flatMap(municipalities::findById)
                .orElseThrow(() -> new FieldValidationException(field, "Comune non valido: sceglilo dall'elenco"));
        // per id: il comune già salvato arriva spesso come proxy di Hibernate (relazione LAZY)
        boolean saved = current != null && chosen.getId().equals(current.getId());
        if (!saved && chosen.getValidTo() != null) {
            throw new FieldValidationException(field,
                    "Il comune %s non è più valido: sceglilo di nuovo dall'elenco".formatted(chosen.getName()));
        }
        return chosen;
    }

    private static <T> Map<String, T> index(Collection<T> items, Function<T, String> key) {
        return items.stream().collect(Collectors.toMap(key, Function.identity()));
    }

    /** Il file confrontato con le versioni attuali: cosa si aggiunge, si sopprime, cambia. */
    private final class Comparison {

        private final Map<String, Row> fileProvinces = new LinkedHashMap<>();
        private final Map<String, Row> fileMunicipalities = new LinkedHashMap<>();
        private final Map<String, Province> currentProvinces;
        private final Map<String, Municipality> currentMunicipalities;
        private final List<Row> addedProvinces = new ArrayList<>();
        private final List<Province> removedProvinces = new ArrayList<>();
        private final List<Row> changedProvinces = new ArrayList<>();
        private final List<Row> addedMunicipalities = new ArrayList<>();
        private final List<Municipality> removedMunicipalities = new ArrayList<>();
        private final List<Row> changedMunicipalities = new ArrayList<>();

        Comparison(List<Row> rows, Map<String, Province> currentProvinces,
                Map<String, Municipality> currentMunicipalities) {
            this.currentProvinces = currentProvinces;
            this.currentMunicipalities = currentMunicipalities;
            rows.stream().sorted((a, b) -> a.name().compareToIgnoreCase(b.name())).forEach(r -> {
                fileProvinces.putIfAbsent(r.provinceCode(), r);
                fileMunicipalities.put(r.cadastralCode(), r);
            });
            fileProvinces.values().forEach(r -> {
                Province current = currentProvinces.get(r.provinceCode());
                if (current == null) {
                    addedProvinces.add(r);
                } else if (!current.getName().equals(r.provinceName())
                        || !current.getAbbreviation().equals(r.provinceAbbreviation())) {
                    changedProvinces.add(r);
                }
            });
            currentProvinces.values().stream()
                    .filter(p -> !fileProvinces.containsKey(p.getCode()))
                    .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                    .forEach(removedProvinces::add);
            Set<String> renewedProvinces = changedProvinces.stream().map(Row::provinceCode).collect(Collectors.toSet());
            fileMunicipalities.values().forEach(r -> {
                Municipality current = currentMunicipalities.get(r.cadastralCode());
                if (current == null) {
                    addedMunicipalities.add(r);
                } else if (!current.getName().equals(r.name()) || !current.getIstatCode().equals(r.istatCode())
                        || !current.getProvince().getCode().equals(r.provinceCode())
                        || renewedProvinces.contains(r.provinceCode())) {
                    // anche la provincia rinominata è una nuova versione del comune, che punta alla nuova provincia
                    changedMunicipalities.add(r);
                }
            });
            currentMunicipalities.values().stream()
                    .filter(m -> !fileMunicipalities.containsKey(m.getCadastralCode()))
                    .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                    .forEach(removedMunicipalities::add);
        }

        void apply(LocalDate day) {
            // prima si chiudono le versioni (e si scrivono), poi si aprono le nuove: un solo comune attuale per codice
            removedProvinces.forEach(p -> p.close(day));
            changedProvinces.forEach(r -> currentProvinces.get(r.provinceCode()).close(day));
            removedMunicipalities.forEach(m -> m.close(day));
            changedMunicipalities.forEach(r -> currentMunicipalities.get(r.cadastralCode()).close(day));
            municipalities.flush();
            provinces.flush();
            Map<String, Province> active = new HashMap<>(currentProvinces);
            removedProvinces.forEach(p -> active.remove(p.getCode()));
            List<Province> newProvinces = new ArrayList<>();
            for (Row r : addedProvinces) {
                newProvinces.add(new Province(r.provinceCode(), r.provinceName(), r.provinceAbbreviation(), day));
            }
            for (Row r : changedProvinces) {
                newProvinces.add(new Province(r.provinceCode(), r.provinceName(), r.provinceAbbreviation(), day));
            }
            provinces.saveAll(newProvinces).forEach(p -> active.put(p.getCode(), p));
            List<Municipality> newMunicipalities = new ArrayList<>();
            for (Row r : addedMunicipalities) {
                newMunicipalities.add(newMunicipality(r, active, day));
            }
            for (Row r : changedMunicipalities) {
                newMunicipalities.add(newMunicipality(r, active, day));
            }
            municipalities.saveAll(newMunicipalities);
            municipalities.flush();
        }

        private Municipality newMunicipality(Row r, Map<String, Province> active, LocalDate day) {
            return new Municipality(r.cadastralCode(), r.istatCode(), r.name(),
                    Objects.requireNonNull(active.get(r.provinceCode())), day);
        }

        MunicipalityImportPreviewDTO toDto() {
            return new MunicipalityImportPreviewDTO(fileMunicipalities.size(),
                    new MunicipalityChangesDTO(addedMunicipalities.size(), removedMunicipalities.size(),
                            changedMunicipalities.size(),
                            examples(addedMunicipalities, Comparison::label),
                            examples(removedMunicipalities, Comparison::label),
                            examples(changedMunicipalities,
                                    r -> label(currentMunicipalities.get(r.cadastralCode())) + " → " + label(r))),
                    new MunicipalityChangesDTO(addedProvinces.size(), removedProvinces.size(),
                            changedProvinces.size(),
                            examples(addedProvinces, Comparison::provinceLabel),
                            examples(removedProvinces, p -> p.getName() + " (" + p.getAbbreviation() + ")"),
                            examples(changedProvinces, r -> {
                                Province p = currentProvinces.get(r.provinceCode());
                                return p.getName() + " (" + p.getAbbreviation() + ") → " + provinceLabel(r);
                            })));
        }

        private static <T> List<String> examples(List<T> items, Function<T, String> label) {
            return items.stream().limit(EXAMPLES).map(label).toList();
        }

        private static String label(Row r) {
            return "%s (%s, %s)".formatted(r.name(), r.provinceAbbreviation(), r.istatCode());
        }

        private static String label(Municipality m) {
            return "%s (%s, %s)".formatted(m.getName(), m.getProvince().getAbbreviation(), m.getIstatCode());
        }

        private static String provinceLabel(Row r) {
            return r.provinceName() + " (" + r.provinceAbbreviation() + ")";
        }
    }
}
