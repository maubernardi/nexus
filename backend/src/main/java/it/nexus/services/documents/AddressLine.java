package it.nexus.services.documents;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.nexus.domain.dto.MunicipalityDTO;

/**
 * Indirizzo su una riga per i documenti (US-1007): «Via Roma 1, Firenze (FI)». Le parti mancanti si saltano senza
 * lasciare separatori; senza nessuna parte il risultato è {@code null} (la riga resta da completare a mano).
 */
public final class AddressLine {

    private AddressLine() {
    }

    /** Indirizzo e comune: «Via Roma 1, Firenze (FI)». */
    public static String of(String address, MunicipalityDTO municipality) {
        String place = municipality == null ? null
                : "%s (%s)".formatted(municipality.name(), municipality.provinceAbbreviation());
        return join(", ", address, place);
    }

    /** Via e numero civico della residenza, poi il comune: «Via Gramsci 12, Sesto Fiorentino (FI)». */
    public static String of(String street, String number, MunicipalityDTO municipality) {
        return of(join(" ", street, number), municipality);
    }

    private static String join(String separator, String... parts) {
        String joined = Stream.of(parts)
                .filter(Objects::nonNull)
                .map(String::strip)
                .filter(p -> !p.isEmpty())
                .collect(Collectors.joining(separator));
        return joined.isEmpty() ? null : joined;
    }
}
