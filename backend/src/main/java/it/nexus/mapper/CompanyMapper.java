package it.nexus.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import it.nexus.domain.Company;
import it.nexus.domain.dto.CompanyDTO;

@Mapper(imports = TsidMapper.class)
public interface CompanyMapper {

    @Mapping(target = "id", expression = "java(TsidMapper.toExternal(company.getId()))")
    @Mapping(target = "active", source = "active")
    CompanyDTO toDto(Company company);
}
