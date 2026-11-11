package it.nexus.mapper;

import it.nexus.domain.Municipality;
import it.nexus.domain.dto.MunicipalityDTO;

/** Versione di un comune (US-114) verso l'API e i testi: «Firenze (FI)» negli indirizzi e nell'audit (US-1007). */
public final class MunicipalityMapper {

    private MunicipalityMapper() {
    }

    public static MunicipalityDTO toDto(Municipality m) {
        return m == null ? null
                : new MunicipalityDTO(TsidMapper.toExternal(m.getId()), m.getName(), m.getIstatCode(),
                        m.getCadastralCode(), m.getProvince().getName(), m.getProvince().getAbbreviation(),
                        m.getValidFrom(), m.getValidTo());
    }

    /** «Sesto Fiorentino (FI)»; {@code null} senza comune. */
    public static String label(Municipality m) {
        return m == null ? null : "%s (%s)".formatted(m.getName(), m.getProvince().getAbbreviation());
    }
}
