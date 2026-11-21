# NEXUS

Progressive Web App multi-progetto per l'**Area Lavoro** e il **Call Center Sociale** di una Cooperativa
Sociale: segnalazioni dei tirocinanti, coda FIFO, matching con le aziende, bacheca opportunità, avvio dei
tirocini e generazione documentale.

- **Tutor territoriali**: uso mobile-first (PWA installabile).
- **Operatori Call Center**: uso da desktop.

Il brief funzionale completo è in [`CLAUDE_CODE_PROMPT.md`](CLAUDE_CODE_PROMPT.md). Le specifiche vive stanno in
[`openspec/`](openspec/).

## Architettura

```
nexus/
├── backend/    Spring Boot 4.1 · Java 25 LTS · PostgreSQL · Flyway · Spring Security 7 (Keycloak JWT)
├── frontend/   React 19.3 · React Router 8 · Vite 8 · TypeScript 6 · Tailwind v4 + shadcn/ui · PWA (Workbox)
├── infra/      docker-compose: PostgreSQL 18 + Keycloak 26.7 (realm "nexus" importato)
├── keycloak/   tema di accesso "nexus" (aspetto del sito, solo italiano) e immagine nexus-keycloak
└── openspec/   specifiche e change proposal (spec-driven development)
```

| Area | Scelte principali |
|---|---|
| API | REST `/api/v1`, OpenAPI su `/swagger-ui.html`, errori in formato uniforme (`timestamp, status, error, message, path, fieldErrors`) |
| Sicurezza | Deny by default, ruoli `TUTOR`, `CALL_CENTER`, `ADMIN`; JWT Keycloak oppure header `X-USER-ID` (solo sviluppo) |
| Dati | Schema gestito solo da Flyway (`ddl-auto: validate`) |
| PWA | Installabile, shell offline, aggiornamento su conferma dell'utente, **nessun dato personale in cache** |
| Accessibilità | WCAG 2.2 AA: eslint-plugin-jsx-a11y + axe-core nei test, token colore verificati, skip link, focus management |

## Prerequisiti

- **JDK 25** (LTS, es. Temurin 25.0.5). Il wrapper Maven (3.10.1) è incluso: la build accetta Maven 3.10.x e rifiuta
  altre versioni di JDK
- **Node.js 26.13.0** (LTS, vedi `.nvmrc`: `nvm use`) e **pnpm 12.18.0** (`npm i -g pnpm@12.18.0`); pnpm rifiuta altre versioni di Node
- Docker

## Avvio in locale

```bash
# 1. Infrastruttura
cd infra && docker compose up -d        # Postgres :5432, Keycloak :8180 (admin/admin)
#    se avevi un volume creato con PostgreSQL 17: docker compose down -v (dati solo di sviluppo)

# 2. Backend (profilo "local" = Postgres + sicurezza mock)
cd backend && ./mvnw spring-boot:run    # http://localhost:8080

# 3. Frontend
cd frontend && pnpm install && pnpm dev # http://localhost:5173
```

In modalità mock il frontend mostra la pagina **Accesso di sviluppo**, dove si sceglie l'utente.

Il profilo `local` carica anche i **dati dimostrativi** (`backend/src/main/resources/db/seed`): progetti GOL e POLIS,
gli utenti di test, zone e tipologie di mansione segnaposto, aziende e mansioni fittizie, beneficiari "Demo", 7 ticket
in stati diversi e 3 post di bacheca. Gli script sono idempotenti; per ripartire da zero:
`docker compose -f infra/docker-compose.yml exec postgres psql -U nexus -d nexus -c 'DROP SCHEMA nexus CASCADE'`.

### Usare Keycloak reale

```bash
cd backend && PROFILES_ACTIVE=keycloak,seed ./mvnw spring-boot:run
cd frontend && VITE_AUTH_MODE=keycloak pnpm dev
```

Il cambio dell'indirizzo email (US-110) aggiorna anche Keycloak tramite il client tecnico `nexus-backend-admin` del
realm di sviluppo: per provarlo con Keycloak reale avviare il backend con
`KEYCLOAK_ADMIN_CLIENT_SECRET=nexus-backend-admin-dev-secret`. Con gli utenti mock le email sono spente e il codice di
conferma compare nel log del backend.

La pagina di accesso usa il tema `keycloak/themes/nexus`, montato nel Keycloak di sviluppo: in `start-dev` la cache dei
temi è spenta, quindi le modifiche a template, CSS e testi si vedono ricaricando la pagina.

### Utenti di test

La password è la stessa per tutti in Keycloak: `password`. Per accedere un utente deve esistere anche in NEXUS
(tabella `app_user`, creata dal seed): gli utenti autenticati ma non censiti o disattivati ricevono 403.

| Username | Ruolo |
|---|---|
| `tutor1`, `tutor2` | TUTOR |
| `operatore.cc` | CALL_CENTER |
| `admin` | ADMIN |

## Modello dati

Schema PostgreSQL `nexus`, gestito solo da migrazioni Flyway (`backend/src/main/resources/db/migration`).
Diagramma ER, tabelle e motivazioni: [`openspec/changes/archive/2026-09-28-add-domain-model/design.md`](openspec/changes/archive/2026-09-28-add-domain-model/design.md);
requisiti in `openspec/specs/domain-model`.

| Area | Tabelle |
|---|---|
| Riferimento | `project`, `zone`, `job_category`, `stored_file` |
| Utenti | `app_user` (censiti da NEXUS, ruolo in copia da Keycloak), `user_project` |
| Beneficiari | `beneficiary`, `beneficiary_language` |
| Aziende | `company`, `job_slot`, `company_audit_event` (in sola aggiunta, imposto da trigger) |
| Segnalazioni | `ticket`, `ticket_status_history`, `ticket_company_blacklist`, `board_post` |

- **Identificativi**: chiave primaria TSID (64 bit), esposta nelle API come stringa di 13 caratteri; ticket e post hanno
  anche un numero progressivo leggibile.
- **Integrità nel database**: vincoli `CHECK`, unicità, chiavi esterne e trigger, verificati da `DomainConstraintsIT`.
- **Audit e concorrenza**: `created/updated at/by` su tutte le tabelle; blocco ottimistico su ticket, mansioni e post.
- **GDPR**: i beneficiari contengono dati di categoria particolare. Accesso controllato, nessun dato personale nei log,
  `anonymized_at` predisposto per l'anonimizzazione.

## Profili backend

| `PROFILES_ACTIVE` | Composizione | Autenticazione |
|---|---|---|
| `local` (default) | postgresql, security-mock, seed | header `X-USER-ID` |
| `keycloak` | postgresql, keycloak (in locale aggiungere `seed`) | JWT Keycloak |
| `test` | Testcontainers, security-mock (senza seed) | header `X-USER-ID` |

Variabili principali: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `KEYCLOAK_ISSUER_URI`, `KEYCLOAK_AUDIENCE`,
`CORS_ALLOWED_ORIGINS`, `OPENAPI_ENABLED`.

## Versioni congelate

Dall'08/11/2026 le versioni sono **sempre le più aggiornate possibile** (decisione del PO, EN-6): ultime stabili, LTS
per Java, Node e Ubuntu; versioni esatte in `pom.xml`, `package.json` e tag Docker; action della CI fissate per SHA.
Gli aggiornamenti arrivano con le PR di Dependabot, le versioni principali in PR separate. Il congelamento iniziale del
2026-09-27 è in
[`openspec/changes/archive/2026-09-27-upgrade-latest-versions/design.md`](openspec/changes/archive/2026-09-27-upgrade-latest-versions/design.md).

| Java | Spring Boot | Node | pnpm | React | Vite | TypeScript | PostgreSQL | Keycloak |
|---|---|---|---|---|---|---|---|---|
| 25.0.5 | 4.1.2 | 26.13.0 | 12.18.0 | 19.3.1 | 8.3.6 | 7.0.2 (API 6.0 per typescript-eslint) | 18.6 | 26.8.2 |

## Demo online

La demo con dati fittizi è pubblicata su **https://portalenexus.it** (Keycloak su `https://auth.portalenexus.it`).
Architettura e decisioni: [`openspec/changes/archive/2026-09-28-add-demo-deployment/design.md`](openspec/changes/archive/2026-09-28-add-demo-deployment/design.md).

| Cosa | Dove |
|---|---|
| Stack (Caddy, backend, Keycloak, PostgreSQL) | `infra/deploy/compose.yaml` |
| Immagini | `backend/Dockerfile`, `frontend/Dockerfile` + `frontend/Caddyfile`, `keycloak/Dockerfile` (tema di accesso), `backup/Dockerfile` → `ghcr.io/maubernardi/nexus-{backend,web,keycloak,backup}` |
| Preparazione del server (una volta) | `infra/deploy/bootstrap.sh` |
| Deploy | GitHub → Actions → **Deploy demo** → *Run workflow* (`.github/workflows/deploy-demo.yml`) |
| Backup | ogni notte alle 3:30; esito e backup subito: Actions → **Backup del database** ([sotto](#backup-del-database)) |

- **Deploy su comando.** Il workflow costruisce le immagini del commit scelto, le pubblica su GHCR e le installa sul
  server con una chiave SSH che può solo eseguire `nexus-deploy`. Se l'avvio fallisce, torna alla release precedente.
- **Immagine nuova.** Il primo caricamento di un'immagine mai pubblicata può essere rifiutato da GHCR (`denied`): il
  pacchetto viene comunque creato e collegato al repository, quindi basta rilanciare il job fallito (successo con
  `nexus-keycloak` il 29/10). Il server non viene toccato finché il build non riesce.
- **Segreti** in `/opt/nexus/.env` sul server (generati da `bootstrap.sh`, mai in git); le credenziali demo si
  rileggono con `sudo cat /opt/nexus/.env`.
- **Console di Keycloak** non esposta su Internet: `ssh -L 8081:127.0.0.1:8081 <server>`, poi
  `http://localhost:8081/admin` (utente `admin`, password `KEYCLOAK_ADMIN_PASSWORD` del `.env`).
- **Tema e lingua del realm (una volta, dopo il primo deploy con il tema).** L'import all'avvio non modifica un realm
  già esistente: sulla demo tema `nexus` e sola lingua italiana si impostano con
  ```bash
  cd /opt/nexus/current && sudo docker compose --env-file ../.env --env-file release.env exec -T keycloak bash -c \
    '/opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:8080 --realm master --user admin \
       --password "$KC_BOOTSTRAP_ADMIN_PASSWORD" --config /tmp/kcadm.config \
     && /opt/keycloak/bin/kcadm.sh update realms/nexus --config /tmp/kcadm.config -s loginTheme=nexus \
       -s internationalizationEnabled=true -s "supportedLocales=[\"it\"]" -s defaultLocale=it; rm -f /tmp/kcadm.config'
  ```
  oppure dalla console: *Realm settings → Themes → Login theme* `nexus`; *Localization → Supported locales* solo
  Italiano. Utenti e client non cambiano.
- **Email delle notifiche (US-1202).** Ogni notifica in app arriva anche via email, inviata da
  `noreply@portalenexus.it` tramite `smtp.ionos.it:587` con l'utente `admin@portalenexus.it`. La password non sta nel
  repository: si aggiunge a mano nel `.env` del server, poi si rifà il deploy (o si riavvia il backend):
  ```bash
  sudo nano /opt/nexus/.env      # aggiungere la riga NEXUS_SMTP_PASSWORD=<password della casella admin@>
  ```
  Senza la password le email restano spente e il log del backend lo dice all'avvio. Le email vanno all'indirizzo
  dell'anagrafica utenti di NEXUS (`app_user.email`): gli utenti demo hanno indirizzi finti `@nexus.local`, che non le
  ricevono (l'errore resta nel log, l'app continua). Per una prova si dà un indirizzo vero a un utente demo dal suo
  profilo (US-110, punto seguente).
- **Cambio dell'indirizzo email (US-110).** Ogni utente lo cambia da «Il mio profilo» (il proprio nome
  nell'intestazione) con un codice inviato al nuovo indirizzo; l'indirizzo cambia in NEXUS e in Keycloak. Il backend
  usa il client tecnico `nexus-backend-admin` (solo `view-users` e `manage-users`), con il segreto
  `NEXUS_KEYCLOAK_ADMIN_CLIENT_SECRET` nel `.env`. Sulle nuove installazioni lo creano `bootstrap.sh` e l'import del
  realm; sul server già installato, **una volta, dopo il deploy di US-110**:
  ```bash
  curl -fsSLO https://raw.githubusercontent.com/maubernardi/nexus/main/infra/deploy/keycloak-backend-client.sh
  sudo bash keycloak-backend-client.sh   # genera il segreto nel .env, crea il client, riavvia il backend
  ```
  Finché manca, il cambio risponde «non disponibile» e il log del backend lo segnala all'avvio; il resto funziona.
- **Utenti creati da NEXUS (US-103).** L'ADMIN crea gli utenti da «Utenti»; Keycloak invia loro l'email per
  impostare la password, con lo stesso SMTP IONOS delle notifiche. Sulle nuove installazioni il server di posta è nel
  realm importato (password da `NEXUS_SMTP_PASSWORD`); sul server già installato, **una volta, dopo il deploy di
  US-103** (e di nuovo se cambia la password SMTP):
  ```bash
  curl -fsSLO https://raw.githubusercontent.com/maubernardi/nexus/main/infra/deploy/keycloak-email.sh
  sudo bash keycloak-email.sh   # legge NEXUS_SMTP_PASSWORD dal .env e imposta la posta del realm nexus
  ```
  Finché manca, gli utenti si creano ma l'invito non parte: l'elenco lo dice e propone «Invia di nuovo l'invito».
- **Recupero della password (US-1308).** «Password dimenticata?» nella pagina di accesso: Keycloak invia un
  collegamento valido 30 minuti, con la stessa posta. Sulle nuove installazioni è nel realm importato; sul server già
  installato, **una volta, dopo `keycloak-email.sh`**:
  ```bash
  curl -fsSLO https://raw.githubusercontent.com/maubernardi/nexus/main/infra/deploy/keycloak-password-reset.sh
  sudo bash keycloak-password-reset.sh   # nessun segreto: recupero, validità di 30 minuti, email di sicurezza
  ```
  A ogni cambio di password Keycloak manda all'utente un'email di sicurezza; il filtro sugli eventi è nell'immagine
  `nexus-keycloak`, quindi lo script va eseguito **dopo** il deploy (prima scriverebbe anche a ogni password sbagliata).
- **Password comuni (EN-7).** Keycloak rifiuta le password tra le più usate (elenco in
  `keycloak/password-blacklists/`, rigenerato con `tools/keycloak/update-password-blacklist.sh`). Sulle nuove
  installazioni la policy è nel realm importato; sul server già installato, **una volta, dopo il deploy di EN-7**:
  ```bash
  curl -fsSLO https://raw.githubusercontent.com/maubernardi/nexus/main/infra/deploy/keycloak-password-policy.sh
  sudo bash keycloak-password-policy.sh   # nessun segreto
  ```
- **Keycloak 26.8 (EN-6).** Da questa versione l'API di amministrazione accetta solo token con lo scope `kc-admin-api`,
  che va dato al client tecnico `nexus-backend-admin` (nei realm importati c'è già). Sul server già installato, **una
  volta, subito dopo il deploy di Keycloak 26.8**:
  ```bash
  curl -fsSLO https://raw.githubusercontent.com/maubernardi/nexus/main/infra/deploy/keycloak-admin-api-scope.sh
  sudo bash keycloak-admin-api-scope.sh   # nessun segreto
  ```
  Finché manca, creazione degli utenti, cambio email, disattivazione e registro degli accessi sono «non disponibili».
- **Registro degli accessi (US-113).** Keycloak registra gli accessi riusciti e falliti (data, ora, IP) e li cancella
  dopo 12 mesi; NEXUS li mostra in «Utenti → Accessi», «Accessi non riusciti» e nel profilo. Il client tecnico
  `nexus-backend-admin` ha anche `view-events`. Sul server già installato, **una volta, dopo il deploy di US-113**:
  ```bash
  curl -fsSLO https://raw.githubusercontent.com/maubernardi/nexus/main/infra/deploy/keycloak-access-log.sh
  sudo bash keycloak-access-log.sh   # nessun segreto: attiva il registro e il permesso di lettura
  ```
  Finché manca, le pagine dicono che il registro non è disponibile.
- **Soggetto promotore e variabili nei documenti (US-1013).** I dati della cooperativa che promuove i tirocini
  (denominazione, sede, codice fiscale, legale rappresentante, contatti, INAIL, polizza RC) stanno nel database, **per
  progetto**: li inserisce l'ADMIN in «Cataloghi» → «Dati dei documenti dei progetti», con intestazione e avviso. Lì
  si aggiungono anche le variabili libere del progetto (es. `SEDE_OPERATIVA`), che diventano segnaposto
  (`{SEDE_OPERATIVA}`) dei modelli di quel progetto. Nel `.env` non serve niente: le variabili `NEXUS_PROMOTER_*` di
  US-1011 non ci sono più. Un dato che manca resta, nei documenti, una riga da completare a mano.
- **Modelli reali di GOL (EN-8).** I 6 modelli della Regione (Extracurriculare: convenzione, progetto formativo,
  relazione finale; Inclusione: Allegati A, B e C) non stanno nel repository: si preparano in
  `documenti_base/modelli-nexus/` dai file della cooperativa e li carica l'ADMIN da «Modelli dei documenti», con
  progetto GOL, tipo di tirocinio e nessun ente (un modello per tutti gli enti). Per cambiarli si modifica il `.docx`
  in Word e lo si ricarica: i segnaposto ammessi sono quelli elencati nella pagina.
- **Stato e log sul server:**
  `cd /opt/nexus/current && sudo docker compose --env-file ../.env --env-file release.env ps` (oppure `logs -f backend`).
- **DNS (IONOS):** A `@`, `auth`, `www` → IP del server; CAA `0 issue "letsencrypt.org"`; i record email restano.

### Backup del database

US-1302 ([design](openspec/changes/add-database-backup/design.md)). Il servizio `backup` del compose (`backup/`):
- ogni notte alle **3:30**, ora italiana, salva i database `nexus` e `keycloak`;
- li cifra sul server con la **chiave pubblica age del PO**: la chiave privata non è mai sul server, quindi né IONOS né
  chi entrasse nel server può leggere i backup;
- li carica su **IONOS Object Storage**, in Germania. Restano 30 giorni, protetti dal blocco degli oggetti.

Ogni mattina alle **9** il workflow «Backup del database» legge l'esito. Se manca un backup riuscito nelle ultime 26
ore, fallisce e GitHub manda un'email. Dallo stesso workflow, a mano: «stato» o «backup adesso». I log del servizio
sono in «Stato del server», servizio `backup`.

**Preparazione (una volta, il PO):**
1. **Contenitore su IONOS** (DCD → *Storage* → *Object Storage* → *Create bucket*):
   - regione in Germania;
   - **Object Lock attivo, modalità *Governance*, 30 giorni**: si può scegliere solo adesso, alla creazione, poi non
     si aggiunge più.

   Poi, in *Access keys*, una chiave nuova: chiave e segreto vanno nel password manager.
2. **Chiavi di cifratura**, sul PC (WSL):
   ```bash
   sudo apt install age
   age-keygen -o ~/nexus-backup-chiave.txt && chmod 600 ~/nexus-backup-chiave.txt   # stampa «Public key: age1…»
   ```
   Il contenuto del file è la **chiave privata**: va nel password manager e in una copia offline (chiavetta o carta).
   **Senza, i backup non si aprono più.** Sul server va solo la chiave pubblica `age1…`.
3. **Variabili sul server**: `sudo nano /opt/nexus/.env`, in fondo:
   ```bash
   NEXUS_BACKUP_S3_ENDPOINT=https://s3.<regione>.ionoscloud.com   # es. https://s3.eu-central-3.ionoscloud.com
   NEXUS_BACKUP_S3_REGION=<regione>                               # es. eu-central-3
   NEXUS_BACKUP_S3_BUCKET=<nome del contenitore>
   NEXUS_BACKUP_S3_ACCESS_KEY=<chiave di IONOS>
   NEXUS_BACKUP_S3_SECRET_KEY=<segreto di IONOS>
   NEXUS_BACKUP_AGE_RECIPIENT=age1...                             # la chiave PUBBLICA
   ```
4. **Script di deploy aggiornato** (non si aggiorna da solo: è il forced command della chiave di deploy):
   ```bash
   sudo curl -fsSL https://raw.githubusercontent.com/maubernardi/nexus/main/infra/deploy/nexus-deploy \
     -o /usr/local/bin/nexus-deploy && sudo chmod 755 /usr/local/bin/nexus-deploy
   ```
5. **Deploy** di `main` (il servizio rilegge il `.env`), poi Actions → «Backup del database» → **backup adesso**.
   - L'esito deve dire `OK` e «Blocco: GOVERNANCE fino al …».
   - Se compare l'avviso sulla **regola di conservazione**, IONOS non ha accettato quella del servizio. Va messa dalla
     console, nel contenitore, in *Lifecycle*: scadenza dopo 30 giorni, versioni non correnti cancellate dopo 1 giorno.

**Ripristino di prova** (sul PC; obbligatorio la prima volta, poi ogni tanto):
1. Scaricare l'ultimo archivio dalla console di IONOS (cartella `nexus/AAAA/MM/`).
2. Lanciare lo script (da WSL i download di Windows sono in `/mnt/c/Users/<utente>/Downloads/`):
   ```bash
   backup/ripristino-di-prova.sh <archivio scaricato>.tar.age ~/nexus-backup-chiave.txt
   ```
   Lo script decifra e controlla le impronte. Poi ripristina in un PostgreSQL temporaneo con la stessa procedura del
   server e stampa righe per tabella, ultima migrazione e utenti di Keycloak. Alla fine cancella tutto.
3. Con `--tieni` il database resta acceso per avviarci il backend in locale: lo script stampa il comando.

**Ripristino sul server** (dopo un guasto; su un server nuovo, prima `bootstrap.sh` e il deploy dello stesso commit
del backup, che è nel `MANIFEST.txt`):
```bash
# sul PC: decifrare (il file in chiaro passa solo dal canale SSH e resta sul server il tempo del ripristino)
age -d -i ~/nexus-backup-chiave.txt -o archivio.tar nexus-AAAAMMGG-HHMMSSZ.tar.age && scp archivio.tar <server>:
# sul server
mkdir -m 700 ~/ripristino && tar -xf ~/archivio.tar -C ~/ripristino && (cd ~/ripristino && sha256sum -c SHA256SUMS)
cd /opt/nexus/current && C="sudo docker compose --env-file ../.env --env-file release.env"
$C stop backend keycloak backup
curl -fsSL https://raw.githubusercontent.com/maubernardi/nexus/main/backup/ricrea-database.sql \
  | $C exec -T postgres psql -U postgres -d postgres
$C exec -T postgres pg_restore --exit-on-error -U postgres -d nexus <~/ripristino/nexus.dump
$C exec -T postgres pg_restore --exit-on-error -U postgres -d keycloak <~/ripristino/keycloak.dump
rm -rf ~/ripristino ~/archivio.tar && $C up -d
```

**Privacy:** un dato cancellato o anonimizzato (US-1303) resta nei backup fino a 30 giorni: va scritto nel registro
dei trattamenti, con il DPO.

### Passaggio in produzione

US-1306 ([design](openspec/changes/prepare-production/design.md)). Decisione del PO del 20/11: si riporta tutto dalla
demo tranne segnalazioni, utenti e beneficiari; le aziende vengono dal database attuale (US-1309). Lo fa lo script
`infra/deploy/passaggio-in-produzione.sh`, provato in locale; il PO lo lancia sul server.

**Prima (il PO):**
1. Collaudo finito. Dai Cataloghi si tolgono le voci create solo per prova: la configurazione passa così com'è.
2. Prova generale sul backup vero della demo, insieme: ripristino con `backup/ripristino-di-prova.sh --tieni`, poi
   pulizia e importazione sul database ripristinato.
3. Dati dell'ADMIN in fondo al `.env` (`sudo nano /opt/nexus/.env`):
   ```bash
   NEXUS_ADMIN_USERNAME=<username, es. mario.rossi>
   NEXUS_ADMIN_FIRST_NAME=<nome>
   NEXUS_ADMIN_LAST_NAME=<cognome>
   NEXUS_ADMIN_EMAIL=<email: riceve l'invito per impostare la password>
   ```
4. Script di deploy aggiornato: scarica il realm di produzione, senza utenti di prova.
   ```bash
   sudo curl -fsSL https://raw.githubusercontent.com/maubernardi/nexus/<commit>/infra/deploy/nexus-deploy \
     -o /usr/local/bin/nexus-deploy && sudo chmod 755 /usr/local/bin/nexus-deploy
   ```
5. Deploy di `main` dalle Actions: il backend riceve i dati dell'ADMIN. Sulla demo non li usa, perché gli ADMIN ci
   sono già.
6. Il file Access sul server, dopo i controlli in Access delle chiamate con possibili nomi (analisi, §7 e §9.3):
   ```bash
   sudo install -d -o "$USER" -m 700 /opt/nexus/import                # sul server
   scp documenti_base/nexus.mdb <server>:/opt/nexus/import/nexus.mdb   # dal PC
   ```

**Passaggio (sul server, una volta sola):**
```bash
curl -fsSLO https://raw.githubusercontent.com/maubernardi/nexus/<commit>/infra/deploy/passaggio-in-produzione.sh
curl -fsSLO https://raw.githubusercontent.com/maubernardi/nexus/<commit>/infra/deploy/pulizia-demo.sql
sudo bash passaggio-in-produzione.sh
```
Lo script si ferma al primo errore e chiede «SI» prima di fermare il sito e prima di cancellare il file Access. Passi:
1. controlli;
2. **backup della demo** su IONOS, il punto a cui tornare;
3. fermo del backend e del sito;
4. **pulizia** del database (`pulizia-demo.sql`), in una sola transazione. Alla fine elenca le voci rimaste come le
   aveva messe la demo: tipologie di mansione segnaposto, progetti, enti con dati inventati;
5. **utenti di Keycloak**: via tutti, resta l'account tecnico del backend;
6. **importazione** del database attuale, con il resoconto in `/opt/nexus/import/nexus.mdb-resoconto.txt`;
7. `.env` con `NEXUS_BACKEND_PROFILES=keycloak` (niente dati dimostrativi), poi il riavvio. Entro un paio di minuti il
   backend crea l'ADMIN e gli manda l'invito;
8. **primo backup della produzione**, poi la cancellazione del file Access, se confermi.

**Dopo (il PO):**
1. Dall'email di invito imposti la password e accedi come ADMIN. Se l'email non arriva: «Password dimenticata?» nella
   pagina di accesso, con lo stesso indirizzo.
2. Nei Cataloghi controlli le voci elencate al passo 4; nel resoconto dell'importazione, duplicati e comuni non trovati.
3. Da «Utenti» crei gli account.
4. Nel registro dei trattamenti: i 30 giorni dei backup, con il DPO.

**Se qualcosa va storto**, lo script dice cosa fare:
- fino alla pulizia, che è una transazione, i dati non cambiano e basta riavviare;
- dopo, si ripristina il backup del passo 2 (sezione precedente, «Ripristino sul server»), si toglie
  `NEXUS_BACKEND_PROFILES=keycloak` dal `.env` e si riavvia.

## Comandi utili

| | Backend (`backend/`) | Frontend (`frontend/`) |
|---|---|---|
| Build + test | `./mvnw verify` | `pnpm build` · `pnpm test --run` |
| Lint | — | `pnpm lint` · `pnpm format:check` |

## Gestione del progetto (Agile)

Sprint di una settimana, con documenti versionati in [`docs/agile/`](docs/agile/README.md):
[Product Backlog](docs/agile/product-backlog.md) (epiche, storie, criteri di accettazione, domande aperte),
[Roadmap](docs/agile/roadmap.md) (release R1–R4) e un file per [sprint](docs/agile/sprints/).

## Workflow OpenSpec

Ogni funzionalità parte da una change proposal:

1. `/opsx:propose <idea>`: crea proposal, specs (delta), design e tasks in `openspec/changes/<nome>/`
2. `/opsx:apply`: implementa i task
3. `/opsx:verify` e poi `/opsx:archive`: le delta vengono consolidate in `openspec/specs/`

`openspec validate --all --strict` viene eseguito anche in CI.

## Regole Git

- Tutti i **merge** li esegue il maintainer; nessun agente o automazione fa merge.
- Nessun **push diretto su `main`** senza autorizzazione esplicita: si lavora su branch dedicati con Pull Request.

Le regole sono imposte su due livelli:

- **GitHub — branch protection su `main`** (vale per chiunque, admin inclusi):
  - modifiche solo tramite Pull Request;
  - check obbligatori `Backend (Java 25 LTS)`, `Frontend (React PWA)` e `OpenSpec`, verdi e con il branch aggiornato rispetto a `main`;
  - force push e cancellazione di `main` vietati;
  - conversazioni della PR risolte prima del merge.
- **Claude Code** — `.claude/hooks/guard-git.sh` e `.claude/settings.json`: i merge sono bloccati e ogni push verso `main`
  richiede conferma esplicita.

> I check obbligatori sono legati ai nomi dei job in `.github/workflows/ci.yml`: se un job viene rinominato, va aggiornata
> anche la branch protection (Settings → Branches), altrimenti le PR restano in attesa di un check che non arriva mai.
