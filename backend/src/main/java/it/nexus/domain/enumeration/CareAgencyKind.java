package it.nexus.domain.enumeration;

/** Tipologia dell'ente che ha in carico il beneficiario (progetto personalizzato di inclusione, US-115). */
public enum CareAgencyKind {
    COMUNE,
    UNIONE_COMUNI,
    COMUNITA_MONTANA,
    SOCIETA_DELLA_SALUTE,
    AZIENDA_SANITARIA,
    /** Ufficio interdistrettuale di esecuzione penale esterna. */
    UIEPE,
    ALTRO
}
