package it.nexus.domain.enumeration;

/** Esito di un tentativo di contatto e della richiesta chiusa (US-512): fissi, dall'analisi con il PO del 23/11. */
public enum ContactOutcome {
    DISPONIBILE("Disponibile a ospitare tirocini"),
    NON_INTERESSATA("Non interessata"),
    NON_RAGGIUNGIBILE("Non raggiungibile");

    private final String label;

    ContactOutcome(String label) {
        this.label = label;
    }

    /** Il testo per il Tutor, anche nell'avviso di chiusura ({@code {esito}}). */
    public String label() {
        return label;
    }
}
