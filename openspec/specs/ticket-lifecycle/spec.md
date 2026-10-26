# ticket-lifecycle Specification

## Purpose
Regole comuni a tutti i cambi di stato del ticket: solo le transizioni ammesse per stato e ruolo, cronologia completa,
nessuna sovrascrittura silenziosa tra utenti concorrenti.
## Requirements
### Requirement: Transizioni ammesse
Il sistema SHALL applicare a un ticket solo le transizioni dichiarate nel catalogo per il suo stato corrente e per il
ruolo dell'utente. Una transizione non ammessa dallo stato corrente SHALL essere rifiutata con `409` senza modificare il
ticket; una transizione non consentita al ruolo SHALL essere rifiutata con `403`.

#### Scenario: Transizione non ammessa
- **WHEN** si richiede una transizione il cui stato di partenza non è quello corrente del ticket
- **THEN** la risposta è `409`, lo stato del ticket non cambia e non viene scritta cronologia

#### Scenario: Ruolo non autorizzato
- **WHEN** un utente richiede una transizione non consentita al suo ruolo
- **THEN** la risposta è `403` e il ticket non cambia

### Requirement: Cronologia degli stati
Il sistema SHALL registrare in `ticket_status_history` ogni cambio di stato riuscito, compresa la creazione, con stato di
partenza (nullo alla creazione), stato di arrivo, autore, istante e nota facoltativa.

#### Scenario: Transizione riuscita
- **WHEN** una transizione ammessa viene applicata
- **THEN** il ticket assume il nuovo stato e la cronologia ha una nuova riga con autore e nota

### Requirement: Modifiche concorrenti
Il sistema SHALL rifiutare con `409` una transizione basata su una versione del ticket non più attuale, sia quando la
versione attesa inviata dal client è superata, sia quando due transazioni modificano lo stesso ticket in parallelo.

#### Scenario: Due operatori sullo stesso ticket
- **WHEN** due richieste partono dalla stessa versione del ticket e la prima va a buon fine
- **THEN** la seconda riceve `409`, lo stato resta quello impostato dalla prima e la cronologia ha una sola riga nuova

### Requirement: Accettazione della proposta
Il Tutor della segnalazione SHALL poter accettare la proposta di una segnalazione in `PROPOSTA_AZIENDA`, previa
conferma: la segnalazione passa in `PROPOSTA_ACCOLTA` e parte un timer che scade alla fine del 7° giorno lavorativo
successivo, secondo il calendario unico. La scadenza SHALL essere visibile nel dettaglio a chi vede la segnalazione.
L'accettazione SHALL essere registrata nell'audit e nello storico dell'azienda. Altri utenti MUST NOT poter accettare.

#### Scenario: Accettazione
- **WHEN** il Tutor della segnalazione accetta la proposta martedì 27 ottobre 2026
- **THEN** lo stato è "Proposta accolta", la scadenza del timer è giovedì 5 novembre 2026, la cronologia mostra
  "Proposta accettata" con la scadenza e lo storico dell'azienda conta una proposta accettata

#### Scenario: Collega di progetto o Call Center
- **WHEN** un collega di progetto o un operatore apre la segnalazione
- **THEN** non trova il comando di accettazione e l'API risponde `403`

#### Scenario: Accettazione già avvenuta o versione superata
- **WHEN** il Tutor accetta una proposta già accettata o partendo da una versione superata
- **THEN** il sistema risponde `409`

