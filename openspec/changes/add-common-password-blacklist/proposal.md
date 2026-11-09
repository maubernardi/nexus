## Why

Enabler **EN-7** (Sprint 11): chiude la lacuna **L1** della revisione OWASP ASVS livello 1 (2.1.7, password comuni o
compromesse rifiutate). Decisione del PO nella review dello Sprint 10 (§8.3): policy `passwordBlacklist` di Keycloak
con un elenco pubblico delle password più usate, nell'immagine di Keycloak.

## What Changes

- **Elenco** `keycloak/password-blacklists/nexus-common-passwords.txt`: dal milione di password più usate di SecLists
  (licenza MIT, commit fissato, SHA-256 verificato) solo quelle da 12 caratteri in su — **46 146** — perché le più corte
  le rifiuta già `length(12)`. L'elenco da 100 000 ne avrebbe date solo 488. Si rigenera con
  `tools/keycloak/update-password-blacklist.sh`.
- **Immagine** `nexus-keycloak`: l'elenco in `/opt/keycloak/data/password-blacklists/`.
- **Policy** del realm della demo: `length(12) and notUsername and notEmail and passwordBlacklist(nexus-common-passwords.txt)`;
  sul server già installato con `infra/deploy/keycloak-password-policy.sh`, dopo il deploy.
- **Messaggio** in italiano: «Password troppo comune: è tra quelle più usate e facili da indovinare. Scegline
  un'altra.»
- **Prova in CI** con Keycloak vero: `password1234` rifiutata con il messaggio, poi una password valida accettata.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `login-page`: requisito aggiunto per le password comuni.

## Impact

- Le password già impostate restano valide; la regola vale dal prossimo cambio (invito, recupero, aggiornamento).
- Nessuna migrazione; un passo sul server dopo il deploy.
