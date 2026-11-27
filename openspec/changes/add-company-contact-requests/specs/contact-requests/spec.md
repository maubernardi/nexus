## Purpose
Permette al Tutor di chiedere al Call Center di contattare un'azienda, nuova o già presente, e al Call Center di
prendere in carico la richiesta, con una traccia che non si perde.

## ADDED Requirements

### Requirement: Richiesta di contatto dal Tutor
Il Tutor SHALL poter chiedere al Call Center di contattare un'azienda. La richiesta è generale: MUST NOT riguardare un
beneficiario né una segnalazione. Ogni richiesta SHALL avere un numero progressivo, «Richiesta di contatto n. N», e lo
stato «aperta» all'invio. La richiesta indica l'azienda in uno di due modi:
- **azienda già presente**: dalla scheda dell'azienda del Tutor, con «Chiedi di contattarla». La richiesta nasce
  collegata all'azienda, che MUST essere attiva;
- **azienda nuova**: il Tutor scrive il nome e sceglie la zona del catalogo, tutti e due obbligatori; la categoria
  merceologica è facoltativa. La zona e la categoria MUST essere voci attive del catalogo.

L'invio SHALL entrare nell'audit con autore e istante. Solo il Tutor MUST poter inviare una richiesta.

#### Scenario: Azienda già presente
- **WHEN** il Tutor apre la scheda di un'azienda e preme «Chiedi di contattarla»
- **THEN** nasce la richiesta aperta collegata all'azienda, con il suo numero, e il Tutor la trova in «Le mie
  richieste»

#### Scenario: Azienda nuova
- **WHEN** il Tutor scrive il nome «Trattoria del Ponte», sceglie una zona e invia
- **THEN** nasce la richiesta aperta per l'azienda nuova, con il nome e la zona, e l'audit registra l'invio

#### Scenario: Dati mancanti
- **WHEN** il Tutor invia una richiesta per un'azienda nuova senza il nome o senza la zona
- **THEN** la richiesta non nasce e il campo mancante è indicato

#### Scenario: Altri ruoli
- **WHEN** un operatore del Call Center prova a inviare una richiesta
- **THEN** riceve un rifiuto per mancanza di permessi

### Requirement: Nomi simili prima dell'invio
Prima di inviare una richiesta per un'azienda nuova, il sistema SHALL mostrare al Tutor le aziende attive con un nome
simile, al massimo cinque, anche con piccole differenze di scrittura. Per ognuna il Tutor SHALL poter aprire la scheda
oppure chiedere di contattare quella; altrimenti conferma che l'azienda è nuova e la richiesta parte. Senza nomi
simili la richiesta parte subito. Il Tutor MUST vedere solo i dati essenziali delle aziende, come nella sua lista.

#### Scenario: Nome simile
- **WHEN** il Tutor scrive «Demo Ristorazione» e invia
- **THEN** prima dell'invio vede «Demo Ristorazione S.n.c.» tra le aziende con un nome simile, e la richiesta parte
  solo se conferma che l'azienda è nuova o se sceglie quella esistente

#### Scenario: Nessun nome simile
- **WHEN** il Tutor scrive un nome che non somiglia a nessuna azienda e invia
- **THEN** la richiesta parte senza passaggi in più

### Requirement: Richieste di contatto del Call Center
Il Call Center e l'ADMIN SHALL vedere la pagina «Richieste di contatto» con le richieste aperte e in carico, dalla più
vecchia: numero, data, Tutor, azienda (con il collegamento alla scheda se è già presente, altrimenti nome, zona e
categoria), stato e operatore. Nel menu, accanto alla voce, SHALL comparire il numero delle richieste aperte, cioè
ancora da prendere in carico. Il numero MUST essere anche nel nome accessibile della voce. Per una richiesta nuova
MUST NOT partire nessuna email agli operatori.

#### Scenario: Elenco e numero nel menu
- **WHEN** ci sono due richieste aperte e una in carico
- **THEN** l'operatore vede le tre richieste, dalla più vecchia, e nel menu il numero 2

### Requirement: Presa in carico
Un operatore qualsiasi del Call Center SHALL poter prendere in carico una richiesta aperta: la richiesta passa «in
carico» e mostra chi ce l'ha. Una richiesta già in carico MUST NOT essere presa da un altro: chi ci prova riceve un
avviso con il nome di chi ce l'ha. La presa in carico SHALL entrare nell'audit con autore e istante.

#### Scenario: Presa in carico
- **WHEN** l'operatore preme «Prendi in carico» su una richiesta aperta
- **THEN** la richiesta è in carico a lui, il numero nel menu scende di uno e l'audit registra la presa in carico

#### Scenario: Già presa
- **WHEN** due operatori prendono in carico la stessa richiesta quasi insieme
- **THEN** la prende il primo, e il secondo riceve un avviso con il nome di chi ce l'ha

### Requirement: Le mie richieste
Il Tutor SHALL vedere in «Le mie richieste» le richieste che ha inviato, dalla più recente: numero, data, azienda,
stato (aperta, in carico, chiusa) e operatore che ce l'ha. MUST NOT vedere le richieste degli altri Tutor.

#### Scenario: Stato visibile
- **WHEN** un operatore prende in carico la richiesta del Tutor
- **THEN** in «Le mie richieste» la richiesta risulta «In carico» con il nome dell'operatore

#### Scenario: Richieste degli altri
- **WHEN** un Tutor chiede una richiesta di un altro Tutor
- **THEN** non la vede
