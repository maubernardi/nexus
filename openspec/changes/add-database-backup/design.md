## Context

Vedi proposal.md, «Why». Sul server gira il compose di `infra/deploy/compose.yaml`: `postgres` (18.6 Alpine) ospita
due database, `nexus` (proprietario `nexus_owner`) e `keycloak` (proprietario `keycloak`). Il deploy scarica dal
repository solo i file elencati in `nexus-deploy` (compose, script dei ruoli, realm). Ogni servizio nuovo deve quindi
arrivare come immagine. La pipeline entra nel server solo con la chiave di deploy, limitata allo script
`nexus-deploy`. I log delle Actions sono pubblici.

Con tutti i dati di Access il database pesa 37 MB, e il formato compresso di `pg_dump` circa 3,6 MB. Trenta copie
costano meno di un euro al mese (review dello Sprint 15, §8.3).

## Goals / Non-Goals

**Goals:** un backup notturno che non dipende dal server, non è leggibile senza la chiave del PO e non è cancellabile
per 30 giorni. Un avviso se manca, e un ripristino provato.

**Non-Goals:** ripristino automatico, backup più frequenti, file fuori dal database (vedi proposal.md).

## Decisions

### D1. Immagine `nexus-backup`

- **Base `postgres:18.6-alpine`**, la stessa del server: `pg_dump` ha la stessa versione del database.
  - `tools/ci/check-image-versions.sh` controlla anche `backup/Dockerfile`.
  - Dependabot la aggiorna con le altre immagini.
- **Pacchetti Alpine**: `age` per cifrare, `aws-cli` per IONOS (lo strumento delle guide di IONOS), `tzdata` per l'ora
  italiana. Le versioni le fissa la base: i pacchetti seguono il ramo di Alpine dell'immagine.
- **Utente `postgres`**, non root.
- **Costruita e pubblicata** dal deploy come `nexus-backend`, `nexus-web` e `nexus-keycloak`. Nella CI la costruisce il
  job «Backup» (D10).

### D2. Pianificazione

- Il comando dell'immagine è un ciclo della shell: calcola la prossima 3:30 con `TZ=Europe/Rome`, dorme, esegue.
- **3:30** perché in Italia l'ora cambia tra le 2 e le 3: le 3:30 esistono sempre, e una volta sola.
- Se il server è spento alle 3:30, quel backup salta: lo segnala il controllo delle 9.
- Un errore non ferma il ciclo, perché un servizio che si riavvia di continuo farebbe fallire il deploy (`--wait`).
- Il servizio **non ha un controllo di salute**: un backup mancato non deve bloccare il deploy dell'app. Lo stato
  dei backup lo legge il controllo del mattino (D8).
- **Un backup alla volta**: `flock` sul volume dello stato. Un backup chiesto a mano mentre gira quello notturno
  aspetta e poi esce, senza duplicarlo.

### D3. Contenuto dell'archivio

- `pg_dump --format=custom` di `nexus` come `nexus_owner` e di `keycloak` come `keycloak`, cioè i proprietari dei
  database: niente superutente.
- `MANIFEST.txt` con:
  - data e ora;
  - commit in esecuzione (`NEXUS_VERSION`) e versione di PostgreSQL;
  - nome, dimensione e impronta SHA-256 di ogni file.
- I tre file vanno in un `tar`, che passa subito ad `age -r <chiave pubblica>`: sul disco resta in chiaro solo la
  cartella di lavoro temporanea, dentro il container, cancellata in ogni caso, anche dopo un errore.
- Nome dell'oggetto: `nexus/AAAA/MM/nexus-AAAAMMGG-HHMMSSZ.tar.age`, in UTC, per non avere doppioni al cambio
  dell'ora.

### D4. Caricamento e verifica

- `aws s3api put-object` con l'endpoint e la regione di IONOS dal `.env`.
- **`Content-MD5` sempre**: con il blocco degli oggetti S3 lo chiede, e lo accettano tutti i servizi compatibili.
- `AWS_REQUEST_CHECKSUM_CALCULATION=when_required`: le impronte aggiunte di default dalla AWS CLI recente non sono
  supportate da tutti i servizi compatibili con S3.
- Dopo il caricamento, `head-object`:
  - la dimensione deve coincidere;
  - modalità e scadenza del blocco finiscono nell'esito. Senza blocco, l'esito dice «non protetto».

### D5. Conservazione e protezione

- **Blocco degli oggetti**: si attiva solo quando si crea il contenitore, e accende il versioning. Lo sceglie il PO
  nella console di IONOS: modalità *Compliance*, 30 giorni.
  - In *compliance* nessuno può accorciare il blocco o cancellare una versione prima della scadenza: né IONOS, né il
    proprietario.
  - Il blocco non ha un costo a parte: si paga lo spazio occupato.
- **Ciclo di vita**: il servizio lo riscrive dopo ogni backup riuscito; l'operazione è idempotente. La richiesta usa
  l'impronta CRC32, perché quella predefinita della AWS CLI recente (CRC64NVME) non la accettano tutti i servizi
  compatibili con S3. Tre regole:
  - le versioni correnti scadono dopo 30 giorni;
  - quelle non più correnti si cancellano dopo 1 giorno, quando il blocco è già scaduto;
  - si tolgono i marcatori di cancellazione rimasti soli.

  Restano così circa 31 copie.
- Se IONOS rifiuta la regola, il servizio riprova senza quella dei marcatori. Se la rifiuta ancora, il backup resta
  riuscito: se nel contenitore c'è già una regola di 30 giorni messa dalla console va bene, altrimenti l'esito riporta
  l'avviso.
- **Chiavi**: quelle di IONOS dell'utente del PO. Con il blocco in *compliance*, chi le rubasse dal server non
  potrebbe cancellare i backup.

### D6. Esito

- File `stato` nel volume `backup-state`, una riga `chiave=valore` per campo:
  - ultimo tentativo e suo esito: `ok`, `errore` o `non configurato`;
  - passo fallito;
  - ultimo backup riuscito: data, oggetto, dimensione, scadenza del blocco;
  - avvisi.
- `nexus-backup stato` lo stampa in italiano ed esce con `1` se l'ultimo backup riuscito ha più di 26 ore, o se non ce
  n'è nessuno.
- I messaggi di `pg_dump`, `age` e `aws` vanno nel log del servizio, mai nel file di stato. Non contengono dati:
  solo nomi di tabelle, codici di errore e nomi dei file.

### D7. Accesso dalle Actions: `nexus-deploy`

Comandi nuovi del forced command, attraverso `docker compose exec -T backup` sulla release in esecuzione:
- `backup`: lo stato, in sola lettura, con il codice di uscita di `nexus-backup stato`;
- `backup adesso`: esegue un backup subito;
- `logs backup [righe]`: il servizio entra tra quelli con i log pubblicabili.

Lo script sul server non si aggiorna con il deploy: il PO lo reinstalla una volta con sudo, con il comando nel README.

### D8. Controllo delle 9

- Workflow «Backup del database» (`backup.yml`), con due cron UTC:
  - `0 8 * * *`, valido quando Roma è a `+0100`;
  - `0 7 * * *`, valido quando Roma è a `+0200`.
- Il primo passo confronta `github.event.schedule` con lo scarto di Roma e salta il cron dell'altra stagione. Così il
  controllo gira una volta sola, anche se GitHub lo ritarda.
- **Avvio manuale**: `stato` o `backup adesso`.
- Il run fallisce se il comando sul server esce con un errore; GitHub manda l'email come per «Controllo della demo».
- «Stato del server» aggiunge `backup` ai servizi dei log.

### D9. Ripristino

- **`backup/ripristino-di-prova.sh`**, per il PC del PO (WSL, Docker e `age`). Prende l'archivio e il file della
  chiave privata, poi:
  - decifra in una cartella temporanea leggibile solo dall'utente;
  - controlla le impronte del `MANIFEST.txt`;
  - avvia un `postgres:18.6-alpine` temporaneo con lo stesso script dei ruoli del server e password generate al
    momento;
  - ripristina i due database come superutente, così proprietari e permessi restano quelli del server;
  - stampa le righe delle tabelle principali, l'ultima migrazione e il numero degli utenti di Keycloak: solo numeri.
- Con `--tieni` il database resta acceso, per avviarci il backend in locale.
- **Sul server**, procedura nel README:
  - fermare backend e Keycloak;
  - ricreare i due database vuoti;
  - `pg_restore` dentro il container di PostgreSQL;
  - riavviare.
- Sullo stesso server o su uno nuovo, dopo `bootstrap.sh` e il primo deploy.

### D10. Prova nella CI

- **Job «Backup»**:
  - costruisce l'immagine;
  - avvia PostgreSQL 18.6 con lo script dei ruoli e un database di prova con una tabella con trigger;
  - avvia un servizio S3 di prova;
  - genera una chiave age al momento;
  - esegue `nexus-backup backup`, poi `stato`, poi lo script di ripristino.
- **Servizio di prova: VersityGW**, ultima versione stabile, compatibile con S3 e con il blocco degli oggetti. MinIO
  non pubblica più le immagini.
- **Controlli**:
  - righe uguali tra origine e ripristino;
  - impronte giuste;
  - archivio illeggibile senza la chiave;
  - stato `1` prima del primo backup e `0` dopo;
  - «non configurato» senza le variabili.
- Lo stesso script (`tools/ci/backup-e2e.sh`) gira anche in locale.

### D11. Variabili nel `.env`

Le mette solo il PO:

| Variabile | Esempio |
|---|---|
| `NEXUS_BACKUP_S3_ENDPOINT` | `https://s3.eu-central-3.ionoscloud.com` |
| `NEXUS_BACKUP_S3_REGION` | `eu-central-3` |
| `NEXUS_BACKUP_S3_BUCKET` | nome del contenitore |
| `NEXUS_BACKUP_S3_ACCESS_KEY`, `NEXUS_BACKUP_S3_SECRET_KEY` | chiavi di IONOS |
| `NEXUS_BACKUP_AGE_RECIPIENT` | `age1…`, la chiave pubblica |

Nel compose sono facoltative (`:-`): senza, il servizio scrive «non configurato» e il deploy riesce.

## Risks / Trade-offs

- **[Server spento alle 3:30]**: il backup salta. → Lo segnala il controllo delle 9; si rimedia con «backup adesso».
- **[La chiave privata si perde]**: i backup diventano illeggibili. → Va nel password manager e in una copia offline,
  e la prova di ripristino la usa davvero (README).
- **[IONOS cambia le impronte richieste]**: il caricamento fallisce. → Lo segnala il controllo delle 9; l'errore di
  `aws` è nel log del servizio.
- **[Regione con il blocco mancante]**: se il contenitore è stato creato senza blocco, il blocco non si aggiunge più.
  → L'esito lo segnala, e si crea un contenitore nuovo.
- **[Pacchetti Alpine non fissati alla versione]**: un nuovo `aws-cli` arriva con una nuova base. → Lo prova il job
  «Backup» della PR di Dependabot.

## Migration Plan

1. PR con l'immagine, il servizio e i workflow. Deploy sulla demo: senza variabili, l'esito dice «non configurato».
2. Il PO:
   - crea il contenitore con il blocco e le chiavi;
   - genera la coppia di chiavi age;
   - mette le variabili nel `.env`;
   - aggiorna `nexus-deploy` con sudo.
3. Riavvio del servizio (`deploy` dello stesso commit), poi «backup adesso» dalle Actions.
4. Il PO scarica l'archivio dalla console di IONOS e lancia il ripristino di prova sul suo PC.

**Ritorno indietro**: il deploy di un commit precedente toglie il servizio (`--remove-orphans`). Volume e backup
restano.

## Open Questions

Nessuna: regione, prezzo, blocco e orario sono decisi (review dello Sprint 15, §8.3; planning dello Sprint 16).
