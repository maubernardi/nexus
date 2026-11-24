## Why

Storia **EN-10** (Sprint 17). PostgreSQL 19 è l'ultima versione principale, e la regola dell'08/11 vuole le versioni
sempre aggiornate. Le tre PR di Dependabot (#182–#184) non si possono unire così: l'immagine della 19 trova nel volume
i dati della 18 e **non parte** (lo dice l'immagine stessa e chiede `pg_upgrade`). Il deploy fallirebbe e tornerebbe da
solo alla release precedente. Serve un passaggio dei dati.

Decisione del PO del 23/11: l'aggiornamento si fa sulla demo **prima** del passaggio in produzione. Così la produzione
nasce sulla 19 e il collaudo si fa sulla versione che si userà.

## What Changes

- **PostgreSQL 19 ovunque**, in una sola PR:
  - compose del server e di sviluppo;
  - immagine del backup e ripristino di prova;
  - test (Testcontainers).
- **Un volume nuovo per la 19**: `postgres19-data` al posto di `postgres-data`. Il nome del volume porta la versione
  principale. I dati della 18 restano nel loro volume, intatti, e sono il ritorno indietro.
- **Script dell'aggiornamento** `infra/deploy/aggiorna-postgresql.sh`, lanciato dal PO con sudo **prima del deploy**:
  1. controlli;
  2. fermo del sito;
  3. backup su IONOS;
  4. copia dei due database;
  5. ripristino in una PostgreSQL 19 temporanea, sul volume nuovo;
  6. confronto dei numeri (righe, trigger, ultima migrazione, utenti di Keycloak).

  Il deploy poi avvia la 19 sul volume già pronto: backend e Keycloak non vedono mai un database vuoto. Dopo il deploy:
  - `verifica` rifà il confronto e un backup sulla 19;
  - `pulisci`, quando il PO decide, toglie il volume della 18.
- **Controllo delle immagini (EN-6)** più ampio:
  - tutte le immagini di PostgreSQL dei test;
  - lo script dell'aggiornamento;
  - il nome del volume, che deve contenere la versione principale. Una PR che cambia la versione principale senza
    volume nuovo fallisce nella CI.
- **Prova nella CI** dello script, su uno stack ridotto con gli stessi volumi del server: aggiornamento, deploy, ritorno
  alla 18, pulizia.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `deployment`: aggiornamento della versione principale di PostgreSQL con backup, ripristino e ritorno indietro;
  controllo del volume per versione nelle immagini.

## Impact

- `infra/deploy/compose.yaml`, `infra/docker-compose.yml`: immagine `postgres:19.0-alpine`, volume `postgres19-data`.
- `backup/Dockerfile`, `backup/ripristino-di-prova.sh`, test Java con Testcontainers.
- Nuovi: `infra/deploy/aggiorna-postgresql.sh`, `tools/ci/aggiorna-postgresql-e2e.sh`, job nella CI.
- `tools/ci/check-image-versions.sh`, README («Aggiornare PostgreSQL»), collaudo.
- Sul server, una volta: lo script con sudo, poi il deploy. Il sito resta fermo dallo script alla fine del deploy.
- Sviluppo in locale: il database riparte vuoto sul volume nuovo e i dati dimostrativi si ricaricano da soli. Il
  volume vecchio si toglie con `docker volume rm nexus_postgres-data`.
- Nessuna migrazione Flyway.
