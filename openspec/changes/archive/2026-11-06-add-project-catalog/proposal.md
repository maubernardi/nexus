## Why

Storia **US-102** (Sprint 9). I progetti finanziati (GOL, POLIS…) oggi arrivano solo dal seed del database: per
abilitare un nuovo finanziamento serve un intervento tecnico. Primo passo dell'obiettivo dello Sprint 9: l'ADMIN
gestisce progetti e utenti da NEXUS.

## What Changes

- **Progetti come sesto catalogo** della pagina «Cataloghi», in cima, con le regole degli altri cataloghi (US-106):
  - aggiunta, nuovo nome, disattivazione e riattivazione, con audit;
  - il codice si ricava dal nome (es. «GOL» → `GOL`) e poi non cambia;
  - nome unico tra i progetti attivi, senza distinguere maiuscole e minuscole.
- **Progetto disattivato:** non si sceglie più in una nuova segnalazione né nei filtri, ma le segnalazioni già fatte lo
  mantengono e restano consultabili. Il backend lo impediva già: qui cambia solo chi può disattivare.
- **Migrazione V28:** codice fino a 30 caratteri (era 20, come negli altri cataloghi), nome non vuoto, nome unico tra
  i progetti attivi (`ck_project_name`, `uq_project_active_name`).
- L'assegnazione dei progetti agli utenti è US-104.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `reference-data`: i progetti entrano nei cataloghi dell'ADMIN.

## Impact

- Backend: `CatalogKind.PROJECTS` (`/catalogs/projects`), repository, V28.
- Frontend: sezione «Progetti» nella pagina «Cataloghi».
