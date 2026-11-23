## 1. Infra: immagine e servizio

- [x] 1.1 Immagine `backup/` con `nexus-backup` (`pianifica`, `backup`, `stato`) (D1–D6); shellcheck pulito
- [x] 1.2 Servizio `backup` e volume `backup-state` nel compose del server, variabili facoltative (D11); il deploy
  costruisce e pubblica `nexus-backup`
- [x] 1.3 `nexus-deploy`: `backup`, `backup adesso`, `logs backup` (D7); controllo degli argomenti come per `logs`
- [x] 1.4 Controllo delle immagini (EN-6) e Dependabot anche per `backup/Dockerfile`

## 2. CI e workflow

- [x] 2.1 `tools/ci/backup-e2e.sh` con PostgreSQL 18.6, VersityGW e una chiave age di prova (D10); job «Backup» nella
  CI
- [x] 2.2 Workflow «Backup del database»: controllo delle 9 con l'ora legale e avvio manuale (D8); «Stato del server»
  con i log del backup

## 3. Ripristino e documentazione

- [x] 3.1 `backup/ripristino-di-prova.sh` (D9), provato in locale con l'archivio della prova
- [x] 3.2 README: variabili, preparazione su IONOS, chiavi age, aggiornamento di `nexus-deploy`, ripristino di prova
  e sul server
- [x] 3.3 Collaudo, Sprint 16, backlog
- [x] 3.4 `openspec validate add-database-backup --strict` senza errori; CI verde

## 4. Messa in funzione (con il PO, dopo il merge)

- [x] 4.1 Deploy sulla demo; esito «non configurato»
- [x] 4.2 Contenitore su IONOS con il blocco, chiavi, coppia age, `.env`, `nexus-deploy` aggiornato
- [x] 4.3 «Backup adesso» dalle Actions, poi ripristino di prova sul PC del PO con l'archivio vero
