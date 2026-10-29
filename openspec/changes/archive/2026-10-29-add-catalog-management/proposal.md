## Why

Storia **US-106** (Sprint 7, chiude R2 con la corsia veloce): l'ADMIN inserisce, rinomina e disattiva zone e
tipologie di mansione da maschere dedicate, per usare i valori reali della cooperativa senza rilasci (Q10). Una voce
disattivata non è più selezionabile ma resta sui dati esistenti; ogni modifica va nell'audit (EN-2).

## What Changes

- **Migrazione V20**: il nome non è vuoto (`ck_zone_name`, `ck_job_category_name`) ed è unico tra le voci attive,
  senza distinguere maiuscole e minuscole (`uq_zone_active_name`, `uq_job_category_active_name`).
- API `/api/v1/catalogs/{zones|job-categories}`, solo per l'ADMIN:
  - elenco di tutte le voci, prima le attive;
  - aggiunta (`POST`): il codice si ricava dal nome;
  - nuovo nome (`PUT`): il codice non cambia;
  - `POST /{id}/deactivate` e `/activate`.
  Audit `ZONE` / `JOB_CATEGORY`: `CREATE`, `UPDATE`, `DEACTIVATE`, `ACTIVATE`.
- Un nome già usato da una voce attiva è rifiutato sul campo. Riattivare una voce con il nome di un'altra attiva è
  rifiutato con il motivo.
- La generazione del codice dal nome (`ReferenceCodes`) è comune a US-302 (nuova tipologia approvata) e ai cataloghi.
- Frontend:
  - voce di menu **Cataloghi** per l'ADMIN;
  - pagina `/cataloghi` con le sezioni Zone e Tipologie di mansione: tabella con stato in testo, aggiunta, nuovo
    nome, disattivazione e riattivazione, esiti con il focus;
  - dopo ogni modifica si aggiornano anche i menu di zone e tipologie degli altri moduli.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `reference-data`: gestione dei cataloghi da parte dell'ADMIN.
