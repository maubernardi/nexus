package it.nexus.mapper;

import java.util.Arrays;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import it.nexus.domain.Beneficiary;
import it.nexus.domain.BeneficiaryLanguage;
import it.nexus.domain.dto.BeneficiaryDTO;
import it.nexus.domain.dto.BeneficiaryLanguageDTO;
import it.nexus.domain.dto.BeneficiarySummaryDTO;
import it.nexus.domain.enumeration.LicenseType;

@Mapper(uses = ReferenceMapper.class, imports = TsidMapper.class)
public interface BeneficiaryMapper {

    @Mapping(target = "id", expression = "java(TsidMapper.toExternal(beneficiary.getId()))")
    @Mapping(target = "hasDrivingLicense", source = "hasDrivingLicense")
    @Mapping(target = "hasVehicle", source = "hasVehicle")
    @Mapping(target = "hasLaw68", source = "hasLaw68")
    BeneficiaryDTO toDto(Beneficiary beneficiary);

    @Mapping(target = "id", expression = "java(TsidMapper.toExternal(beneficiary.getId()))")
    @Mapping(target = "residenceZoneName", source = "residenceZone.name")
    @Mapping(target = "openTicketNumber", ignore = true)
    BeneficiarySummaryDTO toSummary(Beneficiary beneficiary);

    BeneficiaryLanguageDTO toDto(BeneficiaryLanguage language);

    default List<LicenseType> licenseTypes(LicenseType[] types) {
        return types == null ? List.of() : Arrays.stream(types).sorted().toList();
    }
}
