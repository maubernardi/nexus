package it.nexus.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import it.nexus.domain.Company;
import it.nexus.domain.CompanyContact;
import it.nexus.domain.dto.CompanyContactDTO;
import it.nexus.domain.dto.CompanyDTO;
import it.nexus.domain.dto.CompanyDirectoryItemDTO;

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

    /** Azienda nell'elenco del Tutor (US-511): solo i dati essenziali, con la sede operativa se indicata. */
    @Mapping(target = "id", expression = "java(TsidMapper.toExternal(company.getId()))")
    @Mapping(target = "address", expression = "java(hasOperatingSeat(company) ? company.getOperatingAddress() "
            + ": company.getLegalAddress())")
    @Mapping(target = "municipality", expression = "java(MunicipalityMapper.label(hasOperatingSeat(company) "
            + "? company.getOperatingMunicipality() : company.getLegalMunicipality()))")
    CompanyDirectoryItemDTO toDirectoryItem(Company company);

    /** La sede operativa vale se ha l'indirizzo o il comune (US-510); altrimenti la sede legale. */
    default boolean hasOperatingSeat(Company company) {
        return company.getOperatingAddress() != null || company.getOperatingMunicipality() != null;
    }
}
