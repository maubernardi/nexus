package it.nexus.services.documents;

import it.nexus.domain.dto.CompanyTutorDTO;

/**
 * Tutor aziendale su una riga per i documenti (US-1009): «Mario Rossi, Responsabile di produzione (055 123456)». Ruolo e
 * recapito mancanti si saltano senza lasciare separatori.
 */
public final class CompanyTutorLine {

    private CompanyTutorLine() {
    }

    public static String of(CompanyTutorDTO tutor) {
        String line = tutor.role() == null ? tutor.fullName() : tutor.fullName() + ", " + tutor.role();
        return tutor.contact() == null ? line : "%s (%s)".formatted(line, tutor.contact());
    }
}
