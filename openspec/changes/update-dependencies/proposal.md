## Why

Enabler **EN-6** (Sprint 11). Attivati l'08/11, gli avvisi di Dependabot hanno trovato 32 vulnerabilità nel frontend. Il
PO ha deciso di aggiornarle tutte come prima cosa dello Sprint 11 e di tenere **sempre** le versioni il più aggiornate
possibile, al posto del congelamento del 27/09 (review dello Sprint 10, §8.2): ultime stabili, LTS per Java e Node, PR
di Dependabot unite dal PO, versioni principali in PR separate.

## What Changes

- Aggiornamenti fatti in PR separate (#125–#132): frontend e backend alle ultime minori; Keycloak 26.8.2 (con lo scope
  `kc-admin-api` per il client tecnico); vite-plugin-pwa 2; i18next 27 e react-i18next 18; jsdom 31; TypeScript 7 (con
  l'API 6 per typescript-eslint); Node 26 LTS e pnpm 12.18. Avvisi di Dependabot: 0 aperti (`deepmerge` archiviato con
  l'ok del PO: nessuna correzione esistente, solo build).
- Action della CI all'ultima versione, sempre fissate per SHA.
- **Dependabot configurato** (`.github/dependabot.yml`): ogni lunedì PR per npm, Maven, immagini Docker, compose e
  action; minori raggruppate per ecosistema, principali una per PR; Java e i tipi di Node restano sulla LTS.
- **Controllo in CI** che le immagini dei test (Testcontainers) siano le stesse della produzione, perché Dependabot non
  aggiorna le versioni scritte nel codice Java.
- `CLAUDE.md` e README: regola «Versioni sempre aggiornate».

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `deployment`: requisito aggiunto per le dipendenze aggiornate.

## Impact

- Solo configurazione della CI e documentazione in questa PR; nessuna migrazione, nessun passo sul server.
