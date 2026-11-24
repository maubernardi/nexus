## Why

Storia tecnica **EN-11** (Sprint 17), decisa dal PO il 24/11 subito dopo il passaggio in produzione. Fino a ieri le
storie si collaudavano sulla demo, `portalenexus.it`, con dati e utenti finti. Ora lì ci sono i dati veri: le prove con
dati finti finirebbero per sempre nell'audit, e potrebbero partire email verso persone reali. Senza un posto dove
collaudare, nessuna storia nuova può arrivare a Done.

Il PO ha scelto (24/11):
- **il collaudo sul suo PC**, tra quattro strade: PC, prova sullo stesso server, secondo server, produzione;
- **prima del merge**, dal branch della PR: si uniscono solo storie già provate; dopo il deploy basta un controllo
  veloce in produzione.

## What Changes

- **Un comando** avvia sul PC lo stack della produzione per una PR, un branch o un commit: PostgreSQL, Keycloak con il
  tema, backend, PWA dietro Caddy. Le immagini sono costruite in locale dai sorgenti di quel commit, senza toccare il
  lavoro in corso nel repository.
- **Dati e utenti finti**: i dati del seed, come la vecchia demo; gli utenti `tutor1`, `tutor2`, `operatore.cc` e `admin`
  con una password del collaudo. Ogni avvio riparte da zero.
- **Email in una casella finta** (Mailpit), da leggere nel browser: inviti, notifiche, codici e recupero della password
  si provano senza che nessuna email esca dal PC.
- **Fedele alla produzione**: compose, realm, policy delle password e regole di Caddy sono quelli del commit. Cambiano
  solo gli indirizzi (localhost, in HTTP) e la posta.
- **Fermare, riprendere, cancellare**: i dati restano se si ferma; `cancella` toglie container, volumi, immagini e
  sorgenti (A37).
- **Prova nella CI** a ogni PR: avvio del collaudo, accesso di un utente dalla pagina di Keycloak, email del backend e di
  Keycloak nella casella finta, cancellazione senza volumi rimasti.
- **Processo**: le prove T-XX si fanno sul PC prima del merge; la Definition of Done e il collaudo cambiano di
  conseguenza.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `deployment`: ambiente di collaudo sul PC del PO, dallo stack della produzione.

## Impact

- Nuovi file:
  - `infra/collaudo/`: script, override del compose, Caddyfile del collaudo, realm del collaudo;
  - `tools/ci/collaudo-e2e.py`.
- CI: nuovo job «Ambiente di collaudo»; Dependabot segue l'immagine di Mailpit.
- Documenti:
  - README, «Collaudo sul PC»;
  - Definition of Done (`docs/agile/README.md`);
  - `docs/test/collaudo.md`, come prepararsi e T-33;
  - backlog EN-11 e sprint 17.
- Produzione: nessun cambiamento. Nessuna migrazione.
