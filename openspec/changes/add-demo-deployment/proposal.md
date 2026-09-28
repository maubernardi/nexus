## Why

NEXUS deve essere raggiungibile online per mostrarla, provarla da smartphone come PWA installabile (che richiede
HTTPS) e validare presto l'infrastruttura. Serve anche a far emergere subito i problemi di deploy, mentre l'app è
ancora piccola. Il server (VPS IONOS, Rocky Linux 9, 4 GB di RAM) e il dominio `portalenexus.it` sono disponibili,
con SSH già messo in sicurezza. Il primo ambiente è una **demo con dati fittizi**: diventerà produzione quando ci
saranno le funzionalità, dopo una ripulitura dei dati.

## What Changes

- **Immagini container** costruite da GitHub Actions e pubblicate su GitHub Container Registry, taggate con il commit:
  - `nexus-backend`: Temurin JRE 25, utente non root;
  - `nexus-web`: Caddy con la PWA compilata. Fa da reverse proxy HTTPS verso backend e Keycloak.
- **Stack di produzione** (`infra/deploy/`), in Docker Compose:
  - Caddy, backend, Keycloak 26.7.4 in modalità produzione, PostgreSQL 18.6;
  - verso Internet sono esposte solo le porte 80 e 443;
  - limiti di memoria e rotazione dei log.
- **HTTPS automatico** (Let's Encrypt) per `portalenexus.it`, `www.portalenexus.it` (redirect) e `auth.portalenexus.it`,
  con header di sicurezza (HSTS, CSP, anti-framing).
- **Keycloak di produzione**:
  - realm demo separato da quello di sviluppo, con utenti demo e password generate sul server;
  - protezione brute force attiva;
  - console di amministrazione non esposta pubblicamente.
- **Database**: due utenti distinti. Il proprietario dello schema esegue le migrazioni; l'utente applicativo ha solo
  permessi sui dati, quindi non può alterare schema né trigger (audit trail).
- **Deploy su comando**: workflow GitHub "Run workflow" che:
  - costruisce e pubblica le immagini;
  - le installa sul server tramite una chiave SSH dedicata e limitata (forced command), che può solo lanciare lo
    script di deploy per un commit del repository;
  - verifica infine che l'app risponda.
- **Preparazione una tantum del server** (`bootstrap.sh`, eseguito dal committente):
  - Docker CE;
  - utente `deploy`;
  - segreti generati in `.env`, fuori da git;
  - firewall 80/443.
- Documentazione: DNS da configurare, preparazione, deploy, accesso d'emergenza alla console di Keycloak.

## Capabilities

### New Capabilities
- `deployment`: come NEXUS viene pubblicata ed esercita online (HTTPS, superficie esposta, segreti, deploy
  riproducibile, ambiente demo).

### Modified Capabilities
<!-- nessuna -->

## Non-goals

- Backup automatici e invio di email (SMTP): prima del passaggio in produzione, in change dedicate.
- Monitoraggio e alerting, più ambienti (staging e produzione separati), deploy senza interruzione.
- Nessuna modifica funzionale all'app. Il messaggio dedicato "utente non abilitato" nel frontend resta un follow-up.
- PWA e accessibilità: nessun impatto sui requisiti. L'HTTPS rende la PWA installabile e il service worker attivo online.

## Impact

- Nuovi file: `infra/deploy/**`, `backend/Dockerfile`, `frontend/Dockerfile`, `.github/workflows/deploy-demo.yml`.
- Backend: supporto a utenti database separati per Flyway e applicazione, JWKS di Keycloak via rete interna,
  header del proxy. Il comportamento in sviluppo e test non cambia.
- GitHub: environment `demo` con i segreti di deploy, pacchetti GHCR `nexus-backend` e `nexus-web`.
- DNS IONOS: record A per `auth` e `www`, CAA per Let's Encrypt.
- Server: Docker CE 29.8.1 e Compose 5.5.1 (versioni esatte), porte 80/443 aperte su firewalld.
