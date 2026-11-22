## Why

Storia **US-1306** (Sprint 16). NEXUS passa dalla demo alla produzione sullo stesso server. Decisione del PO del
20/11: si riporta tutto dalla demo tranne segnalazioni, utenti e beneficiari, e le aziende vengono dal database attuale
(US-1309). Il backup con il ripristino provato (US-1302) è il punto a cui tornare se il passaggio non riesce.

Questa change prepara il passaggio: lo script, la pulizia e il primo ADMIN, provati in locale. Il passaggio vero si fa
sul server nel giorno che sceglie il PO. La pulizia finale (nomi «produzione», realm demo, profilo predefinito) arriva
con una seconda PR, dopo il passaggio.

## What Changes

- **Pulizia della demo** (`infra/deploy/pulizia-demo.sql`), in una sola transazione. Toglie:
  - le segnalazioni con bacheca, schede del tirocinio, report, storico e notifiche;
  - i beneficiari con i curriculum;
  - gli utenti;
  - le aziende della demo con referenti, diario, storico e mansioni;
  - le voci dell'audit di tutto questo;
  - le righe degli script dimostrativi nella cronologia di Flyway.

  Le regole di sola aggiunta (EN-2) sono sospese solo dentro la transazione. Restano configurazione e cataloghi.
  Alla fine lo script elenca le voci rimaste come le aveva messe la demo, da controllare.
- **Primo ADMIN della produzione**: se in NEXUS non c'è nessun ADMIN e nel `.env` ci sono i suoi dati (`NEXUS_ADMIN_*`),
  il backend lo crea come da «Utenti» (US-103), con l'invito per email. Riprova finché Keycloak non risponde.
- **Realm di produzione** (`infra/deploy/keycloak/nexus-realm.json`): quello della demo senza utenti di prova, con solo
  l'account tecnico del backend. Lo scarica lo script di deploy; le password demo nel compose diventano facoltative.
- **Script del passaggio** (`infra/deploy/passaggio-in-produzione.sh`), lanciato dal PO con sudo. Passi:
  1. controlli;
  2. backup della demo;
  3. fermo del sito;
  4. pulizia;
  5. utenti di Keycloak;
  6. importazione del database attuale;
  7. `.env` senza dati dimostrativi e riavvio;
  8. primo backup della produzione e cancellazione del file Access.

  Chiede conferma prima di toccare i dati, e se si ferma dice come tornare alla demo.
- **Procedura** nel README, «Passaggio in produzione», con quello che fa il PO prima, durante e dopo.

## Non-goals

- **Il passaggio vero**: si fa sul server, con il PO, dopo il collaudo.
- **Nomi «demo» → «produzione»**, profilo predefinito senza `seed`, rimozione del realm e delle password demo, controllo
  di sicurezza ASVS sulla produzione: nella seconda PR di US-1306, dopo il passaggio.
- **Pulizia della configurazione dimostrativa** (tipologie di mansione segnaposto, dati inventati degli enti): la fa il
  PO dai Cataloghi, con l'elenco stampato dalla pulizia (PO, 20/11: si riporta tutto).
- **Account degli utenti**: li crea il PO, da ADMIN (review dello Sprint 15, §8.4).

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `deployment`: nuovo requisito per il passaggio dalla demo alla produzione.
- `user-management`: nuovo requisito per il primo ADMIN della produzione.

## Impact

- **Nessuna migrazione** (la prossima resta V50).
- **Backend**:
  - `AdminBootstrapJob`, attività pianificata;
  - proprietà `nexus.bootstrap.admin.*`;
  - `AppUserRepository.existsByRole`.
- **Infra**:
  - `pulizia-demo.sql`, `passaggio-in-produzione.sh`, `keycloak/nexus-realm.json`;
  - compose: dati dell'ADMIN al backend, password demo facoltative;
  - `nexus-deploy`: scarica il realm di produzione. Il PO lo reinstalla con sudo prima del passaggio.
- **CI**: il job del tema di Keycloak importa anche il realm di produzione e controlla che non abbia utenti.
- **Test**:
  - `DemoCleanupIT`: la pulizia sullo schema vero con i dati dimostrativi;
  - `AdminBootstrapJobIT`: il primo ADMIN.
- **Frontend**: nessun impatto.
- **Dati personali**:
  - la pulizia e lo script stampano solo numeri e codici della configurazione;
  - il file Access si cancella dal server dopo il controllo del resoconto.
