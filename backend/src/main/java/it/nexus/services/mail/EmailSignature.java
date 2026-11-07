package it.nexus.services.mail;

/** Firma in fondo a tutte le email di NEXUS (richiesta del PO del 07/11), uguale a quella delle email di Keycloak. */
final class EmailSignature {

    static final String TEXT = "Portale Nexus";

    /** Riga vuota e firma, da aggiungere in fondo al testo. */
    static final String BLOCK = "\n" + TEXT + "\n";

    private EmailSignature() {
    }
}
