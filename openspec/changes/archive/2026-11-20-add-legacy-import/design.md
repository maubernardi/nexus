## Context

Vedi proposal.md, «Why», e l'[analisi](../../../docs/analisi/database-attuale.md), che ha i conteggi e la qualità dei
dati. Le tabelle di Access che servono sono:
- `Contatti`: le aziende;
- `Chiamate`;
- `Esiti`, `Progetti` e `Assistenti`: i testi dei codici delle chiamate.

Ruoli e categorie hanno già gli id fissi delle voci iniziali (V46, V47). Il backend gira in Docker. Sul server una
seconda istanza si avvia con `docker compose run --rm backend`, con le stesse variabili d'ambiente.

## Goals / Non-Goals

**Goals:** un'importazione che si prova in locale su una copia, si ripete senza danni e lascia un resoconto da
rivedere, pronta per il passaggio in produzione (US-1306).

**Non-Goals:** esecuzione in produzione, interfaccia per l'importazione o per le chiamate, correzione automatica dei
duplicati (vedi proposal.md).

## Decisions

### D1. Avvio: un profilo, non un endpoint

- **Profilo `legacy-import`** e proprietà `nexus.legacy-import.file` (percorso del file `.mdb`).
- `application-legacy-import.yml`:
  - `spring.main.web-application-type: none`;
  - `nexus.scheduling.enabled: false`. `SchedulingConfig` diventa condizionale: senza attività pianificate,
    l'istanza dell'importazione non manda promemoria né scade timer accanto a quella in servizio.
- `LegacyImportRunner` è un `ApplicationRunner` con l'ordine più basso, dopo il caricamento dei comuni del profilo
  `seed`. Importa, scrive il resoconto, scrive i conteggi nel log e chiude l'applicazione: 0 se riesce, 1 se fallisce.
- **Niente endpoint**: il file (124 MB, dati personali) non passa da HTTP, e l'importazione si fa una volta sola sul
  server.
- **Procedura** nell'analisi, §9:
  - in locale: copia del database, profili `postgresql,seed,legacy-import`;
  - sul server: `docker compose run --rm` con il file montato.

### D2. Lettura del file

- Libreria **Jackcess** (`com.healthmarketscience.jackcess`, Apache 2.0, ultima versione stabile): legge Access
  2000–2003 in Java, senza mdbtools sul server.
- Il file attuale è **codificato** da Access (RC4, senza password): serve `jackcess-encrypt`, dello stesso progetto,
  che usa BouncyCastle. BouncyCastle è fissato all'ultima versione stabile, più nuova di quella che porta con sé.
- Il file si apre in **sola lettura**.
- Date e ore di Access sono locali: diventano istanti con il fuso `Europe/Rome`. «OraChiamata» è un'ora sul giorno
  zero di Access e si unisce al giorno di «DataChiamata».
- Testi: spazi tolti; vuoto diventa assente.

### D3. Id fissi e ripetibilità

| Righe | Id |
|---|---|
| Aziende | 893700000010000000 + IDContatto |
| Referenti | 893700000020000000 + IDContatto × 10 + posizione |
| Note del diario | 893700000030000000 + IDContatto × 10 + n (0 = nota di Access, 1 = recapiti) |
| Eventi `IMPORT` dell'audit | 893700000040000000 + IDContatto |
| Chiamate | 893700000050000000 + IDChiamata |

- Gli id sono sotto quelli generati oggi (TSID), come quelli di V46 e V47. Il database di produzione nasce dopo e non
  può avere id generati in quell'intervallo.
- Ogni `INSERT` ha `ON CONFLICT (id) DO NOTHING`: una seconda esecuzione non duplica e non sovrascrive.
- Tutto in **una transazione**: se qualcosa fallisce non entra nulla, e si ripete dopo aver corretto.
- `JdbcTemplate` con inserimenti a lotti: sono circa 60.000 righe, senza passare dalle entità.

### D4. Aziende

| NEXUS | Access |
|---|---|
| Ragione sociale | «NomeSocietà», altrimenti `CompanyPlaceholders.name(IDContatto)` |
| Partita IVA | `CompanyPlaceholders.VAT_CODE` |
| Sede legale | «Indirizzo_1» e il comune di «Sede Legale» (D5) |
| Sede operativa | «Indirizzo_2» e il comune di «Sede Locale» |
| Telefono, cellulare, fax | «TelefonoUfficio», «TelefonoCellulare», «NumeroFax» (D6) |
| Email, seconda email | «IndirizzoPostaElettronica_1» e «_2» (D6) |
| Categoria | 893700000000000400 + «Id_nuova_cat», se la voce esiste |
| Attività | «Paese» |
| Creata il | «data registrazione», altrimenti l'istante dell'importazione |

- Tutte attive.
- `created_by` e `updated_by` valgono `database-attuale`. Il diario mostra quell'autore come «Database attuale»
  (`UserDisplayNameService`).

### D5. Comune

Il testo si normalizza come i codici: senza accenti, in maiuscolo, punteggiatura → spazi. Poi si cerca tra i comuni
**vigenti**, in quest'ordine:
1. **alias** da `legacy/municipality-aliases.csv`: abbreviazioni («S FIORENTINO»), frazioni e quartieri («GRASSINA»,
   «OSMANNORO»), comuni fusi («FIGLINE VALDARNO», «SCARPERIA»), refusi visti nei dati;
2. **nome esatto**;
3. nome con la **sigla della provincia** in coda o in testa («SCANDICCI FI», «FI SCANDICCI»): il comune deve essere di
   quella provincia, e la sigla sceglie tra i nomi uguali;
4. **parole iniziali** che sono un comune («FIRENZE NOVOLI», «SCANDICCI FIRENZE»): vince il nome più lungo. Se le
   parole sono anche l'inizio di un comune più lungo, valgono solo per un comune toscano: «PRATO CALENZANO» è Prato,
   ma «PONTE A SIGNA» non è Ponte (BN) e «SESTO FIORENTION» non è Sesto (BZ);
5. testo di **20 caratteri** (il massimo del campo di Access) che è l'inizio di un solo comune («CASTIGLION
   FIORENTIN»).

Restano **non trovati**: la sola sigla della provincia («FI»), i nomi ambigui e il resto. Finiscono nel resoconto con
il testo e i numeri delle aziende.

La tabella degli alias contiene solo nomi di luoghi e sta nel codice. Si allunga con i casi del resoconto.

### D6. Recapiti

- **Telefoni**: il campo prende la parte iniziale che rispetta il formato del modulo (cifre, spazi, `+ ( ) . - /`).
  Il testo originale va nella nota «Recapiti del database attuale» quando ha altro: un testo accanto al numero, una
  seconda riga, solo lettere.
- **Email**:
  - si estraggono gli indirizzi validi dal testo, togliendo i doppioni. Il testo è quasi sempre un collegamento di
    Access, `testo#http://indirizzo#`: si tolgono `http://`, `https://` e `mailto:`;
  - il controllo è più severo di quello del modulo e di `@Email`, così ogni email importata si salva senza errori;
  - il primo va nel campo, il secondo nella seconda email se è libera;
  - gli altri indirizzi e i testi non validi vanno nella stessa nota.
- La nota ha la data dell'importazione.

### D7. Referenti

- «Nominativo_1» con «IDTipoContatto_1», poi «Nominativo_2» con «IDTipoContatto_2», nelle posizioni 0 e 1. Si salta
  un referente senza nome e senza ruolo.
- Ruolo: 893700000000000500 + il numero, per le 12 voci di V47; 28 diventa 27; gli altri numeri (4, 15, 17…) restano
  senza ruolo e si contano nel resoconto.
- I recapiti in Access sono dell'azienda: i referenti importati non ne hanno.

### D8. Note e audit

- **Nota di Access**: entra nel diario con la data dell'ultima modifica della scheda («Data_um_4»), altrimenti la
  «data registrazione», altrimenti l'istante dell'importazione. Le note sono al più di 1.261 caratteri, sotto i 2.000
  del diario.
- **Audit**: per ogni azienda l'evento `IMPORT` con `{"legacyNumber": n}` e l'autore `database-attuale`.

### D9. Archivio delle chiamate (V48)

Tabella `legacy_call`:
- id e numero della chiamata (`legacy_number`, unico);
- azienda (`company_id`, facoltativa) e numero dell'azienda in Access (`legacy_company_number`, 0 = nessuna);
- `called_at`, `subject`, `outcome`, `outcome_at` e `notes`;
- `handled` (evasa);
- le disponibilità: `internship`, `stage`, `ist`, `work_grant`, `hiring`, `giovanisi`;
- `project`, `operator` e `legacy_updated_at`;
- `created_at` e `created_by`.

Vincoli: `pk_legacy_call`, `uq_legacy_call_legacy_number`, `fk_legacy_call_company`, `ck_legacy_call_legacy_number`;
indice `ix_legacy_call_company`.

- Esito, progetto e operatore sono testi, presi dalle tabelle di Access: l'archivio si legge senza Access.
- Nessuna entità JPA: l'archivio non ha interfaccia.
- Le date anomale (1931, 2030) restano come sono.

### D10. Resoconto

Testo in `nexus.legacy-import.report`, se c'è, altrimenti accanto al file (`<file>-resoconto.txt`).

- **Conteggi**:
  - aziende lette, nuove e già presenti;
  - senza nome;
  - comuni abbinati, per regola, e non trovati;
  - categorie, referenti, ruoli non riconosciuti;
  - recapiti messi nel diario;
  - note;
  - chiamate, con e senza azienda.
- **Da rivedere**, con i numeri di Access e i collegamenti `/aziende/<id>`:
  - duplicati per nome normalizzato (solo i numeri, non il nome);
  - comuni non trovati, raggruppati per testo. Nel campo del comune a volte c'è altro: le sequenze di almeno 5 cifre
    (partite IVA, telefoni) diventano «…»;
  - aziende senza nome;
  - chiamate le cui note hanno «utente», «candidato» o «ragazzo» seguiti da due parole con l'iniziale maiuscola.
    È un aiuto al controllo del PO sulle 7 chiamate dell'analisi, non lo sostituisce.
- Nel **log** solo i conteggi.

### D11. Test

- Un file Access costruito nei test con Jackcess, con le stesse tabelle e colonne e dati inventati sul modello di
  quelli veri:
  - comuni con la sigla;
  - collegamenti `mailto`;
  - numeri con testo;
  - ruoli 28 e 15;
  - chiamate senza azienda.
- Test unitari dell'abbinamento dei comuni e della pulizia dei recapiti.
- Test di integrazione sul database:
  - aziende, referenti, note, audit e archivio;
  - seconda esecuzione senza effetti;
  - elenco dei comuni vuoto;
  - resoconto senza i nomi.
- **Prova in locale** sul file vero: solo i conteggi del resoconto finiscono nella PR.

## Risks / Trade-offs

- **[Comuni sbagliati da un alias]**: una frazione può stare su due comuni (Osmannoro tra Firenze e Sesto Fiorentino)
  → gli alias scelgono il comune più probabile; il resoconto conta gli abbinamenti per regola, e il comune si corregge
  dalla scheda.
- **[Archivio con dati personali]**: le note delle chiamate hanno iniziali dei beneficiari e nomi dei referenti, e
  l'archivio ha i nomi degli operatori → si legge solo dal database. Conservazione e cancellazione vanno decise con il
  passaggio in produzione.
- **[Memoria]**: il file si legge tutto in memoria (circa 50.000 righe). Sta nei 768 MB del contenitore, perché
  Jackcess legge per righe.
- **[Id fissi]**: gli intervalli corrispondono a 12 millisecondi del 02/10/2026, alle 05:24:44 (ora italiana). Solo
  un id generato in quell'istante potrebbe coincidere. Il database di produzione nasce dopo; sulla demo l'importazione
  non si esegue.

## Migration Plan

V48 aggiunge una tabella vuota. In caso di ripristino (A16) la release precedente la ignora. L'importazione non
parte con il deploy: si esegue a mano, con il passaggio in produzione.
