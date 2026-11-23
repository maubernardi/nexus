## Context

Sul server PostgreSQL 18.6 tiene i dati nel volume `postgres-data`, montato su `/var/lib/postgresql`. Dalla 18 le
immagini ufficiali usano una cartella per versione (`18/docker`, `19/docker`). Prova in locale del 23/11: la 19, sullo
stesso volume, **non parte**. Trova i dati della 18 e si ferma, e chiede `pg_upgrade`. Le PR di Dependabot avrebbero
quindi fatto fallire il deploy, e `nexus-deploy` sarebbe tornato da solo alla release precedente.

I database sono piccoli: la demo ha qualche decina di MB. Backup e ripristino con `pg_dump` e `pg_restore` sono già
provati con US-1302, sul backup vero, due volte.

## Goals / Non-Goals

**Goals:**
- passare alla 19 senza perdere dati;
- ritorno indietro semplice, con i dati della 18 intatti;
- backend e Keycloak che non partono mai su un database vuoto;
- una procedura che il PO lancia con un comando, e che serve anche per la versione principale successiva.

**Non-Goals:**
- l'aggiornamento senza fermare il sito: è la demo, bastano pochi minuti di fermo;
- `pg_upgrade`.

## Decisions

### D1. Volume nuovo per versione principale, con backup e ripristino
La 19 usa un volume nuovo, `postgres19-data`, che porta la versione principale nel nome.

Alternativa scartata: `pg_upgrade`. Vuole i programmi delle due versioni nella stessa immagine, e quindi un'immagine
non ufficiale. In modalità `--link` la 18 non si può più riavviare; in copia occupa lo stesso spazio di un volume nuovo.
Backup e ripristino invece sono la procedura che conosciamo già, e con database così piccoli durano pochi secondi.

### D2. Ripristino prima del deploy, in una 19 temporanea
Lo script crea il volume nuovo e ci avvia una `postgres:19.0-alpine` temporanea:
- con lo stesso script dei ruoli e le password del `.env`;
- senza rete (`--network none`): si lavora con `docker exec`.

Ci ripristina i due database, come il ripristino del README (`ricrea-database.sql`, poi `pg_restore`), poi la spegne.
Il deploy trova il volume pronto: la 19 non rifà il primo avvio, e backend e Keycloak ritrovano i loro dati.

Alternativa scartata: ripristino dopo il deploy. Per qualche minuto backend e Keycloak girerebbero su database vuoti:
- Flyway e i dati dimostrativi su `nexus`;
- l'importazione del realm su `keycloak`;
- con i dati dell'ADMIN nel `.env`, il backend ne creerebbe uno e manderebbe un invito destinato a sparire.

### D3. Nessun deploy durante lo script
Lo script prende lo stesso blocco di `nexus-deploy` (`/opt/nexus/.deploy.lock`). Un deploy lanciato a metà si ferma con
«un altro deploy è in corso». Così due PostgreSQL non lavorano mai sullo stesso volume.

### D4. Il volume nuovo è «di compose»
Il volume si crea con le etichette di compose: progetto e nome del volume, presi dal container di PostgreSQL in
esecuzione. Compose lo adotta senza avvisi, e senza l'etichetta della configurazione non chiede di ricrearlo. Provato
in locale con Docker Compose v5.6.1; la prova della CI lo rifà a ogni PR che tocca lo script.

### D5. Solo numeri, e le copie in chiaro durano lo stretto necessario
Il confronto usa:
- le righe per tabella (`conteggi.sql`);
- i trigger dello schema `nexus`;
- l'ultima migrazione;
- gli utenti di Keycloak per realm.

Il riepilogo, solo numeri, resta in `/opt/nexus/aggiornamento-postgresql/` (cartella di root, 700) per la `verifica`.
Le copie dei database stanno nella stessa cartella e si cancellano alla fine, sia se riesce sia se fallisce.

### D6. Ritorno indietro e pulizia
Se il deploy della 19 non parte, `nexus-deploy` torna da solo alla release precedente, cioè alla 18 sul suo volume.
Dopo, il deploy del commit precedente riparte dalla 18, ma con i dati di prima dell'aggiornamento: il ritorno ha senso
solo finché nessuno ha lavorato sulla 19.

Con il passaggio in produzione i dati della 18 diventano inutili: `pulisci` toglie il volume, con la conferma del PO.
Prima controlla che la 19 sia in esecuzione sul volume nuovo.

### D7. Prove
- **CI** (`tools/ci/aggiorna-postgresql-e2e.sh`, job «Backup e ripristino»). Uno stack ridotto con:
  - lo stesso servizio `postgres` e gli stessi volumi del server;
  - servizi finti al posto di backend, Keycloak, sito e backup.

  Prova:
  - il rifiuto senza «SI»;
  - l'aggiornamento e il deploy;
  - la verifica e il ritorno alla 18;
  - la pulizia.
- **In locale sul backup vero**: `ripristino-di-prova.sh`, ora con la 19, sull'ultimo archivio della demo. Poi backend
  e Keycloak avviati sul database ripristinato.
- **Test Java**: tutti i Testcontainers sulla 19.

### D8. Controllo delle immagini (EN-6)
Il controllo ora copre:
- tutte le stringhe `postgres:*` dei test, non solo due;
- lo script dell'aggiornamento;
- il nome del volume nei due compose, che deve essere `postgres<versione principale>-data`.

Una futura PR di Dependabot per la 20 fallisce così con un messaggio che rimanda al README.

## Risks / Trade-offs

- **Keycloak 26.8 sulla 19.** Keycloak usa il dialetto di Hibernate per PostgreSQL e non controlla la versione. Lo
  provano la prova in locale e il deploy: se Keycloak non diventa sano, `nexus-deploy` torna alla 18.
- **Flyway sulla 19.** Avvisa che la 19 è più nuova dell'ultima versione con cui è stato verificato (la 18), ma valida
  e applica le migrazioni: tutti i test del backend passano sulla 19. L'avviso sparirà con un aggiornamento di Flyway,
  che arriva con Dependabot.
- **Fermo del sito.** Dura dallo script alla fine del deploy, circa 10–15 minuti: la parte più lunga è la build delle
  immagini nella pipeline.
- **Spazio su disco.** Fino alla pulizia ci sono due volumi e, per pochi secondi, le copie. Lo script controlla lo
  spazio libero prima di cominciare.
- **Sviluppo in locale.** Il database riparte vuoto sul volume nuovo; i dati dimostrativi si ricaricano da soli.

## Migration Plan

1. PR con la CI verde, merge del PO.
2. Prova sul backup vero in locale (il PO, con la sua chiave).
3. Sul server: script con sudo, poi il deploy di `main`, poi `verifica`.
4. Dependabot chiude da solo le PR #182–#184, ora superate.
5. Dopo il passaggio in produzione: `pulisci`.
