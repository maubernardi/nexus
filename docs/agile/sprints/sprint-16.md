# Sprint 16 — 21 – 23 novembre 2026

**Stato:** 🏃 in corso: planning confermato dal PO il 20/11, 13 punti; 26 con US-1203 e US-1204, aggiunte dal PO
il 22/11.
**Planning:** 20/11, dalla [review dello Sprint 15](sprint-15-review.md) (§8, §9).

## Obiettivo

> NEXUS va in produzione sul server di oggi, con un backup giornaliero cifrato fuori dal server e provato con un
> ripristino. La produzione tiene la configurazione della demo e prende le aziende dal database attuale; parte senza
> segnalazioni, beneficiari e utenti, con la sola utenza dell'ADMIN.

## Impegno (26 punti)

| ID | Storia | Punti | Stato | Change OpenSpec | PR |
|---|---|---|---|---|---|
| US-1302 | Backup del database | 5 | ✅ Ready | `add-database-backup` | [#174](https://github.com/maubernardi/nexus/pull/174) |
| US-1306 | Passaggio in produzione | 8 (era 5) | ✅ Ready | `prepare-production` | [#177](https://github.com/maubernardi/nexus/pull/177) (preparazione) |
| US-1203 | Avvisi di fine tirocinio e blocco delle nuove segnalazioni | 5 (aggiunta il 22/11) | ✅ Ready | `add-internship-end-reminders` | [#179](https://github.com/maubernardi/nexus/pull/179) |
| US-1204 | Testi delle notifiche e delle email modificabili dall'ADMIN | 8 (aggiunta il 22/11) | ✅ Ready | `add-editable-message-texts` | [#180](https://github.com/maubernardi/nexus/pull/180) |

Alla review avevamo detto 10 punti. Scrivendo i criteri, US-1306 è salita a 8: il suo criterio unico nascondeva la
pulizia della demo, il realm senza utenti, la creazione dell'ADMIN, la procedura, l'importazione sul server e il cambio
di nome. Resta sotto la velocity media (23,1), e il margine serve per correggere quello che emerge dal collaudo.

**Ordine:**
1. **US-1302, provata sulla demo.** Nessun dato reale in gioco. Senza un ripristino riuscito non si va in produzione.
2. **Collaudo T-22 … T-25 sulla demo**, a cura tua, con la data che decidiamo in corsa (review, §8.5). Dopo il
   passaggio la demo non c'è più.
3. **US-1306 in due tempi:**
   - prima la preparazione (script di pulizia, realm senza utenti, creazione dell'ADMIN, procedura, nomi), provata in
     locale su una copia del backup della demo e unita come ogni PR;
   - poi il passaggio vero sul server, nel giorno che scegli, seguendo la procedura insieme.

   Se il collaudo non finisce in tre giorni, il passaggio slitta e la storia si chiude quando è fatto.

Migrazioni: **V50** per US-1203, **V51** per US-1204; la prossima è **V52**.

Ogni storia con interfaccia nuova si controlla in Chrome **prima** della PR (A26). Se l'estensione si blocca, il
controllo si fa con Chrome senza finestra e un profilo pulito (A34). Per attendere la CI uso solo
`gh pr checks N --watch` (A36).

## Decisioni del planning (20/11)

Tutte del PO.

- **Dati della produzione**: si riporta tutto dalla demo, tranne segnalazioni, utenti e beneficiari. Le aziende vengono
  dal database attuale (US-1309): quelle della demo si tolgono. Sostituisce la proposta di un'installazione pulita: la
  configurazione non si rifà.
- **Audit della demo**: lo script di pulizia toglie anche le voci su segnalazioni, beneficiari, utenti e aziende tolte;
  restano quelle sulla configurazione che teniamo. È l'unica volta che l'audit si tocca, prima che ci siano dati veri.
- **Avviso del backup**: un controllo ogni mattina alle 9. Se manca un backup riuscito nelle ultime 26 ore, GitHub ti
  manda un'email, come fa oggi il controllo della demo.
- **Protezione dei backup**: se IONOS offre il blocco degli oggetti senza costi in più, nessuno può cancellare un backup
  per 30 giorni, nemmeno chi entrasse nel server. Lo verifico all'inizio di US-1302; altrimenti le chiavi del server
  possono solo caricare.
- **Stima**: 13 punti invece dei 10 detti alla review, per US-1306 portata a 8.

## Decisioni durante lo sprint

- **US-1302**:
  - nuova immagine `nexus-backup` sulla stessa base di PostgreSQL del server: `pg_dump` ha la stessa versione del
    database, e il controllo delle immagini (EN-6) la segue. Dentro `age` e la AWS CLI, dai pacchetti di Alpine;
  - i due database li salvano i loro proprietari (`nexus_owner`, `keycloak`): niente superutente. L'archivio ha un
    indice con commit e versione di PostgreSQL e le impronte SHA-256 dei file;
  - le 3:30 perché in Italia l'ora cambia tra le 2 e le 3: esistono sempre, e una volta sola;
  - niente controllo di salute nel compose: un backup mancato non deve bloccare il deploy dell'app. Lo stato lo legge
    il controllo delle 9;
  - il controllo delle 9 ha due cron UTC, uno per l'ora solare e uno per la legale. Gira solo quello della stagione;
  - IONOS: il blocco degli oggetti si sceglie solo quando si crea il contenitore, e accende il versioning. La regola
    dei 30 giorni la scrive il servizio dopo ogni backup;
  - la AWS CLI recente firma alcune richieste con un'impronta (CRC64NVME) che non tutti i servizi compatibili con S3
    accettano. Il caricamento usa `Content-MD5` e la regola CRC32. Se IONOS rifiuta comunque la regola, l'esito lo
    dice e la si mette dalla console;
  - `nexus-deploy` ha i comandi `backup`, `backup adesso` e `logs backup`. Non si aggiorna con il deploy: lo
    reinstalli tu, una volta, con sudo;
  - la prova completa nella CI usa VersityGW come S3 di prova, perché MinIO non pubblica più le immagini. Non gestisce
    la regola dei 30 giorni: la prova controlla così anche l'avviso;
  - **blocco in *Governance*** invece di *Compliance* (PO, 21/11): il PO vuole poter cancellare un backup a mano, se
    serve. Una richiesta normale resta rifiutata, anche con le chiavi del server. Il rischio che resta: chi prendesse
    il server, con le chiavi dell'utente del PO, potrebbe scavalcare il blocco con una richiesta fatta apposta. Se
    servirà, il server avrà un utente IONOS suo, senza quel permesso;
  - **messa in funzione (21/11)**:
    - deploy di `765dec9`;
    - il PO ha creato il contenitore a Berlino (`eu-central-3`) con il blocco di 30 giorni, la chiave, la coppia age e
      le righe del `.env`, e ha reinstallato `nexus-deploy`;
    - il primo «backup adesso» è riuscito (1 MB, blocco fino al 21/12), e IONOS ha accettato la regola dei 30 giorni
      al primo tentativo;
  - **ripristino di prova riuscito** (21/11), sul PC del PO con l'archivio scaricato da IONOS:
    - impronte corrette;
    - ultima migrazione V49 e gli 8 trigger dello schema;
    - tutte le tabelle della demo, compresi 7.896 comuni e 6 modelli dei documenti;
    - 6 utenti nel realm `nexus`;
  - lo script di ripristino ora controlla subito che Docker risponda: con Docker Desktop spento si fermava senza dire
    perché.
  - **primo giorno (22/11)**:
    - il backup delle 3:30 è riuscito, con il blocco fino al 22/12;
    - il controllo ha scelto il cron giusto (ora solare) e ha saltato l'altro;
    - GitHub però l'ha fatto partire alle 17:06 invece che alle 9. Il controllo passa al minuto 7, meno carico
      dello scoccare dell'ora.

- **US-1306, preparazione** (prima PR; il passaggio vero nel giorno che scegli):
  - pulizia della demo in `infra/deploy/pulizia-demo.sql`, una sola transazione come `nexus_owner`:
    - `TRUNCATE` senza `CASCADE` delle 25 tabelle di segnalazioni, beneficiari, utenti e aziende: se ne mancasse
      una, PostgreSQL si fermerebbe;
    - via i curriculum, l'audit degli oggetti tolti e le righe degli script dimostrativi nella cronologia di Flyway,
      senza le quali il backend non partirebbe senza `seed`;
    - controlli finali dentro la transazione;
    - alla fine elenca le voci della configurazione rimaste come le aveva messe la demo: le 8 tipologie di mansione
      segnaposto, i progetti, gli enti con i dati inventati;
  - primo ADMIN: con i dati `NEXUS_ADMIN_*` nel `.env` e nessun ADMIN in NEXUS, il backend lo crea come da «Utenti»,
    con l'invito, e riprova ogni minuto finché Keycloak non risponde;
  - realm di produzione = quello della demo senza utenti di prova. Lo scarica `nexus-deploy`, da reinstallare con
    sudo; la CI controlla che si importi senza utenti;
  - utenti di Keycloak: l'elenco esclude da solo l'account tecnico del backend (provato su Keycloak 26.8.2);
  - script del passaggio con 8 passi, conferme e indicazioni per tornare indietro;
  - il profilo predefinito resta `keycloak,seed` fino al passaggio: lo script mette `keycloak` nel `.env`, e la
    seconda PR cambia il compose. Prima, un deploy della demo non partirebbe;
  - prova generale in locale (21/11): demo dai dati dimostrativi, pulizia, importazione del file vero (13.799 aziende,
    38.789 chiamate, come il 19/11), backend senza `seed` con 49 migrazioni valide e l'ADMIN creato con l'invito;
  - **prova generale sul backup vero della demo** (23/11), quello delle 3:30, ripristinato dal PO sul suo PC:
    - pulizia riuscita. Tolte 39 voci dell'audit e 17 righe degli script dimostrativi; restano 7.896 comuni, 6
      modelli, 4 enti e 13 voci dell'audit della configurazione;
    - voci da controllare: il progetto POLIS e le 8 tipologie di mansione segnaposto. Gli enti hanno già i dati veri;
    - importazione del file Access: 13.799 aziende, 38.789 chiamate. Backend senza `seed` con 51 migrazioni valide e
      l'ADMIN creato con l'invito;
    - Keycloak del backup: tolti i 5 utenti, l'account tecnico del backend resta;
    - un difetto trovato e corretto: lo spegnimento del ripristino di prova lasciava sul disco il volume dei dati, cioè
      il backup in chiaro. Ora gli script lo cancellano (`docker rm -f -v`).

- **US-1203, aggiunta dal PO il 22/11**, da avere prima del passaggio in produzione. Decisioni del PO:
  - avvisi prima della fine solo al Tutor;
  - giorni di calendario;
  - blocco delle nuove segnalazioni dal giorno dopo la fine prevista.

  Lo sprint passa da 13 a 18 punti, sempre sotto la velocity media (23,1). Altre scelte:
  - il controllo gira alle 8;
  - le fasi degli avvisi sono sulla scheda del tirocinio (V50) e si scrivono senza toccare la sua versione, così un
    Tutor che sta modificando la scheda non trova un conflitto;
  - dopo un giorno saltato parte solo l'avviso più recente;
  - il blocco vale anche per la candidatura dalla bacheca.

- **US-1204, aggiunta dal PO il 22/11**, dopo la domanda su dove stanno i testi delle email. Decisioni del PO:
  - per ogni notifica l'ADMIN cambia due testi distinti, quello nell'app e quello dell'email;
  - l'oggetto resta «PORTALE NEXUS - Segnalazione n. N», con una parte distintiva per ogni tipo, modificabile;
  - si fa subito, in questo sprint.

  Lo sprint arriva a 26 punti, sopra la velocity media (23,1), come il PO ha messo in conto. Altre scelte:
  - i predefiniti restano nel codice, e il database tiene solo i testi cambiati: dopo il deploy non cambia nulla,
    tranne la parte distintiva dell'oggetto, che è nuova;
  - le notifiche nell'app prendono il testo dal backend: il frontend non ha più una copia;
  - pagina a parte, «Testi dei messaggi», nel menu di amministrazione;
  - le email di Keycloak restano nel tema; si ridefiniscono dalla console di Keycloak (*Realm settings → Localization
    → Realm overrides*), provato il 23/11 con l'oggetto dell'invito.

## Come li faccio

**Backup (US-1302):**
- un servizio nel compose del server, quindi arriva con il deploy: sul server non si installa nulla a mano;
- ogni notte salva i due database (NEXUS e Keycloak), li cifra con la chiave pubblica e li carica su IONOS;
- tiene le ultime 30 copie e scrive l'esito, che il controllo del mattino legge;
- l'ultimo esito si legge anche da «Stato del server».

**Passaggio in produzione (US-1306):**
- resta tutta la configurazione della demo, con le impostazioni di Keycloak;
- uno script, una volta sola e in una transazione, toglie segnalazioni (con bacheca, schede, report, storico,
  notifiche), beneficiari, utenti di NEXUS e di Keycloak e aziende della demo (con referenti, diario, storico e
  mansioni), e le loro voci dell'audit;
- storico dell'azienda, diario, report di chiusura e audit rifiutano le cancellazioni (EN-2): lo script sospende queste
  regole solo dentro la sua transazione. Se qualcosa va storto, la transazione si annulla e non cambia nulla;
- lo provo in locale su una copia del backup della demo: è il primo uso di US-1302, e nel backup ci sono solo dati
  dimostrativi;
- dopo la pulizia, l'importazione del database attuale (analisi, §9.2);
- il backend parte senza il profilo `seed`: dalla cronologia di Flyway tolgo le voci degli script dimostrativi,
  altrimenti non partirebbe;
- Keycloak importa un realm senza utenti di prova;
- al primo avvio NEXUS crea l'ADMIN dall'email nel `.env`, con l'invito di US-103;
- la procedura parte da un backup della demo, per poter tornare indietro, e finisce con il controllo di sicurezza.

## Per il PO

**Per partire con US-1302** (ti guido passo per passo quando ci arriviamo):
- in IONOS crei il contenitore, con i dati in Germania, e una chiave di accesso;
- sul tuo PC generi la coppia di chiavi di cifratura con un comando che ti do. La chiave privata va nel password
  manager e in una copia offline, mai sul server;
- metti nel `.env` del server le chiavi di IONOS e la chiave pubblica: mai in chat.

**Prima del passaggio:**
- il collaudo T-22 … T-25. Segnalazioni, beneficiari, utenti e aziende che crei per le prove li toglie la pulizia; la
  configurazione invece passa in produzione così com'è, quindi le voci create solo per prova vanno tolte a mano;
- i comuni completi sulla demo (file ISTAT, US-114), se non li hai già caricati: l'importazione li usa per le sedi;
- il controllo in Access delle 7 chiamate con nome e cognome e delle 20 con possibili nomi;
- l'email dell'ADMIN nel `.env`;
- la copia del file Access sul server (analisi, §9.2).

**Dopo il passaggio:**
- gli account degli utenti;
- i 30 giorni dei backup nel registro dei trattamenti, con il DPO.

## Burndown

| Momento | Punti residui |
|---|---|
| Inizio (20/11, dopo la conferma del planning) | 13 |
| US-1302 unita (21/11) | 8 |
| US-1203 aggiunta dal PO (22/11) | 13 |
| US-1203 unita (22/11) | 8 |
| US-1204 aggiunta dal PO (22/11) | 16 |
| US-1204 unita | 8 |
