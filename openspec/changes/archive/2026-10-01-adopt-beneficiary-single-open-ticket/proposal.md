## Why

Correzione del Product Owner (01/10/2026) su **US-201/US-301** e risposta alla domanda **Q12**:
1. il soggetto della segnalazione è il **beneficiario**, non il "candidato";
2. un beneficiario non può avere più di **una segnalazione aperta** alla volta.

## What Changes

- **Terminologia ovunque**: testi dell'interfaccia, codice (`Beneficiary`, `BeneficiaryService`, …), API
  (`/api/v1/beneficiaries`, campo `beneficiaryId`), rotte (`/beneficiari/nuovo`, `?beneficiario=`), database (migrazione
  `V8`: tabelle `beneficiary`/`beneficiary_language`, colonne `beneficiary_id`, vincoli e indici rinominati), seed demo,
  spec, backlog. Le spec principali `domain-model` e `dev-seed-data` sono corrette direttamente: cambia solo il nome,
  non il comportamento. Le change non ancora archiviate (US-201, US-301, US-401) sono allineate.
- **Una segnalazione aperta per beneficiario** ("aperta" = ogni stato tranne `FORM_RESTITUZIONE`): controllo nel service
  (`400` sul campo `beneficiaryId` con il numero della segnalazione aperta) e indice unico parziale
  `uq_ticket_open_per_beneficiary` nel database (`V9`), che copre anche le richieste concorrenti (`409`).
- L'elenco dei beneficiari del Tutor riporta il numero dell'eventuale segnalazione aperta; nel modulo quei beneficiari
  restano visibili ma non selezionabili, e se lo sono tutti la pagina lo spiega.
- `V9` riallinea i soli dati demo (se presenti) spostando due ticket su due nuovi beneficiari fittizi.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `ticket-submission`: nuova regola sulla segnalazione aperta.
- `domain-model`: nuovo vincolo nel database.

## Non-goals

- Regole per la segnalazione speciale dalla bacheca (US-306) e per il ticket RIAPERTO: valgono la stessa regola e lo
  stesso vincolo; i dettagli si discutono con quelle storie.
