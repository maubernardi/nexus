## Context

Motivazione: vedi `proposal.md`.

Oggi `InternshipDocumentServiceImpl`:
- compila un documento per tipo (convenzione, progetto formativo) con i 28 segnaposto di `DocumentPlaceholders`;
- prende i valori dalla scheda (`InternshipDTO`) e dalla segnalazione;
- sceglie il modello caricato dall'ADMIN o quello generico nel codice (US-1010);
- rifiuta la relazione finale con `404`.

La scheda ha già:
- la convenzione iniziale e le proroghe, ognuna con i suoi progetti formativi, mesi e monte ore calcolati (US-1008,
  US-1012);
- il beneficiario con sesso, cittadinanza, titolo di studio, contatti, ente di presa in carico e case manager (US-206,
  US-308);
- l'ente con sede, codice fiscale, rappresentante e referente (US-115);
- la sede di competenza nella segnalazione (US-115);
- nel report di restituzione: fine effettiva, motivo di chiusura e descrizione dell'interruzione (US-1009, US-1103).

I modelli reali (`documenti_base/`, fuori dal repository) mostrano cosa manca:
- dati del promotore, dell'ente e del case manager;
- numero e date della convenzione e del progetto;
- orario a caselle per giorno;
- dati della chiusura.

## Goals / Non-Goals

**Goals:**
- Tutti i dati che NEXUS conosce, in segnaposto descritti, così EN-8 può preparare i 6 modelli di GOL.
- Un documento per ogni convenzione e per ogni progetto formativo; la relazione finale a tirocinio chiuso.
- Dati del promotore fuori dal repository e fuori dai modelli.

**Non-Goals:** vedi `proposal.md`.

## Decisions

### D1 — Quale documento: `extension` e `project`

`GET …/documents/{kind}` riceve due parametri facoltativi:
- `extension`: il numero della proroga, da 1. Senza, vale la convenzione iniziale.
- `project`: il numero del progetto formativo nella sua convenzione, solo per il progetto formativo.

Se il numero non esiste, la risposta è `404` («Proroga non trovata», «Progetto formativo non trovato»). Senza
`project` il progetto formativo esce con i dati del progetto vuoti: è il documento unico di oggi, utile prima di
inserire i progetti. La relazione finale ignora i due parametri.

Il nome del file dice quale documento è:
- `Convenzione-segnalazione-12.docx`;
- `Convenzione-proroga-1-segnalazione-12.docx`;
- `Progetto-formativo-2-segnalazione-12.docx`;
- `Progetto-formativo-proroga-1-progetto-2-segnalazione-12.docx`;
- `Relazione-finale-segnalazione-12.docx`.

La cronologia registra anche proroga e progetto. I modelli sono gli stessi per tutte le convenzioni (PO, 15/11): la
scelta del modello non cambia.

Alternativa scartata: un `kind` per la convenzione della proroga. I modelli sarebbero due, mentre il PO li vuole uguali.

### D2 — Valori della convenzione e del progetto

| Segnaposto | Convenzione iniziale | Proroga *k* |
|---|---|---|
| `NUMERO_CONVENZIONE`, `DATA_CONVENZIONE`, `DATA_STIPULA_CONVENZIONE` | quelli della scheda | quelli della proroga |
| `INIZIO_CONVENZIONE`, `FINE_CONVENZIONE` | inizio e fine prevista della scheda | dal / al della proroga |

Nel progetto formativo, la convenzione è quella del progetto. Ci sono anche:
- `NUMERO_PROGETTO_FORMATIVO`, `INIZIO_PROGETTO_FORMATIVO`, `FINE_PROGETTO_FORMATIVO`;
- `MESI_PROGETTO_FORMATIVO`, `ORE_PROGETTO_FORMATIVO`, con i valori già calcolati dal server;
- `MANSIONE_PROGETTO_FORMATIVO`, `DETTAGLIO_MANSIONE_PROGETTO_FORMATIVO`.

I segnaposto di oggi tengono il significato:
- `DATA_INIZIO` è l'inizio del tirocinio;
- `DATA_FINE` è la fine prevista del tirocinio con le proroghe, cioè la fine dell'ultima convenzione (uguale a oggi
  se non ci sono proroghe);
- `MONTE_ORE_TOTALE` somma tutte le convenzioni;
- `MANSIONE` è quella della segnalazione.

### D3 — Relazione finale a tirocinio chiuso

Si scarica solo con la segnalazione in `FORM_RESTITUZIONE`, cioè con il report di restituzione inviato; prima la
risposta è `409` («La relazione finale si scarica a tirocinio chiuso»). Valori dal report:
- `DATA_FINE_EFFETTIVA`: fine effettiva;
- `DATA_CHIUSURA`: giorno dell'invio del report, ora italiana;
- `MOTIVO_CHIUSURA`: nome del motivo;
- `DESCRIZIONE_INTERRUZIONE`: descrizione dell'interruzione anticipata.

I report inviati prima di US-1009 non hanno la fine effettiva: la riga resta da completare.

Il luogo è `SEDE_COMPETENZA`, che vale in tutti i documenti. Il modello generico della relazione finale si aggiunge a
quelli nel codice (`templates/documents/relazione-finale/default.docx`, generato da `tools/genera-modelli-docx.py`).

### D4 — Mesi del tirocinio

`MESI_TIROCINIO` va dall'inizio alla fine effettiva oppure, se il tirocinio non è chiuso, alla fine prevista. Conta
solo i giorni coperti da una convenzione (US-1012), sospensioni comprese.

Il calcolo:
1. le convenzioni consecutive (una inizia il giorno dopo la fine dell'altra) diventano un solo periodo;
2. per ogni periodo, mesi interi e giorni restanti;
3. si sommano i mesi e i giorni;
4. ogni 30 giorni fanno un mese, e un resto di 15 giorni o più vale un mese in più.

Con un solo periodo il risultato è quello di `InternshipMonths.rounded`, la regola dei progetti formativi.

### D5 — Dati del promotore nel `.env`

`nexus.promoter.*` in `ApplicationProperties`. Ogni proprietà è letta da `application.yml` con un nome leggibile
(`${NEXUS_PROMOTER_NAME:}` e così via), e il compose le passa dal `.env`. Sono le 14 variabili concordate con il PO
nello Sprint 14 (`docs/agile/sprints/sprint-14.md`):

| Variabile | Segnaposto |
|---|---|
| `NEXUS_PROMOTER_NAME` | `PROMOTORE` |
| `NEXUS_PROMOTER_ADDRESS` | `SEDE_PROMOTORE` |
| `NEXUS_PROMOTER_FISCAL_CODE` | `CODICE_FISCALE_PROMOTORE` |
| `NEXUS_PROMOTER_REPRESENTATIVE` | `LEGALE_RAPPRESENTANTE_PROMOTORE` |
| `NEXUS_PROMOTER_REPRESENTATIVE_ROLE` | `RUOLO_LEGALE_RAPPRESENTANTE_PROMOTORE` |
| `NEXUS_PROMOTER_REPRESENTATIVE_BIRTH_PLACE` | `LUOGO_NASCITA_LEGALE_RAPPRESENTANTE_PROMOTORE` |
| `NEXUS_PROMOTER_REPRESENTATIVE_BIRTH_DATE` | `DATA_NASCITA_LEGALE_RAPPRESENTANTE_PROMOTORE` |
| `NEXUS_PROMOTER_PHONE` | `TELEFONO_PROMOTORE` |
| `NEXUS_PROMOTER_EMAIL` | `EMAIL_PROMOTORE` |
| `NEXUS_PROMOTER_PEC` | `PEC_PROMOTORE` |
| `NEXUS_PROMOTER_INAIL_POSITION` | `POSIZIONE_INAIL` |
| `NEXUS_PROMOTER_RC_INSURER` | `COMPAGNIA_RC` |
| `NEXUS_PROMOTER_RC_POLICY` | `POLIZZA_RC` |
| `NEXUS_PROMOTER_RC_EXPIRY` | `SCADENZA_RC` |

I valori sono testo, scritto come deve comparire (es. la data «25/03/1971»): un errore di battitura non blocca l'avvio
e si vede nel documento. Vuoti = righe da completare. Nel repository non c'è nessun valore reale.

### D6 — Dati dei documenti per progetto

V43 aggiunge a `project` due colonne facoltative:
- `notice_reference` (fino a 1000 caratteri);
- `header_line` (fino a 300).

Entrambe hanno un vincolo con nome: se c'è un valore, non è vuoto.

Il testo predefinito dell'avviso è una costante nel codice (Avviso pubblico n. 10 di GOL). Una colonna vuota vale il
predefinito: chi aggiunge un progetto non deve riscriverlo, e se cambia la legge cambia una riga sola.

L'API:
- `GET /document-catalogs/project-documents` restituisce, per ogni progetto, il testo in vigore e se è il
  predefinito;
- `PUT /document-catalogs/projects/{id}/documents` salva i due testi, solo per l'ADMIN. Un testo vuoto torna al
  predefinito; ogni modifica va nell'audit del progetto.

Segnaposto: `AVVISO` e `INTESTAZIONE_PROGETTO`.

### D7 — Segnaposto descritti e regole dei valori vuoti

`DocumentPlaceholders` diventa un elenco ordinato di voci con nome, gruppo e descrizione. `KNOWN` resta l'insieme dei
nomi, usato per controllare i modelli. I gruppi sono:
- Segnalazione e progetto;
- Soggetto promotore;
- Ente di presa in carico;
- Soggetto ospitante;
- Tirocinante;
- Tirocinio e orario;
- Convenzione;
- Progetto formativo;
- Chiusura.

`GET /document-templates/placeholders` restituisce le voci e la pagina le mostra per gruppo.

Valori mancanti:
- come oggi, un valore mancante è la riga `____________`;
- caselle (`SESSO_M`, `SESSO_F`, `GIORNO_LUNEDI` … `GIORNO_DOMENICA`): sempre `☒` o `☐`, quindi senza M o F restano
  entrambe vuote;
- fasce per giorno (`LUNEDI_MATTINA` … `DOMENICA_POMERIGGIO`): l'orario «09:00–13:00» se il giorno ha quella fascia,
  altrimenti vuote. Sono celle di una tabella: una riga in ogni cella vuota renderebbe illeggibile la griglia.

### D8 — Valori in una classe a parte

I valori passano a `DocumentValues` (in `services.documents`): riceve segnalazione, scheda, promotore, convenzione,
progetto e report, e restituisce la mappa. Il servizio resta su scelta del documento, modello e audit, e i valori si
provano con test unitari, senza Spring.

### D9 — Sezione «Documenti» nella scheda

Un gruppo per documento (`h3`). Ogni gruppo ha la scelta del modello, quando ce n'è più d'uno, e un pulsante per ogni
documento:
- convenzione: «Scarica la Convenzione», poi «Scarica la Convenzione della proroga 1»…;
- progetto formativo: «Scarica il Progetto Formativo 1»…, poi «Proroga 1: scarica il Progetto Formativo 1»…; senza
  progetti, un solo pulsante come oggi;
- relazione finale: il pulsante solo a tirocinio chiuso, altrimenti la frase «Si scarica a tirocinio chiuso…».

Non un pulsante disabilitato: non riceverebbe il focus e il motivo non si leggerebbe. L'esito si annuncia nella regione
di stato di oggi.

Durante un download i pulsanti restano attivi con `aria-disabled` e ignorano i clic: disabilitato, il pulsante
premuto perderebbe il focus, che in Chrome finirebbe sul `body` (difetto trovato nel controllo in Chrome, c'era già
prima). I pulsanti hanno etichette lunghe: vanno a capo e si restringono, così a 390 px la pagina non sborda.

## Risks / Trade-offs

- [Tanti segnaposto] → descritti e raggruppati nella pagina dei modelli; il controllo al caricamento elenca quelli
  sconosciuti.
- [Dati del promotore assenti sul server] → righe da completare, non errori; il README dice come impostarli.
- [Una mansione o una data cambiano dopo il download] → come oggi: il documento è quello del momento.
- [Mesi con convenzioni non consecutive] → regola di D4, provata con test; con le proroghe consecutive coincide con
  quella dei progetti.

## Migration Plan

Deploy normale. V43 aggiunge due colonne vuote, che i dati attuali rispettano. In caso di ripristino (A16) la release
precedente ignora le colonne.

Sul server, prima o dopo il deploy:
1. aggiungere le righe `NEXUS_PROMOTER_*` al `.env`;
2. rifare il deploy o riavviare il backend.

## Open Questions

Nessuna.
