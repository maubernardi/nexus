## Why

Storia **US-1303** (Sprint 10): i dati identificativi dei beneficiari vanno anonimizzati alla scadenza della
conservazione (GDPR, Q7: parametrica, per ora 2 anni). Decisioni del PO del 07/11:
- i 2 anni contano dall'**ultima attività**, così nessuno resta per sempre;
- di nome e cognome restano le **iniziali**; si cancellano CV, vincoli e i dati identificativi e le note delle schede dei
  tirocini; restano i dati statistici;
- un **job notturno segna** «Da anonimizzare»; il Tutor e l'ADMIN li vedono evidenziati, li filtrano e **confermano**,
  oppure **tengono i dati per sempre** con un motivo obbligatorio; una nuova attività toglie il segno.

## What Changes

- **Migrazione V31**: `beneficiary.anonymization_due_since` e `kept_reason`, con i vincoli `ck_beneficiary_kept_reason`
  e `ck_beneficiary_retention`.
- **Job notturno** (`nexus.retention.cron`, 2:30 ora di Roma; periodo `nexus.retention.period`, P2Y): toglie il segno a
  chi ha avuto attività e segna chi non ne ha da 2 anni (scheda, segnalazioni, schede dei tirocini). Il segno non conta
  come attività.
- **Decisioni** (Tutor proprietario o ADMIN, solo su un beneficiario da anonimizzare): anonimizza / tieni i dati con il
  motivo; audit `ANONYMIZATION_DUE`, `ANONYMIZATION_CANCELLED`, `ANONYMIZE`, `KEEP` senza valori personali.
- Un anonimizzato non si modifica e non si segnala più.
- **Frontend**: nei «Miei beneficiari» la colonna «Dati» con lo stato, le righe da anonimizzare evidenziate, il filtro e
  le due decisioni (conferma; motivo obbligatorio); per l'ADMIN la pagina «Beneficiari» (`/beneficiari/tutti`) con tutti,
  il Tutor, la data del segno e il motivo dei dati tenuti.
- Dati demo: un beneficiario fermo da 3 anni, per provarlo sulla demo.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `beneficiary-registry`: requisito aggiunto per la conservazione e l'anonimizzazione.

## Impact

- Backend: `GET /beneficiaries/all` (ADMIN), `POST /beneficiaries/{id}/anonymize`, `POST /beneficiaries/{id}/keep`;
  `retention` nel riepilogo del beneficiario.
- Migrazione **V31**. Nessun passo manuale sul server.
