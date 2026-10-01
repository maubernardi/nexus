package it.nexus.mapper;

import io.hypersistence.tsid.TSID;

/** Conversione tra id interno (TSID a 64 bit) e forma esposta nelle API (stringa di 13 caratteri). */
public final class TsidMapper {

    private TsidMapper() {
    }

    public static String toExternal(Long id) {
        return id == null ? null : TSID.from(id).toString();
    }

    /** Id interno, oppure {@code null} se la stringa non è un TSID valido. */
    public static Long toInternal(String id) {
        return id != null && TSID.isValid(id) ? TSID.from(id).toLong() : null;
    }
}
