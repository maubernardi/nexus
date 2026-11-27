## Why

Storia **US-507** (Sprint 19), dall'analisi con il PO del 23/11. Un Tutor spesso conosce un'azienda che potrebbe
ospitare tirocini e chiede al Call Center di contattarla. Oggi la richiesta passa a voce o per email: non resta
traccia, non si sa chi se ne occupa e a volte si perde.

## What Changes

- **Richiesta di contatto**, generale: non riguarda un beneficiario né una segnalazione. Ha un numero progressivo, «Richiesta
  di contatto n. N», e uno stato: aperta, in carico, chiusa. La chiusura arriva con US-512.
- **Azienda nuova**, da «Le mie richieste» del Tutor:
  - nome e zona del catalogo obbligatori, categoria merceologica facoltativa;
  - prima dell'invio NEXUS mostra le aziende attive con un nome simile. Il Tutor può aprirne la scheda, chiedere di
    contattare una di quelle, oppure confermare che l'azienda è nuova.
- **Azienda già presente**: nella scheda dell'azienda del Tutor (US-511) il pulsante «Chiedi di contattarla». La
  richiesta nasce collegata all'azienda.
- **Call Center**:
  - la pagina «Richieste di contatto», dalla più vecchia;
  - nel menu il numero delle richieste ancora da prendere in carico;
  - nessuna email agli operatori;
  - un operatore qualsiasi prende in carico una richiesta aperta, e la richiesta mostra chi ce l'ha.
- **Tutor**: la pagina «Le mie richieste», dalla più recente, con lo stato e l'operatore che ce l'ha. Nel menu la
  voce è «Richieste».
- **Menu**: diventa a tendina sotto i 1280 pixel invece che sotto i 1024. Tra 1024 e 1279 pixel le voci non ci stavano
  più: per l'ADMIN e per i Tutor che coordinano un progetto non ci stavano già prima. Per l'ADMIN, inoltre, «Richieste
  di contatto» sta nel sottomenu «Amministrazione».
- **Audit** (EN-2): invio e presa in carico, con autore e istante.

## Capabilities

### New Capabilities
- `contact-requests`: richieste di contatto di un'azienda dal Tutor al Call Center.

### Modified Capabilities
- `dev-seed-data`: richieste di contatto dimostrative per il collaudo.

## Non-goals

- Tentativi di contatto, chiusura con l'esito e avviso al Tutor: sono US-512.
- Email o avvisi agli operatori per una richiesta nuova: basta il numero nel menu (PO, 23/11).
- Una nota libera del Tutor nella richiesta: non c'è nei criteri della storia.
- Impedire più richieste per la stessa azienda: NEXUS mostra i nomi simili e il Call Center vede tutte le richieste.
- Collegare la richiesta all'azienda del database alla chiusura: è US-512.

## Impact

- **Database: V56**:
  - tabella `contact_request` con il contatore `contact_request_number_seq`, stato, azienda o nome e zona della
    azienda nuova, categoria facoltativa, Tutor, operatore e istante della presa in carico;
  - vincoli con nome esplicito, testati in `DomainConstraintsIT`;
  - estensione `pg_trgm` per i nomi simili. È un'estensione «trusted»: la crea il proprietario del database, ed entra
    nel backup perché il dump è dell'intero database.
- **Backend**:
  - entità `ContactRequest`, servizio, repository e risorsa `/api/v1/contact-requests`;
  - nomi simili in `/api/v1/companies/directory/similar`;
  - tipo `CONTACT_REQUEST` nell'audit.
- **Frontend**:
  - pagine «Le mie richieste» e nuova richiesta, per il Tutor;
  - pagina «Richieste di contatto» per il Call Center;
  - voci del menu, con il numero;
  - pulsante «Chiedi di contattarla» nella scheda del Tutor;
  - testi in `it.ts`.
- **Accessibilità**:
  - numero del menu anche nel nome accessibile della voce;
  - nomi simili annunciati, con il focus sul loro riquadro;
  - esito della presa in carico annunciato;
  - test con axe.
- **PWA**: nessun cambiamento; le API restano fuori dalla cache.
- **Collaudo**: prove T-40 sul PC del PO prima del merge (EN-11).
