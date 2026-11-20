# job-matching Specification

## Purpose
Ricerca delle mansioni compatibili con una segnalazione e abbinamento con proposta all'azienda (fase 4).
## Requirements
### Requirement: Mansioni compatibili
Il sistema SHALL proporre per una segnalazione solo le mansioni a disposizione, di aziende attive e non escluse per
quella segnalazione, che non sono già assegnate a un'altra segnalazione dello stesso tipo di tirocinio, filtrabili per
zona e tipologia. Una mansione assegnata a una segnalazione di cui non si conosce il tipo non è proponibile. Una
mansione assegnata a segnalazioni di altri tipi SHALL comparire con l'avviso ben visibile «Già assegnata alla
segnalazione n. … (tipo)», anche nei dettagli e nella conferma della proposta. Nell'interfaccia i filtri SHALL partire
dalla zona di residenza del beneficiario e dalla mansione richiesta, e l'operatore SHALL poterli allargare a tutte le
zone o tipologie.

#### Scenario: Esclusioni
- **WHEN** l'operatore cerca le mansioni per una segnalazione Extracurriculare
- **THEN** non compaiono mansioni ritirate, di aziende disattivate, di aziende escluse per la segnalazione o già
  assegnate a una segnalazione Extracurriculare

#### Scenario: Mansione assegnata a un altro tipo
- **WHEN** l'operatore cerca le mansioni per una segnalazione Extracurriculare e una mansione è assegnata alla
  segnalazione n. 30, Di inclusione
- **THEN** la mansione compare con l'avviso «Già assegnata alla segnalazione n. 30 (Di inclusione)», che si ritrova nei
  dettagli e nella conferma della proposta

#### Scenario: Filtri allargati
- **WHEN** l'operatore sceglie "Tutte le zone"
- **THEN** l'elenco include le mansioni compatibili di ogni zona e il numero di risultati viene annunciato

### Requirement: Abbinamento e proposta
Il sistema SHALL permettere a qualunque operatore del Call Center, o all'ADMIN, di abbinare una segnalazione presa in
carico, con il tipo di tirocinio indicato, a una mansione compatibile, anche se è in carico a un collega. La mansione
diventa assegnata alla segnalazione per quel tipo, la segnalazione passa in `PROPOSTA_AZIENDA`, l'assegnazione
all'operatore non cambia e l'azione è registrata con il suo autore nell'audit, nella cronologia e nell'audit trail
dell'azienda. Il sistema SHALL rispondere `409` con un messaggio chiaro, senza modificare nulla, se:
- la mansione è appena stata assegnata a un'altra segnalazione dello stesso tipo, o è cambiata;
- la segnalazione non è ancora stata presa in carico;
- la segnalazione non ha il tipo di tirocinio.

#### Scenario: Proposta riuscita
- **WHEN** l'operatore conferma la proposta di una mansione compatibile
- **THEN** la segnalazione è in `PROPOSTA_AZIENDA` con la mansione proposta e la mansione è assegnata alla segnalazione

#### Scenario: Stessa mansione, due tipi
- **WHEN** la mansione «Cameriere» è assegnata a una segnalazione Extracurriculare e l'operatore la propone a una
  segnalazione Di inclusione
- **THEN** la proposta riesce e la mansione è assegnata a entrambe

#### Scenario: Stesso tipo
- **WHEN** l'operatore propone una mansione già assegnata a un'altra segnalazione dello stesso tipo
- **THEN** la risposta è `409` con il numero di quella segnalazione e la segnalazione resta in lavorazione

#### Scenario: Mansione appena bloccata
- **WHEN** la mansione scelta è stata assegnata a un'altra segnalazione, o modificata, un istante prima
- **THEN** la risposta è `409`, la segnalazione resta in lavorazione e l'interfaccia spiega cosa è successo

#### Scenario: Tipo non indicato
- **WHEN** l'operatore propone una mansione a una segnalazione senza tipo di tirocinio
- **THEN** la risposta è `409` e l'interfaccia spiega che il tipo lo indica il Tutor

#### Scenario: Segnalazione di un collega
- **WHEN** un operatore abbina una segnalazione in carico a un collega
- **THEN** l'abbinamento riesce, la segnalazione resta in carico al collega e l'audit indica l'operatore che ha agito

### Requirement: Presa in carico di una segnalazione di un collega
Il sistema SHALL permettere a un operatore di spostare su di sé una segnalazione in carico a un collega ("Prendila in
carico tu"), partendo dalla versione vista, con un evento di audit che riporta l'operatore prima e dopo.

#### Scenario: Collega assente
- **WHEN** un operatore usa "Prendila in carico tu" su una segnalazione in carico a un collega
- **THEN** la segnalazione è in carico a lui, compare nelle sue lavorazioni e l'audit registra `TAKE_OVER`

### Requirement: Dettagli di azienda e mansione durante l'abbinamento
Per ogni mansione compatibile il sistema SHALL offrire "Dettagli", che apre un dialogo modale con la scheda
dell'azienda (ragione sociale, partita IVA, sede, referente e contatti, stato), la scheda della mansione (titolo,
tipologia, zona, descrizione), le note più recenti e lo storico con il riepilogo dei pregressi. Dal dialogo SHALL essere
possibile proporre la mansione con la stessa conferma della tabella. Il dialogo SHALL tenere il focus al suo interno,
chiudersi con Esc o "Chiudi" riportando il focus su "Dettagli", e occupare l'intero schermo sui dispositivi piccoli.

#### Scenario: Consultazione
- **WHEN** l'operatore preme "Dettagli" su una mansione compatibile
- **THEN** il dialogo mostra azienda, mansione, note recenti e pregressi, con il link alla scheda completa dell'azienda

#### Scenario: Chiusura
- **WHEN** l'operatore preme Esc nel dialogo
- **THEN** il dialogo si chiude e il focus torna sul pulsante "Dettagli" di quella mansione

#### Scenario: Proposta dal dialogo
- **WHEN** l'operatore preme "Proponi" nel dialogo
- **THEN** il dialogo si chiude e compare la conferma della proposta, con il focus sul suo titolo, senza proposta
  inviata finché non conferma

### Requirement: Esclusione di un'azienda
Il Call Center e l'ADMIN SHALL poter escludere un'azienda per una segnalazione presa in carico, in lavorazione o
riaperta, indicando un motivo obbligatorio; l'azienda esclusa MUST NOT comparire più tra le mansioni compatibili della
segnalazione. L'esclusione SHALL essere registrata nella cronologia (con il motivo) e nello storico dell'azienda, e le
aziende escluse SHALL essere elencate nel dettaglio con motivo, autore e data.

#### Scenario: Esclusione
- **WHEN** l'operatore esclude Alfa Logistica con il motivo "Non assume nel periodo estivo"
- **THEN** Alfa Logistica non compare più tra le compatibili, l'elenco delle escluse la mostra con il motivo e la
  cronologia registra l'esclusione

#### Scenario: Motivo mancante o azienda già esclusa
- **WHEN** l'operatore non indica il motivo, o l'azienda è già esclusa
- **THEN** il sistema chiede il motivo, oppure risponde `409`

#### Scenario: Stato o ruolo non ammessi
- **WHEN** la segnalazione non è presa in carico, o chi esclude è un Tutor
- **THEN** la risposta è rispettivamente `409` e `403`

### Requirement: Rilascio della mansione per una sola segnalazione
Il rifiuto della proposta, la scadenza del timer, l'appuntamento fallito e il report di restituzione con la mansione
ancora disponibile SHALL liberare la mansione solo per la segnalazione interessata. Le altre assegnazioni della
mansione restano. Con il report di restituzione e la mansione non più disponibile, l'assegnazione resta, come oggi.

#### Scenario: Rifiuto con un'altra assegnazione
- **WHEN** il Tutor rifiuta la proposta di una mansione assegnata anche a una segnalazione di un altro tipo
- **THEN** la mansione resta assegnata all'altra segnalazione ed è di nuovo proponibile per il tipo della segnalazione
  rifiutata

