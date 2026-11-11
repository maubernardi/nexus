package it.nexus.services.documents;

import java.util.List;

/**
 * Segnaposto che NEXUS sa compilare nei documenti del tirocinio (US-1004, US-1007, US-1009): i modelli nel codice e
 * quelli caricati dall'ADMIN (US-1010) possono usare solo questi. US-1011 aggiungerà quelli dei documenti reali.
 */
public final class DocumentPlaceholders {

    public static final List<String> KNOWN = List.of("NUMERO_SEGNALAZIONE", "PROGETTO", "DATA_DOCUMENTO",
            "TIPO_TIROCINIO", "TUTOR_PROMOTORE", "RAGIONE_SOCIALE", "PARTITA_IVA", "SEDE_LEGALE", "CODICE_ATECO",
            "LEGALE_RAPPRESENTANTE", "CODICE_FISCALE_LEGALE_RAPPRESENTANTE", "DATA_NASCITA_LEGALE_RAPPRESENTANTE",
            "LUOGO_NASCITA_LEGALE_RAPPRESENTANTE", "RESIDENZA_LEGALE_RAPPRESENTANTE", "NOME_TIROCINANTE",
            "CODICE_FISCALE_TIROCINANTE", "DATA_NASCITA_TIROCINANTE", "LUOGO_NASCITA_TIROCINANTE",
            "RESIDENZA_TIROCINANTE", "MANSIONE", "SEDE_TIROCINIO", "DATA_INIZIO", "DATA_FINE", "SOSPENSIONI",
            "TUTOR_AZIENDALI", "ORE_SETTIMANALI", "MONTE_ORE_TOTALE", "GRIGLIA_ORARIA");

    private DocumentPlaceholders() {
    }
}
