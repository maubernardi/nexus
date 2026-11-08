## Why

Storia **US-1307** (Sprint 10): prima della produzione serve accorgersi se l'app non risponde, prima degli utenti, e una
revisione di sicurezza (OWASP ASVS livello 1). Decisione del PO (review dello Sprint 9, §8.7): controllo dalla CI di
GitHub ogni 10 minuti, **solo dalle 8 alle 20**, con un'email al PO se fallisce; la revisione la fa il team, con un
documento di esito.

## What Changes

- Workflow **«Controllo della demo»** (`uptime.yml`): ogni 10 minuti dalle 8 alle 20 ora italiana (il cron di GitHub è
  in UTC: il primo passo salta i run fuori fascia, anche con l'ora legale); controlla PWA, Keycloak e backend
  (`/api/v1/me` → `401`) con 3 tentativi a 30 secondi, e la scadenza dei certificati (almeno 14 giorni). Se fallisce,
  GitHub avvisa il PO per email. Nessun segreto.
- Documento **`docs/security/asvs-l1.md`**: esito requisito per requisito, con due lacune e le azioni proposte (password
  comuni non bloccate; nessun avviso per dipendenze vulnerabili).

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `deployment`: requisiti aggiunti per il controllo della disponibilità e la revisione di sicurezza.

## Impact

- Solo un workflow e documentazione: nessuna modifica a backend, frontend e database; nessun passo sul server.
