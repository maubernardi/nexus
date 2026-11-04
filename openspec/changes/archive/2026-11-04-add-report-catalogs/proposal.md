## Why

Storia **US-109** (Sprint 8, review dello Sprint 7 §8.4). Nel report di restituzione (US-1101) il Tutor sceglie la
formula di assunzione o il motivo della mancata assunzione. Le voci sono provvisorie (Q6) e "devono poter cambiare
senza rilascio": diventano due cataloghi dell'ADMIN, come zone, tipologie e tipi di tirocinio.

## What Changes

- **Migrazione V25**: tabelle `hiring_formula` e `no_hire_reason`, con gli stessi vincoli degli altri cataloghi:
  - codice unico e in formato;
  - nome non vuoto;
  - nome unico tra le voci attive.

  Le voci iniziali sono quelle provvisorie di Q6:
  - formule: tempo determinato 3, 6 e 12 mesi, tempo indeterminato, contratto a chiamata, prestazione occasionale;
  - motivi: l'azienda non ha risorse, giudica il tirocinante non idoneo, non aveva mai espresso l'intenzione di
    assumere.
- **Cataloghi `hiring-formulas` e `no-hire-reasons`** in `/catalogs/{kind}` (solo ADMIN), con le regole di US-106:
  aggiunta, nuovo nome, disattivazione e riattivazione, con audit (`HIRING_FORMULA`, `NO_HIRE_REASON`).
- **Voci attive per il report**: `GET /reference/hiring-formulas` e `GET /reference/no-hire-reasons`.
- **Frontend**: due sezioni in più nella pagina "Cataloghi".

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `reference-data`: cataloghi del report di restituzione.
