## Decisions

### D1 — La completezza la calcola il server, sui dati salvati
`InternshipService` elenca i dati mancanti, come codici (`missingForStart`) nella risposta della scheda. Li usa due
volte:
- la pagina mostra l'elenco con i collegamenti ai campi;
- l'avvio rifiuta con le stesse etichette in italiano.

Conta solo ciò che è salvato: se il Tutor ha modifiche non salvate, l'elenco lo dice ("in base alla scheda salvata").

### D2 — Legale rappresentante: mancante o incompleto
Il Tutor può inserire il legale rappresentante se manca, non correggerlo (PO, 29/10). Per questo i casi sono due:
- `legalRepresentative`: manca del tutto, e il Tutor lo inserisce dalla scheda;
- `legalRepresentativeData`: c'è, ma senza codice fiscale, nascita o residenza. La pagina dice di chiederlo al Call
  Center.

### D3 — Nessun vincolo sulla data
L'avvio non controlla la data di inizio: il Tutor conferma quando il tirocinio parte. Un limite in più (per esempio
"non prima della data di inizio") andrebbe chiesto al PO prima di realizzarlo (azione A14).

### D4 — Come gli altri passaggi del Tutor
Lo schema è quello dell'appuntamento fissato (US-801):
- solo il Tutor della segnalazione (`403` per gli altri);
- versione della segnalazione (`409` se superata);
- evento nello storico dell'azienda e notifica all'operatore.

La mansione resta bloccata dalla segnalazione per tutto il tirocinio.
