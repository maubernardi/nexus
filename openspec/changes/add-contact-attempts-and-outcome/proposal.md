## Why

Storia **US-512** (Sprint 19), dall'analisi con il PO del 23/11. Con US-507 il Tutor chiede al Call Center di contattare
un'azienda che non è in anagrafica, e un operatore prende in carico la richiesta. Poi non succede niente di visibile: i
tentativi di contatto e l'esito restano fuori da NEXUS, e il Tutor non sa com'è andata.

## What Changes

- **Pagina della richiesta di contatto**, una per tutti: i dati della richiesta, i tentativi, l'esito. Si apre dal
  numero negli elenchi:
  - per il Tutor da «Le mie richieste», in sola lettura;
  - per Call Center e ADMIN da «Richieste di contatto».
- **Tentativi**: l'operatore che ha la richiesta in carico segna data (non futura), esito e nota facoltativa. Gli
  esiti sono fissi: «Disponibile a ospitare tirocini», «Non interessata», «Non raggiungibile». Un tentativo non chiude
  la richiesta; il Tutor li vede tutti.
- **Chiusura a mano**, solo da parte dell'operatore che ha la richiesta in carico, quando decide:
  - esito finale e una nota, per il Tutor, obbligatori;
  - se l'azienda è in anagrafica, perché c'era o perché l'ha appena creata, la collega cercandola per nome;
  - prima di chiudere, una conferma: la chiusura non si annulla.
- **Avviso al Tutor** alla chiusura, nell'app e per email, con il numero, l'esito e la nota, e il collegamento alla
  richiesta. Testi tra i «Testi dei messaggi» (US-1204), con i segnaposto `{numero}`, `{esito}` e `{nota}`; email con
  oggetto «PORTALE NEXUS - Richiesta di contatto n. N - …» e firma «Portale Nexus».
- **Audit** (EN-2): tentativi e chiusura, con autore e istante; la nota (testo libero) no.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `contact-requests`: tentativi, chiusura con l'esito e pagina della richiesta.
- `notifications`: avviso di chiusura della richiesta di contatto al Tutor.

## Non-goals

- Un elenco delle richieste chiuse per il Call Center: restano nella pagina della richiesta e in «Le mie richieste»
  del Tutor.
- Modificare o cancellare un tentativo, o riaprire una richiesta chiusa.
- Creare l'azienda dalla richiesta: l'operatore la crea dall'anagrafica, come oggi, e poi la collega.
- Avvisi al Tutor per ogni tentativo: solo alla chiusura (PO, 23/11).

## Impact

- **Database: V57**:
  - tabella `contact_request_attempt`, con data, esito e nota;
  - in `contact_request` l'esito, la nota e l'istante della chiusura;
  - in `notification` la richiesta di contatto a cui porta l'avviso.

  Vincoli con nome, testati in `DomainConstraintsIT`.
- **Backend**:
  - `GET /api/v1/contact-requests/{id}`: il Tutor solo le sue, Call Center e ADMIN tutte;
  - `POST …/{id}/attempts` e `POST …/{id}/close`;
  - avviso `CONTACT_REQUEST_CLOSED` con i testi predefiniti;
  - email con l'oggetto della richiesta di contatto.
- **Frontend**:
  - pagina della richiesta, con i moduli per l'operatore;
  - numero cliccabile negli elenchi;
  - esito in «Le mie richieste»;
  - avviso che apre la richiesta;
  - testi in `it.ts`.
- **Accessibilità**: moduli con etichette e riepilogo degli errori; conferma della chiusura con il focus; esiti in
  testo; test con axe.
- **Pulizia della demo**: anche i tentativi.
- **Collaudo**: prove T-41 sul PC del PO prima del merge (EN-11).
