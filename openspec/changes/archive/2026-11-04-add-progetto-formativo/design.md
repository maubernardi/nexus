## Decisions

### D1 — Stesso meccanismo, nuovo valore dell'enum
`InternshipDocumentKind` riceve `PROGETTO_FORMATIVO` e il campo `filePrefix`: l'etichetta della cronologia
("Progetto Formativo") e il nome del file ("Progetto-formativo-…") sono separati. Servizio, endpoint, accesso e audit
non cambiano.

### D2 — Obiettivi e attività da compilare a mano
La scheda non ha ancora obiettivi e attività del tirocinio: i campi veri arriveranno con i moduli ufficiali (Q5). Il
modello segnaposto lascia righe libere da compilare con il tutor aziendale. Nessun campo nuovo nella scheda, e quindi
nessuna migrazione.
