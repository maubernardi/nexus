## MODIFIED Requirements

### Requirement: Nuova tipologia di mansione dal Tutor
Il Tutor SHALL poter descrivere una tipologia di mansione che non è nel catalogo al posto di sceglierne una; la
segnalazione SHALL nascere in `IN_ATTESA_APPROVAZIONE` e non entrare in coda. Una tipologia già nel catalogo (stesso
nome, senza distinguere maiuscole) MUST essere scelta dall'elenco. Un operatore del Call Center o l'ADMIN SHALL poter:
approvarla, anche correggendone il nome, e allora la tipologia entra nel catalogo; oppure respingerla scegliendo una
tipologia del catalogo con un motivo obbligatorio. In entrambi i casi la segnalazione passa in `NUOVA` (coda) e la
decisione è registrata nell'audit e in cronologia; il Tutor della segnalazione ne riceve una notifica in app.

#### Scenario: Invio con tipologia nuova
- **WHEN** il Tutor descrive la tipologia "Tecnico del suono" e invia
- **THEN** la segnalazione è "In attesa di approvazione" e il Call Center la trova in "Da approvare", non in coda

#### Scenario: Approvazione con nome corretto
- **WHEN** l'operatore approva la tipologia con il nome "Tecnico audio"
- **THEN** "Tecnico audio" entra nel catalogo, la segnalazione va in coda con quella tipologia e la cronologia mostra
  "Nuova tipologia approvata"

#### Scenario: Respingimento
- **WHEN** l'operatore sceglie "Magazziniere" dal catalogo con il motivo "Rientra nel catalogo"
- **THEN** la segnalazione va in coda come "Magazziniere", la cronologia mostra proposta → scelta e il motivo, e il Tutor
  riceve la notifica "tipologia cambiata"

#### Scenario: Tipologia già nel catalogo
- **WHEN** il Tutor descrive una tipologia con il nome di una già presente
- **THEN** il sistema chiede di sceglierla dall'elenco

#### Scenario: Altri ruoli
- **WHEN** un Tutor prova ad approvare o respingere una tipologia
- **THEN** la risposta è `403`
