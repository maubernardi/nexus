# contact-requests Specification

## Purpose
Permette al Tutor di chiedere al Call Center di contattare un'azienda che non è ancora in anagrafica, e al Call Center
di prendere in carico la richiesta, con una traccia che non si perde.
## Requirements
### Requirement: Richiesta di contatto dal Tutor
Il Tutor SHALL poter chiedere al Call Center di contattare un'azienda che non è ancora in anagrafica (PO, 27/11): per
un'azienda già presente la richiesta MUST NOT esserci. La richiesta è generale: MUST NOT riguardare un beneficiario né
una segnalazione. Il Tutor scrive il nome e sceglie la zona del catalogo, tutti e due obbligatori; la categoria
merceologica è facoltativa. La zona e la categoria MUST essere voci attive del catalogo. Ogni richiesta SHALL avere un
numero progressivo, «Richiesta di contatto n. N», e lo stato «aperta» all'invio. L'invio SHALL entrare nell'audit con
autore e istante. Solo il Tutor MUST poter inviare una richiesta.

#### Scenario: Nessuna richiesta dalla scheda
- **WHEN** il Tutor apre la scheda di un'azienda già in anagrafica
- **THEN** non trova un pulsante per chiedere di contattarla

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
Prima dell'invio il sistema SHALL mostrare al Tutor le aziende attive con un nome simile, al massimo cinque, anche con
piccole differenze di scrittura. Se è una di quelle, l'azienda è già in anagrafica e la richiesta non serve: il Tutor
ne SHALL poter aprire la scheda, e MUST NOT poter chiedere di contattarla. Altrimenti conferma che l'azienda è nuova e
la richiesta parte. Senza nomi simili la richiesta parte subito. Il Tutor MUST vedere solo i dati essenziali delle
aziende, come nella sua lista.

#### Scenario: Nome simile
- **WHEN** il Tutor scrive «Demo Ristorazioni» e invia
- **THEN** prima dell'invio vede «Demo Ristorazione S.n.c.» tra le aziende con un nome simile, con la sua scheda, e la
  richiesta parte solo se conferma che l'azienda è nuova

#### Scenario: Nessun nome simile
- **WHEN** il Tutor scrive un nome che non somiglia a nessuna azienda e invia
- **THEN** la richiesta parte senza passaggi in più

### Requirement: Richieste di contatto del Call Center
Il Call Center e l'ADMIN SHALL vedere la pagina «Richieste di contatto» con le richieste aperte e in carico: in cima
quelle aperte, da prendere in carico, poi quelle in carico, ognuna dalla più vecchia (PO, 27/11). Per ognuna: numero,
data, Tutor, azienda (nome, zona e categoria scritti dal Tutor), stato e operatore. Nel menu, accanto alla voce, SHALL
comparire il numero delle richieste aperte, cioè ancora da prendere in carico. Il numero MUST essere anche nel nome accessibile della voce. Per una richiesta nuova
MUST NOT partire nessuna email agli operatori.

#### Scenario: Elenco e numero nel menu
- **WHEN** ci sono due richieste aperte e una in carico
- **THEN** l'operatore vede in cima le due aperte, dalla più vecchia, poi quella in carico, e nel menu il numero 2

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

### Requirement: Pagina della richiesta di contatto
Ogni richiesta di contatto SHALL avere la sua pagina con i dati della richiesta, i tentativi dal più recente e, se è
chiusa, l'esito, la nota, l'istante della chiusura e l'azienda collegata. Il Tutor SHALL vedere solo le proprie
richieste; Call Center e ADMIN tutte. Negli elenchi il numero della richiesta SHALL aprire la sua pagina.

#### Scenario: Il Tutor apre la sua richiesta
- **WHEN** il Tutor preme il numero di una sua richiesta in «Le mie richieste»
- **THEN** vede la pagina della richiesta con i tentativi, senza moduli

#### Scenario: Richiesta di un altro Tutor
- **WHEN** un Tutor apre la pagina di una richiesta di un altro Tutor
- **THEN** la richiesta non risulta

### Requirement: Tentativi di contatto
L'operatore che ha la richiesta in carico SHALL poter segnare un tentativo di contatto: la data, che MUST NOT essere
futura, l'esito e una nota facoltativa per il Tutor. Gli esiti SHALL essere fissi: «Disponibile a ospitare tirocini»,
«Non interessata», «Non raggiungibile». Un tentativo MUST NOT chiudere la richiesta. Gli altri utenti MUST NOT poter
segnare tentativi, e una richiesta aperta o chiusa MUST NOT riceverne. Il tentativo SHALL entrare nell'audit con autore
e istante, senza la nota.

#### Scenario: Tentativo
- **WHEN** l'operatore che ha la richiesta segna un tentativo di oggi «Non raggiungibile» con una nota
- **THEN** il tentativo compare nella pagina della richiesta, anche per il Tutor, e la richiesta resta in carico

#### Scenario: Data futura
- **WHEN** l'operatore segna un tentativo con la data di domani
- **THEN** il tentativo non si salva e la data è indicata

#### Scenario: Un altro operatore
- **WHEN** un operatore che non ha la richiesta in carico prova a segnare un tentativo
- **THEN** riceve un avviso con il nome di chi ce l'ha

### Requirement: Chiusura con l'esito
L'operatore che ha la richiesta in carico SHALL poterla chiudere a mano, quando decide, con l'esito finale, tra gli
stessi tre, e una nota per il Tutor, tutti e due obbligatori. Alla chiusura SHALL poter collegare la richiesta a
un'azienda attiva dell'anagrafica, cercandola per nome; il collegamento è facoltativo. Prima della chiusura il sistema
SHALL chiedere una conferma. Una richiesta chiusa MUST NOT riaprirsi né cambiare. La chiusura SHALL entrare nell'audit
con autore, istante, esito e azienda collegata, senza la nota.

#### Scenario: Chiusura con l'azienda collegata
- **WHEN** l'operatore chiude la richiesta con l'esito «Disponibile a ospitare tirocini», una nota e l'azienda appena
  creata in anagrafica, e conferma
- **THEN** la richiesta è chiusa, sparisce dall'elenco del Call Center e nella sua pagina il nome apre la scheda
  dell'azienda

#### Scenario: Esito o nota mancanti
- **WHEN** l'operatore prova a chiudere senza l'esito o senza la nota
- **THEN** la richiesta non si chiude e il campo mancante è indicato

### Requirement: Esito in «Le mie richieste»
In «Le mie richieste» lo stato di una richiesta chiusa SHALL mostrare anche l'esito finale.

#### Scenario: Richiesta chiusa
- **WHEN** l'operatore chiude la richiesta del Tutor con l'esito «Non interessata»
- **THEN** in «Le mie richieste» la richiesta risulta «Chiusa: Non interessata»

