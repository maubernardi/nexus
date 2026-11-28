## Why

Storia **US-309** (Sprint 20), dalla ricerca A42 della review dello Sprint 19. Oggi, se il Tutor sbaglia il progetto o
la mansione richiesta di una segnalazione, nessuno li può correggere dall'app: resta solo un intervento sul database.

## What Changes

- **Correzione dopo l'invio, solo da parte del Tutor della segnalazione** (PO, 28/11), finché la segnalazione non ha
  una proposta all'azienda: nuova, in attesa dell'approvazione della tipologia, in lavorazione. Call Center e ADMIN non
  la correggono.
- **Segnalazione riaperta** (PO, 28/11): il Tutor corregge la mansione richiesta, per le nuove proposte; il progetto
  resta quello.
- **Progetto**: uno dei progetti del Tutor. Se la sede di competenza (US-308) o il tipo di tirocinio (US-605) non
  valgono per il progetto nuovo, il Tutor li sceglie di nuovo tra quelli del progetto.
- **Mansione richiesta**: un'altra tipologia attiva del catalogo. Se la segnalazione aspetta l'approvazione della nuova
  tipologia (US-302), si corregge il testo libero.
- **Storico e audit** (EN-2): la correzione compare nello storico della segnalazione, con prima e dopo.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `ticket-submission`: correzione del progetto e della mansione richiesta dopo l'invio.

## Non-goals

- Correggere la segnalazione dalla prima proposta in poi, salvo la mansione della segnalazione riaperta; il progetto
  della riaperta.
- Correggere le candidature dalla bacheca: hanno la mansione e, se riservato, il progetto dell'annuncio.
- Passare dalla tipologia del catalogo al testo libero o viceversa: per una tipologia nuova resta l'approvazione
  (US-302).
- Correggere il beneficiario della segnalazione.
- Avvisi al Call Center per la correzione: la vede nello storico.

## Impact

- **Database**: nessuna migrazione.
- **Backend**:
  - `PUT /api/v1/tickets/{id}/request`, solo per il Tutor della segnalazione;
  - `canCorrectRequest` e `canCorrectProject` nel dettaglio;
  - voce «REQUEST_CORRECTED» nello storico.
- **Frontend**: nel dettaglio della segnalazione il comando «Correggi progetto e mansione», con il modulo (progetto,
  mansione, sede e tipo di tirocinio quando cambia il progetto), oppure «Correggi la mansione» nella riaperta, e la
  voce nello storico; testi in `it.ts`.
- **Accessibilità**: modulo con etichette, aiuti e riepilogo degli errori; esito annunciato; test con axe.
- **Collaudo**: prove T-42 sul PC del PO prima del merge (EN-11).
