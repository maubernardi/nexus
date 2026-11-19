package it.nexus.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import it.nexus.domain.Company;
import it.nexus.domain.CompanyContact;
import it.nexus.domain.dto.CompanyContactDTO;
import it.nexus.domain.dto.CompanyDTO;

@Mapper(uses = ReferenceMapper.class, imports = {TsidMapper.class, MunicipalityMapper.class})
public interface CompanyMapper {

    @Mapping(target = "id", expression = "java(TsidMapper.toExternal(company.getId()))")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "legalMunicipality",
            expression = "java(MunicipalityMapper.toDto(company.getLegalMunicipality()))")
    @Mapping(target = "operatingMunicipality",
            expression = "java(MunicipalityMapper.toDto(company.getOperatingMunicipality()))")
    @Mapping(target = "missingData", expression = "java(company.missingData())")
    CompanyDTO toDto(Company company);

    /** Referente (US-510), con il ruolo anche se disattivato. */
    CompanyContactDTO toDto(CompanyContact contact);
}
