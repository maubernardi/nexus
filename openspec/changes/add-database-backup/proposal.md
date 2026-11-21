## Why

Storia **US-1302** (Sprint 16, PO 20/11). Prima di passare in produzione (US-1306) serve un backup che non dipenda dal
server: oggi i database di NEXUS e di Keycloak esistono solo sul suo disco. Il PO ha scelto IONOS Object Storage, con i
dati in Germania, a 0,008 € per GiB ogni 30 giorni (review dello Sprint 15, §8.3). Un backup vale solo se il ripristino
è stato provato: va fatto ora, sulla demo, dove non ci sono dati veri.

## What Changes

- **Backup ogni notte alle 3:30, ora italiana**, da un nuovo servizio `backup` nel compose del server. Arriva con il
  deploy: sul server non si installa nulla a mano.
  - Salva i due database, NEXUS e Keycloak, con lo stesso PostgreSQL del server.
  - Li chiude in un archivio con un indice dei contenuti e le impronte dei file.
  - Cifra l'archivio sul server con la chiave pubblica del PO (age). La chiave che lo apre resta solo al PO: IONOS e
    chi entrasse nel server non possono leggerlo.
  - Lo carica su IONOS Object Storage e controlla che sia arrivato intero.
- **Conservazione di 30 giorni**: i backup più vecchi si cancellano da soli, con una regola del contenitore impostata
  dal servizio.
- **Protezione**: il contenitore si crea con il blocco degli oggetti in modalità *compliance* per 30 giorni (PO,
  20/11). Per 30 giorni nessuno può cancellare un backup, nemmeno con le chiavi del server.
- **Esito**: il servizio scrive data, dimensione e nome dell'ultimo backup riuscito, o il passo fallito. Mai dati
  personali.
- **Controllo del mattino alle 9** (PO, 20/11): un workflow legge l'esito dal server. Se manca un backup riuscito nelle
  ultime 26 ore fallisce, e GitHub manda un'email al PO.
- **Su richiesta**, dalle Actions: lo stato dei backup, un backup subito e i log del servizio.
- **Ripristino di prova** sul PC del PO, con uno script: decifra, controlla le impronte, ripristina in un PostgreSQL
  temporaneo e conta le righe. Procedura nel README, anche per il ripristino sul server.
- **Prova automatica nella CI**: backup, caricamento su un servizio S3 di prova e ripristino, a ogni PR.

## Non-goals

- **Backup dei file fuori dal database**: non ce ne sono. Documenti e modelli stanno nel database (V32).
- **Ripristino automatico** o un secondo server pronto: il ripristino resta una procedura manuale documentata.
- **Backup più frequenti** di uno al giorno, o la conservazione oltre i 30 giorni.
- **Il passaggio in produzione**: è US-1306, che userà questo backup per tornare indietro.

## Capabilities

### New Capabilities
<!-- nessuna: il backup è parte della messa online del servizio -->

### Modified Capabilities
- `deployment`: nuovi requisiti per il backup notturno cifrato fuori dal server, la conservazione e la protezione, il
  controllo del mattino e il ripristino provato.

## Impact

- **Nuova immagine** `nexus-backup` (cartella `backup/`), costruita e pubblicata dal deploy come le altre:
  - PostgreSQL 18.6 Alpine, la stessa del server, con `age` e AWS CLI dai pacchetti di Alpine;
  - il controllo delle immagini (EN-6) e Dependabot la seguono.
- **Compose del server**: servizio `backup` e volume `backup-state`. Le variabili nuove nel `.env` le mette solo il
  PO: endpoint, regione, contenitore e chiavi di IONOS, chiave pubblica age. Senza di esse il servizio gira, ma
  scrive «non configurato».
- **`nexus-deploy`**: comandi in sola lettura `backup` e `logs backup`, più `backup adesso`. Il PO aggiorna una volta
  lo script sul server, con sudo.
- **Workflow**: nuovo «Backup del database», con il controllo delle 9 e l'avvio manuale. «Stato del server» legge
  anche i log del backup.
- **CI**: nuovo job «Backup» con la prova di backup e ripristino.
- **Nessuna migrazione**, nessun impatto su backend e frontend.
- **Dati personali**:
  - i backup contengono tutto il database, cifrato; restano fino a 30 giorni dopo una cancellazione o
    un'anonimizzazione (US-1303). Il PO lo scrive nel registro dei trattamenti, con il DPO;
  - nei log e nelle Actions vanno solo date, dimensioni e nomi dei file.
