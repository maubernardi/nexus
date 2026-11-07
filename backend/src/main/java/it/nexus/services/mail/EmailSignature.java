package it.nexus.services.mail;

/**
 * Oggetto e firma di tutte le email di NEXUS (richiesta del PO del 07/11), come nelle email di Keycloak (tema email
 * {@code nexus}): l'oggetto inizia con {@link #SUBJECT_PREFIX}, il testo finisce con la firma.
 */
final class EmailSignature {

    static final String SUBJECT_PREFIX = "PORTALE NEXUS - ";

    static final String TEXT = "Portale Nexus";

    /** Riga vuota e firma, da aggiungere in fondo al testo. */
    static final String BLOCK = "\n" + TEXT + "\n";

    private EmailSignature() {
    }
}
