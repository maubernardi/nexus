## Context

Vedi proposal.md, «Why». Sul server gira la demo:
- `nexus` con il profilo `seed`, che porta gli script dimostrativi `R__seed_*` e i comuni ISTAT;
- Keycloak con il realm importato da `nexus-realm.demo.json`;
- il backup notturno (US-1302).

Il database ha quattro tabelle in sola aggiunta, con trigger che rifiutano UPDATE, DELETE e TRUNCATE (EN-2):
`audit_event`, `company_audit_event`, `company_note`, `closure_report`. L'importazione del database attuale (US-1309) è
un'istanza del backend con il profilo `legacy-import` (analisi, §9.2).

Il ripristino di prova del 21/11 sul backup della demo ha contato:
- 7.896 comuni e 6 modelli dei documenti;
- 7 aziende, 10 segnalazioni e 11 beneficiari;
- 6 utenti nel realm `nexus`.

## Goals / Non-Goals

**Goals:** un passaggio fatto da uno script, provato in locale, con un punto a cui tornare. Dopo, NEXUS non ha più dati
dimostrativi e ha un solo ADMIN, che arriva con l'invito.

**Non-Goals:** il passaggio vero e la PR successiva (vedi proposal.md).

## Decisions

### D1. Pulizia: uno script SQL in una transazione

- **`infra/deploy/pulizia-demo.sql`**, lanciato con `psql` come `nexus_owner`, il proprietario dello schema: può
  sospendere i trigger, l'utente dell'app no.
- **`ALTER TABLE … DISABLE TRIGGER USER`** sulle quattro tabelle in sola aggiunta. In PostgreSQL è transazionale: se la
  transazione si annulla, i trigger restano attivi. Si riattivano prima del `COMMIT`.
- **`TRUNCATE` senza `CASCADE`** delle 25 tabelle di segnalazioni, beneficiari, utenti e aziende. Se una tabella che
  punta a queste mancasse dall'elenco, PostgreSQL si fermerebbe: non svuota niente di nascosto. Con un solo
  `TRUNCATE` si superano anche i riferimenti incrociati, per esempio segnalazione ↔ bacheca.
- **File**: via quelli che nessun modello dei documenti usa, cioè i curriculum.
- **Audit**: via le voci dei tipi `TICKET`, `BENEFICIARY`, `APP_USER`, `COMPANY`, `JOB_SLOT`, `BOARD_POST`; resta la
  storia della configurazione (PO, 20/11).
- **Flyway**: via le righe `R__seed_*`. Senza, il backend con il profilo predefinito valida le migrazioni; con, non
  partirebbe (`repeatable:missing` è ignorato solo nel profilo `seed`).
- **Controlli dentro la transazione**: tabelle vuote, niente audit degli oggetti tolti, niente `R__` nella cronologia,
  nessun trigger sospeso. Un controllo fallito annulla tutto.
- **Dopo il `COMMIT`**, solo numeri e codici:
  - le righe della configurazione;
  - le voci ancora come le aveva messe la demo: progetti e tipologie di mansione con autore e ultimo autore `seed`, enti
    con i codici fiscali o le email inventati.
- **Rilanciabile**: su un database già pulito non cambia nulla.

### D2. Utenti di Keycloak

- **`kcadm.sh`** dentro il container, come gli altri script `keycloak-*.sh`, con la password della console.
- **`GET users`** non restituisce gli account tecnici (provato su Keycloak 26.8.2): si cancellano tutti quelli
  elencati.
- **Poi due controlli**: `users/count` deve essere 0, e l'account tecnico di `nexus-backend-admin` deve esserci ancora.

### D3. Importazione del database attuale

- **Come nell'analisi, §9.2**:
  - `docker compose run --rm --no-deps` del backend con `PROFILES_ACTIVE=keycloak,legacy-import`;
  - il file in `/opt/nexus/import`, montato;
  - l'utente del container è il proprietario della cartella, così il resoconto è leggibile.
- Il log dell'importazione resta in `/opt/nexus/import/importazione.log`: contiene solo conteggi, ma non serve altrove.
- L'istanza dell'importazione non ha attività pianificate, quindi non crea l'ADMIN.

### D4. Primo ADMIN

- **`AdminBootstrapJob`**, attività pianificata:
  - parte 30 secondi dopo l'avvio;
  - riprova ogni minuto (`nexus.bootstrap.admin.initial-delay`, `retry-delay`);
  - dopo un esito definitivo si ferma.
- **Condizione**: nessun utente con il ruolo ADMIN, attivo o no. Un ADMIN disattivato apposta non va rimpiazzato.
- **Dati da `nexus.bootstrap.admin.*`**, cioè dalle variabili `NEXUS_ADMIN_USERNAME`, `NEXUS_ADMIN_FIRST_NAME`,
  `NEXUS_ADMIN_LAST_NAME` e `NEXUS_ADMIN_EMAIL` del `.env`:
  - vuoti in locale, nei test e sulla demo;
  - validati con le regole di `UserCreateDTO`, perché il servizio non passa dal controller.
- **`UserAdminService.create`**, lo stesso di «Utenti» (US-103):
  - crea l'account in Keycloak e l'utente in NEXUS;
  - scrive l'audit con l'autore `system`, perché non c'è un utente collegato;
  - manda l'invito.
- **Esiti**:
  - Keycloak non risponde (`ServiceUnavailableException`): si riprova al minuto successivo;
  - username o email già usati, o dati non validi: errore nel log, e ci si ferma;
  - invito non partito: un avviso nel log. Si rimedia con «Password dimenticata?» (US-1308): non c'è ancora nessuno
    che possa reinviarlo da «Utenti».
- **Nessun dato personale nel log**.

### D5. Realm di produzione

- **`infra/deploy/keycloak/nexus-realm.json`** = il realm della demo senza gli utenti di prova. Restano:
  - l'account tecnico;
  - ruoli, client, regole delle password, blocco, tema, posta e registro degli accessi.
- **Keycloak lo legge solo se il realm non esiste**: sulla demo, e dopo il passaggio, non cambia nulla. Serve se il suo
  database ripartisse vuoto, per non ricreare gli utenti di prova con le password note.
- **`nexus-deploy`** scarica questo file al posto di quello demo. Il PO lo reinstalla con sudo prima del passaggio.
  Finché non lo fa, scarica ancora il file demo, che resta nel repository fino alla seconda PR.
- **Le password demo nel compose diventano facoltative** (`:-`).
- **Nella CI** il job del tema importa il realm di produzione e controlla: zero utenti, account tecnico presente.

### D6. Script del passaggio

- **`infra/deploy/passaggio-in-produzione.sh`**, con sudo, accanto a `pulizia-demo.sql`. I due file si scaricano con
  `curl` dall'URL del commit.
- **Passi** come nel proposal. Controlli iniziali:
  - c'è il file Access;
  - ci sono i dati dell'ADMIN e del backup nel `.env`;
  - la release in esecuzione passa i dati dell'ADMIN al backend, cioè ha già questa change;
  - l'accesso a Keycloak riesce.
- **Conferma esplicita** («SI») prima di fermare il sito, e prima di cancellare il file Access.
- **Se si ferma dopo aver fermato il sito**, dice cosa fare:
  - entro la pulizia, che è una transazione, basta riavviare;
  - dopo la pulizia, si ripristina il backup del passo 2 (README, US-1302).
- **Alla fine**:
  - `NEXUS_BACKEND_PROFILES=keycloak` nel `.env`, così il backend parte senza `seed`;
  - `docker compose up -d --wait`;
  - il file `/opt/nexus/.passaggio-in-produzione` impedisce di rilanciare lo script.

### D7. Due tempi

- **Prima PR (questa)**: si può fare il deploy sulla demo, senza effetti. La demo ha già gli ADMIN, il realm esiste già
  e il profilo resta `keycloak,seed`.
- **Passaggio**: lo script, nel giorno scelto dal PO.
- **Seconda PR, dopo il passaggio**:
  - profilo predefinito `keycloak` nel compose;
  - via il realm demo, le password demo e la loro generazione in `bootstrap.sh`;
  - nomi «produzione» in workflow, README e collaudo;
  - controllo ASVS sulla produzione;
  - storia chiusa.

  Il profilo predefinito non cambia adesso: un deploy della demo prima del passaggio non partirebbe.

### D8. Prove

- **`DemoCleanupIT`**, con Testcontainers:
  - migrazioni e script dimostrativi;
  - un curriculum, un modello con il suo file e voci dell'audit di ogni tipo;
  - poi `psql -f pulizia-demo.sql` nel container.

  Controlla: tabelle vuote, configurazione uguale, file del modello rimasto, audit della sola configurazione, trigger
  di nuovo attivi, Flyway valido senza `seed` (e non valido prima), seconda esecuzione senza effetti, elenco delle voci
  dimostrative.
- **`AdminBootstrapJobIT`**, con Keycloak simulato:
  - creazione con invito e audit `system`;
  - niente se c'è un ADMIN, o con dati incompleti o non validi;
  - Keycloak giù, poi su.
- **Prova generale in locale (21/11)**:
  - demo ricostruita dal backend con il profilo `seed` (7.896 comuni, 6 aziende, 7 segnalazioni, 4 utenti);
  - pulizia;
  - importazione del file Access vero: 13.799 aziende, 38.789 chiamate, 7.786 referenti, come nel §9.3;
  - backend senza `seed`, con 49 migrazioni valide e l'ADMIN creato con l'invito.
- **Keycloak 26.8.2 in locale**: elenco e cancellazione degli utenti, account tecnico intatto, import del realm di
  produzione.
- **Da fare con il PO prima del passaggio**: la stessa prova generale sul backup vero della demo, ripristinato con
  `ripristino-di-prova.sh --tieni`.

## Risks / Trade-offs

- **[Una tabella nuova che punta agli utenti o alle aziende]**: il `TRUNCATE` senza `CASCADE` si ferma. → Va aggiunta
  allo script; `DemoCleanupIT` lo scopre a ogni PR.
- **[Invito dell'ADMIN non arrivato]**: nessuno può reinviarlo. → «Password dimenticata?» con la stessa email; avviso
  nel log del backend.
- **[Passaggio fermo a metà]**: per esempio l'importazione fallisce dopo la pulizia. → Backup del passo 2 e ripristino
  documentato e provato (US-1302); lo script dice cosa fare.
- **[Configurazione di prova creata durante il collaudo]**: passa in produzione. → L'elenco delle voci dimostrative
  aiuta; il resto lo controlla il PO (planning, «Per il PO»).

## Migration Plan

1. **Merge e deploy** di questa PR sulla demo: nessun effetto visibile.
2. **Prova generale** con il PO sul backup vero della demo.
3. **Prima del passaggio**, il PO:
   - finisce il collaudo;
   - mette i dati dell'ADMIN nel `.env`;
   - copia il file Access sul server;
   - reinstalla `nexus-deploy`.
4. **Passaggio**: `sudo bash passaggio-in-produzione.sh`. Poi l'invito, l'accesso dell'ADMIN e i controlli.
5. **Seconda PR** (D7).

**Ritorno indietro**: ripristino del backup del passo 2, poi `.env` senza `NEXUS_BACKEND_PROFILES=keycloak`.

## Open Questions

Nessuna. La data del passaggio la sceglie il PO dopo il collaudo (planning dello Sprint 16).
