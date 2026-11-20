## MODIFIED Requirements

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

#### Scenario: Tipo non indicato
- **WHEN** l'operatore propone una mansione a una segnalazione senza tipo di tirocinio
- **THEN** la risposta è `409` e l'interfaccia spiega che il tipo lo indica il Tutor

#### Scenario: Segnalazione di un collega
- **WHEN** un operatore abbina una segnalazione in carico a un collega
- **THEN** l'abbinamento riesce, la segnalazione resta in carico al collega e l'audit indica l'operatore che ha agito

## ADDED Requirements

### Requirement: Rilascio della mansione per una sola segnalazione
Il rifiuto della proposta, la scadenza del timer, l'appuntamento fallito e il report di restituzione con la mansione
ancora disponibile SHALL liberare la mansione solo per la segnalazione interessata. Le altre assegnazioni della
mansione restano. Con il report di restituzione e la mansione non più disponibile, l'assegnazione resta, come oggi.

#### Scenario: Rifiuto con un'altra assegnazione
- **WHEN** il Tutor rifiuta la proposta di una mansione assegnata anche a una segnalazione di un altro tipo
- **THEN** la mansione resta assegnata all'altra segnalazione ed è di nuovo proponibile per il tipo della segnalazione
  rifiutata
