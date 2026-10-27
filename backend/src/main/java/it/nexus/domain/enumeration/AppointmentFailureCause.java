package it.nexus.domain.enumeration;

/**
 * Causa dell'appuntamento non andato a buon fine (US-802). Voci provvisorie, da confermare con il committente (review
 * dello Sprint 4, §8.2); con {@link #ALTRO} la nota è obbligatoria.
 */
public enum AppointmentFailureCause {
    AZIENDA_NON_CONFERMA("L'azienda non ha confermato il tirocinio"),
    BENEFICIARIO_RINUNCIA("Il beneficiario ha rinunciato"),
    BENEFICIARIO_ASSENTE("Il beneficiario non si è presentato"),
    ALTRO("Altro");

    private final String label;

    AppointmentFailureCause(String label) {
        this.label = label;
    }

    /** Testo per cronologia e storico dell'azienda. */
    public String label() {
        return label;
    }
}
