## Why

Storia **US-1013** (Sprint 14, PO 16/11). Con US-1011 i dati del soggetto promotore stanno nel `.env` del server, uguali
per tutti i progetti. Il PO li vuole nel database e **per progetto**, perché possono cambiare da un progetto all'altro.
Vuole anche poter aggiungere **variabili libere** per progetto, da usare nei modelli come segnaposto, per i dati che
NEXUS non prevede. Il `.env` non era ancora stato scritto sul server: si cambia strada senza migrare dati.

## What Changes

- **Dati del promotore per progetto** (V44): i 14 dati di US-1011 diventano colonne facoltative di `project`, con date
  vere per la nascita del legale rappresentante e la scadenza della polizza. L'ADMIN li modifica in «Cataloghi», nella
  sezione dei dati dei documenti del progetto, con intestazione e avviso. I segnaposto (`{PROMOTORE}`, `{POLIZZA_RC}`…)
  non cambiano: i modelli di EN-8 restano validi.
- **Variabili libere per progetto** (V44, tabella `project_variable`): nome, valore e descrizione facoltativa.
  - Il nome (maiuscole, cifre e trattino basso, al massimo 50 caratteri) è il segnaposto `{NOME}` nei modelli di quel
    progetto. Non può essere un segnaposto di NEXUS, né ripetersi nel progetto.
  - Al caricamento di un modello sono ammessi i segnaposto di NEXUS e le variabili del progetto del modello.
  - `GET /document-templates/placeholders` aggiunge un gruppo per ogni progetto con variabili.
  - Una variabile usata da un modello attivo del progetto non si può togliere (`400` sul campo, con il nome del
    modello); un modello disattivato si riattiva solo se i suoi segnaposto sono ancora tutti noti (`409`).
- **Il `.env` esce di scena**: via `nexus.promoter.*` da `ApplicationProperties`, `application.yml` e compose; il README
  spiega i dati nei Cataloghi.
- **Audit** di ogni modifica, nel progetto: per nome, luogo e data di nascita del legale rappresentante solo che sono
  cambiati.

## Non-goals

- **Copiare i dati da un progetto all'altro**: si inseriscono progetto per progetto.
- **Variabili per tutti i progetti**: ogni variabile è di un progetto; un modello vale sempre per un progetto (US-1010).
- **Rinominare una variabile**: si toglie e se ne aggiunge un'altra (se non è usata da un modello).

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `reference-data`: dati del promotore e variabili libere per progetto.
- `internship-documents`: i dati del promotore vengono dal progetto, non dalla configurazione del server; le variabili
  del progetto nei documenti.
- `document-templates`: segnaposto ammessi = quelli di NEXUS più le variabili del progetto del modello.

## Impact

- Migrazione **V44** solo additiva: 14 colonne facoltative su `project`, tabella `project_variable`; vincoli con nome
  testati.
- API (solo ADMIN): `GET /document-catalogs/project-documents` e `PUT /document-catalogs/projects/{id}/documents` con
  promotore e variabili; `GET /document-templates/placeholders` con i gruppi delle variabili.
- Frontend: il modulo dei dati dei documenti del progetto ha anche promotore e variabili; la tabella dei Cataloghi
  mostra promotore e variabili, ed è una regione scorrevole raggiungibile da tastiera come le altre tabelle.
- Sul server: niente nel `.env`. Dopo il deploy l'ADMIN inserisce i dati del promotore di GOL in «Cataloghi».
