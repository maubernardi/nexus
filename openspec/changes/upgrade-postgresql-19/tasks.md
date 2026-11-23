## 1. PostgreSQL 19 ovunque

- [x] 1.1 `postgres:19.0-alpine` in compose del server e di sviluppo, immagine del backup, ripristino di prova, test
      Java (Testcontainers) e analisi del database attuale
- [x] 1.2 Volume `postgres19-data` nei due compose, con il commento sulla regola del volume per versione (D1)
- [x] 1.3 Prova in locale: la 19 non parte sul volume della 18, e la 18 riparte con i suoi dati (Context)

## 2. Script dell'aggiornamento

- [x] 2.1 `infra/deploy/aggiorna-postgresql.sh` con `prepara`, `verifica`, `pulisci` (D2–D6)
- [x] 2.2 `tools/ci/aggiorna-postgresql-e2e.sh`: rifiuti, aggiornamento, deploy, verifica, ritorno alla 18, pulizia
      (D7); passo nel job «Backup e ripristino» della CI
- [x] 2.3 Controllo delle immagini (EN-6): tutte le immagini dei test, lo script, il volume per versione (D8); prova in
      negativo in locale

## 3. Prove

- [x] 3.1 Prova del backup (`tools/ci/backup-e2e.sh`) con l'immagine sulla 19
- [x] 3.2 `./mvnw verify` con tutti i Testcontainers sulla 19 (246 test unitari, 561 di integrazione)
- [ ] 3.3 Ripristino di prova sul backup vero della demo con la 19 (il PO, sul PC); backend e Keycloak avviati sul
      database ripristinato

## 4. Documentazione

- [x] 4.1 README: «Aggiornare PostgreSQL», versioni, sviluppo in locale
- [x] 4.2 Collaudo: sezione 30
- [x] 4.3 Sprint 17 e backlog

## 5. Sul server (dopo il merge)

- [ ] 5.1 `prepara`, deploy di `main`, `verifica` (il PO, con sudo)
- [ ] 5.2 Controllo del mattino del giorno dopo: verde con il backup della notte sulla 19
- [ ] 5.3 PR di Dependabot #182–#184 chiuse
- [ ] 5.4 Dopo il passaggio in produzione: `pulisci`
