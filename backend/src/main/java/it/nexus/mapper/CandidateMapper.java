package it.nexus.mapper;

import java.util.Arrays;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import it.nexus.domain.Candidate;
import it.nexus.domain.CandidateLanguage;
import it.nexus.domain.dto.CandidateDTO;
import it.nexus.domain.dto.CandidateLanguageDTO;
import it.nexus.domain.dto.CandidateSummaryDTO;
import it.nexus.domain.enumeration.LicenseType;

@Mapper(uses = ReferenceMapper.class, imports = TsidMapper.class)
public interface CandidateMapper {

    @Mapping(target = "id", expression = "java(TsidMapper.toExternal(candidate.getId()))")
    @Mapping(target = "hasDrivingLicense", source = "hasDrivingLicense")
    @Mapping(target = "hasVehicle", source = "hasVehicle")
    @Mapping(target = "hasLaw68", source = "hasLaw68")
    CandidateDTO toDto(Candidate candidate);

    @Mapping(target = "id", expression = "java(TsidMapper.toExternal(candidate.getId()))")
    @Mapping(target = "residenceZoneName", source = "residenceZone.name")
    CandidateSummaryDTO toSummary(Candidate candidate);

    CandidateLanguageDTO toDto(CandidateLanguage language);

    default List<LicenseType> licenseTypes(LicenseType[] types) {
        return types == null ? List.of() : Arrays.stream(types).sorted().toList();
    }
}
