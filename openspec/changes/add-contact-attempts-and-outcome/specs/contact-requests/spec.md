## ADDED Requirements

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
