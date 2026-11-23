## ADDED Requirements

### Requirement: Aggiornamento della versione principale di PostgreSQL
Il passaggio a una nuova versione principale di PostgreSQL SHALL spostare i dati con un backup e un ripristino, senza
perdite. I dati della nuova versione MUST stare in un volume nuovo, con la versione principale nel nome. Il volume della
versione precedente MUST restare intatto finché il PO non lo toglie. Uno script, lanciato dal PO con sudo prima del
deploy, SHALL:
1. fare un backup su IONOS;
2. copiare i due database;
3. ripristinarli nella nuova versione, sul volume nuovo;
4. confrontare righe per tabella, trigger, ultima migrazione e utenti di Keycloak.

Dopo il deploy, lo stesso script SHALL rifare il confronto e un backup. Nel terminale MUST comparire solo numeri e
codici, nessun dato personale.

#### Scenario: Aggiornamento riuscito
- **WHEN** il PO lancia lo script sulla versione precedente, conferma, e poi si fa il deploy della nuova
- **THEN** la nuova versione parte sul volume già ripristinato, con le stesse righe, gli stessi trigger e la stessa
  ultima migrazione, e backend e Keycloak non partono mai su un database vuoto

#### Scenario: Ripristino con numeri diversi
- **WHEN** dopo il ripristino le righe, i trigger o gli utenti di Keycloak non coincidono con l'origine
- **THEN** lo script si ferma, toglie il volume nuovo e spiega come riavviare il sito sulla versione precedente, che non
  è cambiata

#### Scenario: Ritorno indietro
- **WHEN** dopo l'aggiornamento si fa il deploy del commit precedente
- **THEN** la versione precedente riparte dal suo volume con i dati di prima dell'aggiornamento

#### Scenario: Deploy durante lo script
- **WHEN** parte un deploy mentre lo script copia o ripristina i dati
- **THEN** il deploy si ferma con «un altro deploy è in corso» e non avvia niente

#### Scenario: Pulizia
- **WHEN** il PO lancia la pulizia con la nuova versione in esecuzione sul volume nuovo
- **THEN** dopo la sua conferma il volume della versione precedente è cancellato; se la nuova versione non è quella in
  esecuzione, la pulizia si rifiuta

## MODIFIED Requirements

### Requirement: Dipendenze aggiornate
Le dipendenze del frontend, del backend, delle immagini e della CI SHALL essere all'ultima versione stabile; Java e Node
MUST essere all'ultima LTS. Gli aggiornamenti SHALL arrivare con le PR di Dependabot, una a settimana per ecosistema per
le versioni minori e una per ogni versione principale, e passare dalla CI prima del merge del PO. Le vulnerabilità
segnalate SHALL essere chiuse aggiornando; un avviso senza correzione esistente MAY essere archiviato con il motivo e
l'ok del PO. Le immagini usate dai test, dal backup, dal ripristino di prova e dallo script di aggiornamento MUST essere
le stesse della produzione. Il volume dei dati di PostgreSQL MUST avere nel nome la versione principale dell'immagine.

#### Scenario: Aggiornamento settimanale
- **WHEN** esce una versione minore di una dipendenza del frontend
- **THEN** il lunedì successivo Dependabot apre una PR con tutte le minori del frontend, che il PO unisce dopo la CI verde

#### Scenario: Versione principale
- **WHEN** esce una nuova versione principale di una dipendenza
- **THEN** Dependabot apre una PR solo per quella; per Java e per i tipi di Node non la apre, perché seguono la LTS

#### Scenario: Immagini dei test diverse dalla produzione
- **WHEN** l'immagine di PostgreSQL o di Keycloak della produzione cambia e quella dei test no
- **THEN** la CI fallisce indicando il file da aggiornare

#### Scenario: Nuova versione principale di PostgreSQL senza volume nuovo
- **WHEN** una PR porta PostgreSQL a una nuova versione principale e lascia il volume con il nome della precedente
- **THEN** la CI fallisce e rimanda alla procedura di aggiornamento del README
